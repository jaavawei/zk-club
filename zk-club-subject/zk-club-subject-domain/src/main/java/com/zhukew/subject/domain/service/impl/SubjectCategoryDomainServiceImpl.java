package com.zhukew.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhukew.subject.common.enums.IsDeletedFlagEnum;
import com.zhukew.subject.domain.convert.SubjectCategoryConverter;
import com.zhukew.subject.domain.entity.SubjectCategoryBO;
import com.zhukew.subject.domain.entity.SubjectLabelBO;
import com.zhukew.subject.domain.service.SubjectCategoryDomainService;
import com.zhukew.subject.domain.util.CacheUtil;
import com.zhukew.subject.infra.basic.entity.SubjectCategory;
import com.zhukew.subject.infra.basic.entity.SubjectLabel;
import com.zhukew.subject.infra.basic.entity.SubjectMapping;
import com.zhukew.subject.infra.basic.service.SubjectCategoryService;
import com.zhukew.subject.infra.basic.service.SubjectLabelService;
import com.zhukew.subject.infra.basic.service.SubjectMappingService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubjectCategoryDomainServiceImpl implements SubjectCategoryDomainService {

    @Resource
    private SubjectCategoryService subjectCategoryService;

    @Resource
    private SubjectMappingService subjectMappingService;

    @Resource
    private SubjectLabelService subjectLabelService;

    @Resource
    private ThreadPoolExecutor labelThreadPool;

    @Resource
    private CacheUtil cacheUtil;

    @Override
    public void add(SubjectCategoryBO subjectCategoryBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryController.add.bo:{}", JSON.toJSONString(subjectCategoryBO));
        }
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE
                .convertBoToCategory(subjectCategoryBO);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectCategoryService.insert(subjectCategory);
    }

    @Override
    public List<SubjectCategoryBO> queryCategory(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE
                .convertBoToCategory(subjectCategoryBO);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectCategory> subjectCategoryList = subjectCategoryService.queryCategory(subjectCategory);
        List<SubjectCategoryBO> boList = SubjectCategoryConverter.INSTANCE
                .convertBoToCategory(subjectCategoryList);
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryController.queryPrimaryCategory.boList:{}",
                    JSON.toJSONString(boList));
        }
        boList.forEach(bo -> {
            Integer subjectCount = subjectCategoryService.querySubjectCount(bo.getId());
            bo.setCount(subjectCount);
        });
        return boList;
    }

    @Override
    public Boolean update(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE
                .convertBoToCategory(subjectCategoryBO);
        int count = subjectCategoryService.update(subjectCategory);
        return count > 0;
    }

    @Override
    public Boolean delete(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE
                .convertBoToCategory(subjectCategoryBO);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        int count = subjectCategoryService.update(subjectCategory);
        return count > 0;
    }

    @SneakyThrows
    @Override
    public List<SubjectCategoryBO> queryCategoryAndLabel(SubjectCategoryBO subjectCategoryBO) {
        Long id = subjectCategoryBO.getId();
        // 前端初始化界面展示的分类和标签需要经常利用多线程查询，浪费资源
        // 其实这些分类和标签很少发生变化，将其存入 Caffeine 本地缓存中
        // 后续现在本地缓存中查找，如果没有，再调用接口查找
        String cacheKey = "categoryAndLabel." + subjectCategoryBO.getId();
        List<SubjectCategoryBO> subjectCategoryBOS = cacheUtil.getResult(cacheKey,
                SubjectCategoryBO.class, (key) -> getSubjectCategoryBOS(id));
        return subjectCategoryBOS;
    }

    /**
     * 本地缓存查询失败后调用该方法多线程并发查询
     */
    private List<SubjectCategoryBO> getSubjectCategoryBOS(Long categoryId) {
        SubjectCategory subjectCategory = new SubjectCategory();
        subjectCategory.setParentId(categoryId);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectCategory> subjectCategoryList = subjectCategoryService.queryCategory(subjectCategory);
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryController.queryCategoryAndLabel.subjectCategoryList:{}",
                    JSON.toJSONString(subjectCategoryList));
        }
        List<SubjectCategoryBO> categoryBOList = SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryList);
        // 分类ID ：标签List
        Map<Long, List<SubjectLabelBO>> map = new HashMap<>();
        // 利用 CompletableFuture对每个标签进行并发查询
        List<CompletableFuture<Map<Long, List<SubjectLabelBO>>>> completableFutureList = categoryBOList.stream().map(category ->
                CompletableFuture.supplyAsync(() -> getLabelBOList(category), labelThreadPool)
        ).collect(Collectors.toList());
        // 等待所有 CompletableFuture 完成后汇总查询结果
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[0])).thenRun(() -> {
            // 拿到每个 CompletableFuture 的查询结果，并合并到 map 中。
            completableFutureList.forEach(future -> {
                try {
                    Map<Long, List<SubjectLabelBO>> resultMap = future.get();
                    if (!MapUtils.isEmpty(resultMap)) {
                        map.putAll(resultMap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).join(); // 阻塞等待所有任务完成
        // 将 map 中的 查询结果组装到 categoryBOList 中
        categoryBOList.forEach(categoryBO -> {
            if (!CollectionUtils.isEmpty(map.get(categoryBO.getId()))) {
                categoryBO.setLabelBOList(map.get(categoryBO.getId()));
            }
        });
        return categoryBOList;
    }
    
    private Map<Long, List<SubjectLabelBO>> getLabelBOList(SubjectCategoryBO category) {
        if (log.isInfoEnabled()) {
            log.info("getLabelBOList:{}", JSON.toJSONString(category));
        }
        Map<Long, List<SubjectLabelBO>> labelMap = new HashMap<>();
        SubjectMapping subjectMapping = new SubjectMapping();
        subjectMapping.setCategoryId(category.getId());
        List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);
        if (CollectionUtils.isEmpty(mappingList)) {
            return null;
        }
        List<Long> labelIdList = mappingList.stream().map(SubjectMapping::getLabelId).collect(Collectors.toList());
        List<SubjectLabel> labelList = subjectLabelService.batchQueryById(labelIdList);
        List<SubjectLabelBO> labelBOList = new ArrayList<>();
        labelList.forEach(label -> {
            SubjectLabelBO subjectLabelBO = new SubjectLabelBO();
            subjectLabelBO.setId(label.getId());
            subjectLabelBO.setLabelName(label.getLabelName());
            subjectLabelBO.setCategoryId(label.getCategoryId());
            subjectLabelBO.setSortNum(label.getSortNum());
            labelBOList.add(subjectLabelBO);
        });
        labelMap.put(category.getId(), labelBOList);
        return labelMap;
    }

}
