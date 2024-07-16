package com.zhukew.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhukew.subject.common.entity.PageResult;
import com.zhukew.subject.common.enums.IsDeletedFlagEnum;
import com.zhukew.subject.common.util.IdWorkerUtil;
import com.zhukew.subject.common.util.LoginUtil;
import com.zhukew.subject.domain.convert.SubjectInfoConverter;
import com.zhukew.subject.domain.entity.SubjectInfoBO;
import com.zhukew.subject.domain.entity.SubjectOptionBO;
import com.zhukew.subject.domain.handler.subject.SubjectTypeHandler;
import com.zhukew.subject.domain.handler.subject.SubjectTypeHandlerFactory;
import com.zhukew.subject.domain.redis.RedisUtil;
import com.zhukew.subject.domain.service.SubjectInfoDomainService;
import com.zhukew.subject.domain.service.SubjectLikedDomainService;
import com.zhukew.subject.infra.basic.entity.*;
import com.zhukew.subject.infra.basic.service.*;
import com.zhukew.subject.infra.entity.UserInfo;
import com.zhukew.subject.infra.rpc.UserRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目详情服务实现(domain层)
 *
 * @author: Wei
 */
@Service
@Slf4j
public class SubjectInfoDomainServiceImpl implements SubjectInfoDomainService {

    @Resource
    private SubjectInfoService subjectInfoService;

    @Resource
    private SubjectMappingService subjectMappingService;

    @Resource
    private SubjectLabelService subjectLabelService;

    @Resource
    private SubjectTypeHandlerFactory subjectTypeHandlerFactory;

    @Resource
    private SubjectEsService subjectEsService;

    @Resource
    private SubjectLikedDomainService subjectLikedDomainService;

    @Resource
    private UserRpc userRpc;

    @Resource
    private RedisUtil redisUtil;

    private static final String RANK_KEY = "subject_rank";

    /**
     * 添加题目
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SubjectInfoBO subjectInfoBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectInfoDomainServiceImpl.add.bo:{}", JSON.toJSONString(subjectInfoBO));
        }
        SubjectInfo subjectInfo = SubjectInfoConverter.INSTANCE.convertBoToInfo(subjectInfoBO);
        subjectInfo.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectInfoService.insert(subjectInfo);

        // 工厂 + 策略模式，根据题目类型由工厂提供不同的 Handler 进行题目插入
        SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(subjectInfo.getSubjectType());
        subjectInfoBO.setId(subjectInfo.getId());
        handler.add(subjectInfoBO);
        List<Integer> categoryIds = subjectInfoBO.getCategoryIds();
        List<Integer> labelIds = subjectInfoBO.getLabelIds();
        List<SubjectMapping> mappingList = new ArrayList<>();

        // 插入题目标签映射（SubjectMapping）表
        categoryIds.forEach(categoryId -> {
            labelIds.forEach(labelId -> {
                SubjectMapping subjectMapping = new SubjectMapping();
                subjectMapping.setSubjectId(subjectInfo.getId());
                subjectMapping.setCategoryId(Long.valueOf(categoryId));
                subjectMapping.setLabelId(Long.valueOf(labelId));
                subjectMapping.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
                mappingList.add(subjectMapping);
            });
        });
        subjectMappingService.batchInsert(mappingList);

        //将题目同步到es中
        SubjectInfoEs subjectInfoEs = new SubjectInfoEs();
        subjectInfoEs.setDocId(new IdWorkerUtil(1, 1, 1).nextId());
        subjectInfoEs.setSubjectId(subjectInfo.getId());
        subjectInfoEs.setSubjectAnswer(subjectInfoBO.getSubjectAnswer());
        subjectInfoEs.setCreateTime(new Date().getTime());
        subjectInfoEs.setCreateUser("猪客");
        subjectInfoEs.setSubjectName(subjectInfo.getSubjectName());
        subjectInfoEs.setSubjectType(subjectInfo.getSubjectType());
        subjectEsService.insert(subjectInfoEs);

        //存入redis中，用于计算贡献排行榜
        redisUtil.addScore(RANK_KEY, LoginUtil.getLoginId(), 1);
    }

    /**
     * 分页查询题目
     */
    @Override
    public PageResult<SubjectInfoBO> getSubjectPage(SubjectInfoBO subjectInfoBO) {
        // 封装分页条件
        PageResult<SubjectInfoBO> pageResult = new PageResult<>();
        pageResult.setPageNo(subjectInfoBO.getPageNo());
        pageResult.setPageSize(subjectInfoBO.getPageSize());
        int start = (subjectInfoBO.getPageNo() - 1) * subjectInfoBO.getPageSize();
        SubjectInfo subjectInfo = SubjectInfoConverter.INSTANCE.convertBoToInfo(subjectInfoBO);
        // 查询满足条件的题目数量
        int count = subjectInfoService.countByCondition(subjectInfo, subjectInfoBO.getCategoryId()
                , subjectInfoBO.getLabelId());
        if (count == 0) {
            return pageResult;
        }

        // 根据分页条件和分类标签条件查询
        List<SubjectInfo> subjectInfoList = subjectInfoService.queryPage(subjectInfo, subjectInfoBO.getCategoryId()
                , subjectInfoBO.getLabelId(), start, subjectInfoBO.getPageSize());
        List<SubjectInfoBO> subjectInfoBOS = SubjectInfoConverter.INSTANCE.convertListInfoToBO(subjectInfoList);

        // 为查询到的题目封装标签信息
        subjectInfoBOS.forEach(info -> {
            SubjectMapping subjectMapping = new SubjectMapping();
            subjectMapping.setSubjectId(info.getId());
            List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);
            List<Long> labelIds = mappingList.stream().map(SubjectMapping::getLabelId).collect(Collectors.toList());
            List<SubjectLabel> labelList = subjectLabelService.batchQueryById(labelIds);
            List<String> labelNames = labelList.stream().map(SubjectLabel::getLabelName).collect(Collectors.toList());
            info.setLabelName(labelNames);
        });
        pageResult.setRecords(subjectInfoBOS);
        pageResult.setTotal(count);
        return pageResult;
    }

    /**
     * 查询题目详情
     */
    @Override
    public SubjectInfoBO querySubjectInfo(SubjectInfoBO subjectInfoBO) {
        SubjectInfo subjectInfo = subjectInfoService.queryById(subjectInfoBO.getId());

        // 工厂模式 + 策略模式 查询不同类型题目的信息
        SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(subjectInfo.getSubjectType());
        SubjectOptionBO optionBO = handler.query(subjectInfo.getId().intValue());
        SubjectInfoBO bo = SubjectInfoConverter.INSTANCE.convertOptionAndInfoToBo(optionBO, subjectInfo);

        SubjectMapping subjectMapping = new SubjectMapping();
        subjectMapping.setSubjectId(subjectInfo.getId());
        subjectMapping.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());

        // 查询题目包含的标签id
        List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);
        List<Long> labelIdList = mappingList.stream().map(SubjectMapping::getLabelId).collect(Collectors.toList());

        // 查询具体的标签信息
        List<SubjectLabel> labelList = subjectLabelService.batchQueryById(labelIdList);
        List<String> labelNameList = labelList.stream().map(SubjectLabel::getLabelName).collect(Collectors.toList());

        bo.setLabelName(labelNameList);

        // 该题目是否被当前用户点赞（把小心心标红）
        bo.setLiked(subjectLikedDomainService.isLiked(subjectInfoBO.getId().toString(), LoginUtil.getLoginId()));
        // 该题目的点赞数量
        bo.setLikedCount(subjectLikedDomainService.getLikedCount(subjectInfoBO.getId().toString()));

        // 封装两侧题目id
        assembleSubjectCursor(subjectInfoBO, bo);
        return bo;
    }

    /**
     * 封装题目游标id：用于快速刷题（上一题，下一题）
     */
    private void assembleSubjectCursor(SubjectInfoBO subjectInfoBO, SubjectInfoBO bo) {
        // 查询封装好的 bo 中包含题目的所有信息，
        // 并不知道当前用户查看的是哪个分类和哪个标签，
        // 因此需要前端传递的参数。
        Long categoryId = subjectInfoBO.getCategoryId();
        Long labelId = subjectInfoBO.getLabelId();
        Long subjectId = subjectInfoBO.getId();
        if (Objects.isNull(categoryId) || Objects.isNull(labelId)) {
            return;
        }
        Long nextSubjectId = subjectInfoService.querySubjectIdCursor(subjectId, categoryId, labelId, 1);
        bo.setNextSubjectId(nextSubjectId);
        Long lastSubjectId = subjectInfoService.querySubjectIdCursor(subjectId, categoryId, labelId, 0);
        bo.setLastSubjectId(lastSubjectId);
    }

    /**
     * ES分页检索
     */
    @Override
    public PageResult<SubjectInfoEs> getSubjectPageBySearch(SubjectInfoBO subjectInfoBO) {
        SubjectInfoEs subjectInfoEs = new SubjectInfoEs();
        subjectInfoEs.setPageNo(subjectInfoBO.getPageNo());
        subjectInfoEs.setPageSize(subjectInfoBO.getPageSize());
        subjectInfoEs.setKeyWord(subjectInfoBO.getKeyWord());
        return subjectEsService.querySubjectList(subjectInfoEs);
    }

    /**
     * 获取贡献排行榜
     */
    @Override
    public List<SubjectInfoBO> getContributeList() {
        // 从 ZSet 中读取题目贡献前五名
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisUtil.rankWithScore(RANK_KEY, 0, 5);
        if (log.isInfoEnabled()) {
            log.info("getContributeList.typedTuples:{}", JSON.toJSONString(typedTuples));
        }
        if (CollectionUtils.isEmpty(typedTuples)) {
            return Collections.emptyList();
        }
        List<SubjectInfoBO> boList = new ArrayList<>();
        // 根据用户 loginId 查询用户信息并封装
        typedTuples.forEach((rank -> {
            SubjectInfoBO subjectInfoBO = new SubjectInfoBO();
            subjectInfoBO.setSubjectCount(rank.getScore().intValue());
            UserInfo userInfo = userRpc.getUserInfo(rank.getValue());
            subjectInfoBO.setCreateUser(userInfo.getNickName());
            subjectInfoBO.setCreateUserAvatar(userInfo.getAvatar());
            boList.add(subjectInfoBO);
        }));
        return boList;
    }

}
