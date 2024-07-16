package com.zhukew.practice.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhukew.practice.api.common.PageInfo;
import com.zhukew.practice.api.common.PageResult;
import com.zhukew.practice.api.enums.CompleteStatusEnum;
import com.zhukew.practice.api.enums.IsDeletedFlagEnum;
import com.zhukew.practice.api.enums.SubjectInfoTypeEnum;
import com.zhukew.practice.api.req.GetPracticeSubjectsReq;
import com.zhukew.practice.api.req.GetUnCompletePracticeReq;
import com.zhukew.practice.server.dao.*;
import com.zhukew.practice.server.entity.dto.CategoryDTO;
import com.zhukew.practice.server.entity.dto.PracticeSetDTO;
import com.zhukew.practice.server.entity.dto.PracticeSubjectDTO;
import com.zhukew.practice.server.entity.po.*;
import com.zhukew.practice.server.service.PracticeSetService;
import com.zhukew.practice.server.util.DateUtils;
import com.zhukew.practice.server.util.LoginUtil;
import com.zhukew.practice.api.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 练习套卷service实现
 *
 * @author: Wei
 */
@Service
@Slf4j
public class PracticeSetServiceImpl implements PracticeSetService {

    @Resource
    private SubjectCategoryDao subjectCategoryDao;

    @Resource
    private SubjectMappingDao subjectMappingDao;

    @Resource
    private SubjectLabelDao subjectLabelDao;

    @Resource
    private PracticeSetDetailDao practiceSetDetailDao;

    @Resource
    private PracticeSetDao practiceSetDao;

    @Resource
    private SubjectDao subjectDao;

    @Resource
    private PracticeDetailDao practiceDetailDao;

    @Resource
    private PracticeDao practiceDao;

    @Resource
    private SubjectRadioDao subjectRadioDao;

    @Resource
    private SubjectMultipleDao subjectMultipleDao;

    /**
     * 获取专项练习内容
     * 该接口向用户展示可以选择哪些专项练习
     * 通过查询大类，小类，标签下有多少题目，将可以选择的专项练习（分类-标签）返回给前端
     */
    @Override
    public List<SpecialPracticeVO> getSpecialPracticeContent() {
        // 查询大类 -> 查询小类 -> 查询标签

        // 返回：specialPracticeVOList
        List<SpecialPracticeVO> specialPracticeVOList = new ArrayList<>();

        List<Integer> subjectTypeList = new ArrayList<>();
        // 过滤：练题只练单选，多选，判断
        subjectTypeList.add(SubjectInfoTypeEnum.RADIO.getCode());
        subjectTypeList.add(SubjectInfoTypeEnum.MULTIPLE.getCode());
        subjectTypeList.add(SubjectInfoTypeEnum.JUDGE.getCode());
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setSubjectTypeList(subjectTypeList);
        // 查询出满足条件的大类，包含：大类id，大类包含题目数量
        List<PrimaryCategoryPO> poList = subjectCategoryDao.getPrimaryCategory(categoryDTO);

        if (CollectionUtils.isEmpty(poList)) {
            // 没有满足条件的大类，直接返回
            return specialPracticeVOList;
        }

        // 对查询到的大类进行组装
        poList.forEach(primaryCategoryPO -> {
            SpecialPracticeVO specialPracticeVO = new SpecialPracticeVO();
            specialPracticeVO.setPrimaryCategoryId(primaryCategoryPO.getParentId());
            // 根据id查询出大类信息
            CategoryPO categoryPO = subjectCategoryDao.selectById(primaryCategoryPO.getParentId());
            specialPracticeVO.setPrimaryCategoryName(categoryPO.getCategoryName());
            // 查询大类下小类
            CategoryDTO categoryDTOTemp = new CategoryDTO();
            categoryDTOTemp.setCategoryType(2);
            categoryDTOTemp.setParentId(primaryCategoryPO.getParentId());
            List<CategoryPO> smallPoList = subjectCategoryDao.selectList(categoryDTOTemp);
            if (CollectionUtils.isEmpty(smallPoList)) {
                // 没有满足条件的小类，不对该大类进行封装
                return;
            }
            // 小类list：对每个小类封装标签后存入
            List<SpecialPracticeCategoryVO> categoryList = new ArrayList();
            smallPoList.forEach(smallPo -> {
                // 查询小类下标签
                List<SpecialPracticeLabelVO> labelVOList = getLabelVOList(smallPo.getId(), subjectTypeList);
                if (CollectionUtils.isEmpty(labelVOList)) {
                    // 没有满足条件的标签，直接返回
                    return;
                }
                // 封装小类
                SpecialPracticeCategoryVO specialPracticeCategoryVO = new SpecialPracticeCategoryVO();
                specialPracticeCategoryVO.setCategoryId(smallPo.getId());
                specialPracticeCategoryVO.setCategoryName(smallPo.getCategoryName());
                List<SpecialPracticeLabelVO> labelList = new ArrayList<>();
                // 为小类封装标签list
                labelVOList.forEach(labelVo -> {
                    SpecialPracticeLabelVO specialPracticeLabelVO = new SpecialPracticeLabelVO();
                    specialPracticeLabelVO.setId(labelVo.getId());
                    // 封装唯一id：分类id + 标签id
                    specialPracticeLabelVO.setAssembleId(labelVo.getAssembleId());
                    specialPracticeLabelVO.setLabelName(labelVo.getLabelName());
                    labelList.add(specialPracticeLabelVO);
                });
                specialPracticeCategoryVO.setLabelList(labelList);
                categoryList.add(specialPracticeCategoryVO);
            });
            specialPracticeVO.setCategoryList(categoryList);
            specialPracticeVOList.add(specialPracticeVO);
        });
        return specialPracticeVOList;
    }

    /**
     * 开始练习
     * 用户在选择一个或多个 分类-标签 后点击开始练习
     * 此接口查询 分类-标签s 下的题目并返回动态配置的数量的不同类型的题目
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeSetVO addPractice(PracticeSubjectDTO dto) {
        PracticeSetVO setVO = new PracticeSetVO();
        // 获取套卷题目，包含题目id和题目类型
        List<PracticeSubjectDetailVO> practiceList = getPracticeList(dto);
        if (CollectionUtils.isEmpty(practiceList)) {
            // 没查询到题目直接返回空套卷
            return setVO;
        }
        // 对套卷信息进行封装
        PracticeSetPO practiceSetPO = new PracticeSetPO();
        practiceSetPO.setSetType(1);
        List<String> assembleIds = dto.getAssembleIds();
        Set<Long> categoryIdSet = new HashSet<>();
        assembleIds.forEach(assembleId -> {
            Long categoryId = Long.valueOf(assembleId.split("-")[0]);
            categoryIdSet.add(categoryId);
        });

        // 处理套卷名称信息，用分类名称填充
        StringBuffer setName = new StringBuffer();
        int i = 1;
        for (Long categoryId : categoryIdSet) {
            // 套卷名中最多展示两个分类
            if (i > 2) {
                break;
            }
            CategoryPO categoryPO = subjectCategoryDao.selectById(categoryId);
            setName.append(categoryPO.getCategoryName());
            setName.append("、");
            i = i + 1;
        }
        setName.deleteCharAt(setName.length() - 1);
        if (i == 2) {
            setName.append("专项练习");
        } else {
            setName.append("等专项练习");
        }

        // 存储该套卷到 套题信息表
        practiceSetPO.setSetName(setName.toString());
        // 套卷的题目都在一个大类下，直接通过一个标签查找
        String labelId = assembleIds.get(0).split("-")[1];
        SubjectLabelPO labelPO = subjectLabelDao.queryById(Long.valueOf(labelId));
        practiceSetPO.setPrimaryCategoryId(labelPO.getCategoryId());
        practiceSetPO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        // 将套卷的创建者封装为用户的openId
        practiceSetPO.setCreatedBy(LoginUtil.getLoginId());
        practiceSetPO.setCreatedTime(new Date());
        practiceSetDao.add(practiceSetPO);
        Long practiceSetId = practiceSetPO.getId();

        // 存储套卷题目到 套题详情表
        practiceList.forEach(e -> {
            PracticeSetDetailPO detailPO = new PracticeSetDetailPO();
            detailPO.setSetId(practiceSetId);
            detailPO.setSubjectId(e.getSubjectId());
            detailPO.setSubjectType(e.getSubjectType());
            detailPO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
            detailPO.setCreatedBy(LoginUtil.getLoginId());
            detailPO.setCreatedTime(new Date());
            practiceSetDetailDao.add(detailPO);
        });
        setVO.setSetId(practiceSetId);
        return setVO;
    }

    /**
     * 获取套卷题目信息
     * 根据用户选择的 分类-标签s 选择题目返回
     */
    private List<PracticeSubjectDetailVO> getPracticeList(PracticeSubjectDTO dto) {
        List<PracticeSubjectDetailVO> practiceSubjectListVOS = new ArrayList<>();
        //避免重复：排除一些题目id
        List<Long> excludeSubjectIds = new ArrayList<>();

        //获取 nacos 动态配置的题目数量
        Integer radioSubjectCount = 10;
        Integer multipleSubjectCount = 6;
        Integer judgeSubjectCount = 4;
        Integer totalSubjectCount = 20;
        // 查询单选
        dto.setSubjectCount(radioSubjectCount);
        dto.setSubjectType(SubjectInfoTypeEnum.RADIO.getCode());
        assembleList(dto, practiceSubjectListVOS, excludeSubjectIds);
        // 查询多选
        dto.setSubjectCount(multipleSubjectCount);
        dto.setSubjectType(SubjectInfoTypeEnum.MULTIPLE.getCode());
        assembleList(dto, practiceSubjectListVOS, excludeSubjectIds);
        // 查询判断
        dto.setSubjectCount(judgeSubjectCount);
        dto.setSubjectType(SubjectInfoTypeEnum.JUDGE.getCode());
        assembleList(dto, practiceSubjectListVOS, excludeSubjectIds);
        // 补充题目
        if (practiceSubjectListVOS.size() == totalSubjectCount) {
            return practiceSubjectListVOS;
        }
        // 还差多少题目
        Integer remainCount = totalSubjectCount - practiceSubjectListVOS.size();
        dto.setSubjectCount(remainCount);
        // 用单选题目补足
        dto.setSubjectType(1);
        assembleList(dto, practiceSubjectListVOS, excludeSubjectIds);
        return practiceSubjectListVOS;
    }

    /**
     * 封装指定类型的题目
     */
    private List<PracticeSubjectDetailVO> assembleList(PracticeSubjectDTO dto, List<PracticeSubjectDetailVO> list, List<Long> excludeSubjectIds) {
        dto.setExcludeSubjectIds(excludeSubjectIds);
        // 查询要求的类型和数量的题目
        List<SubjectPO> subjectPOList = subjectDao.getPracticeSubject(dto);
        if (CollectionUtils.isEmpty(subjectPOList)) {
            return list;
        }
        // 将查询到的题目封装到指定 list 中
        subjectPOList.forEach(e -> {
            PracticeSubjectDetailVO vo = new PracticeSubjectDetailVO();
            vo.setSubjectId(e.getId());
            vo.setSubjectType(e.getSubjectType());
            excludeSubjectIds.add(e.getId());
            list.add(vo);
        });
        return list;
    }

    /**
     * 获取小类下标签list
     */
    private List<SpecialPracticeLabelVO> getLabelVOList(Long categoryId, List<Integer> subjectTypeList) {
        // 查询标签id及题目数量
        List<LabelCountPO> countPOList = subjectMappingDao.getLabelSubjectCount(categoryId, subjectTypeList);
        if (CollectionUtils.isEmpty(countPOList)) {
            return Collections.emptyList();
        }
        List<SpecialPracticeLabelVO> voList = new ArrayList<>();
        // 查询并封装标签信息
        countPOList.forEach(countPo -> {
            SpecialPracticeLabelVO vo = new SpecialPracticeLabelVO();
            vo.setId(countPo.getLabelId());
            // 组装唯一id：分类id + 标签id
            vo.setAssembleId(categoryId + "-" + countPo.getLabelId());
            SubjectLabelPO subjectLabelPO = subjectLabelDao.queryById(countPo.getLabelId());
            vo.setLabelName(subjectLabelPO.getLabelName());
            voList.add(vo);
        });
        return voList;
    }

    /**
     * 获取练习题
     * 用户交卷或提前交卷，统计所有练习题
     */
    @Override
    public PracticeSubjectListVO getSubjects(GetPracticeSubjectsReq req) {
        Long setId = req.getSetId();
        PracticeSubjectListVO vo = new PracticeSubjectListVO();
        List<PracticeSubjectDetailVO> practiceSubjectListVOS = new ArrayList<>();
        // 查询套卷中所有信息，共有list.size道题
        List<PracticeSetDetailPO> practiceSetDetailPOS = practiceSetDetailDao.selectBySetId(setId);
        if (CollectionUtils.isEmpty(practiceSetDetailPOS)) {
            return vo;
        }
        String loginId = LoginUtil.getLoginId();
        Long practiceId = req.getPracticeId();
        // 检查该用户完成了哪些题目并标识出来
        practiceSetDetailPOS.forEach(e -> {
            PracticeSubjectDetailVO practiceSubjectListVO = new PracticeSubjectDetailVO();
            practiceSubjectListVO.setSubjectId(e.getSubjectId());
            practiceSubjectListVO.setSubjectType(e.getSubjectType());
            // 判断该用户已经回答了该题，对返回的VO进行封装
            if (Objects.nonNull(practiceId)) {
                PracticeDetailPO practiceDetailPO = practiceDetailDao.selectDetail(practiceId, e.getSubjectId(), loginId);
                if (Objects.nonNull(practiceDetailPO) && StringUtils.isNotBlank(practiceDetailPO.getAnswerContent())) {
                    // 如果练习详情表中找不到说明没回答该题，反之说明回答了
                    practiceSubjectListVO.setIsAnswer(1);
                } else {
                    practiceSubjectListVO.setIsAnswer(0);
                }
            }
            practiceSubjectListVOS.add(practiceSubjectListVO);
        });
        vo.setSubjectList(practiceSubjectListVOS);
        PracticeSetPO practiceSetPO = practiceSetDao.selectById(setId);
        vo.setTitle(practiceSetPO.getSetName());
        if (Objects.isNull(practiceId)) {
            // 用户未完成所有题目，将本次练习状态记为未完成
            Long newPracticeId = insertUnCompletePractice(setId);
            vo.setPracticeId(newPracticeId);
        } else {
            // 用户完成套卷后交卷，将本次练习状态改为已完成
            updateUnCompletePractice(practiceId);
            PracticePO practicePO = practiceDao.selectById(practiceId);
            vo.setTimeUse(practicePO.getTimeUse());
            vo.setPracticeId(practiceId);
        }
        return vo;
    }

    /**
     * 记录用户未完成的练习
     */
    private Long insertUnCompletePractice(Long practiceSetId) {
        PracticePO practicePO = new PracticePO();
        practicePO.setSetId(practiceSetId);
        practicePO.setCompleteStatus(CompleteStatusEnum.NO_COMPLETE.getCode());
        practicePO.setTimeUse("00:00:00");
        practicePO.setSubmitTime(new Date());
        practicePO.setCorrectRate(new BigDecimal("0.00"));
        practicePO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        practicePO.setCreatedBy(LoginUtil.getLoginId());
        practicePO.setCreatedTime(new Date());
        practiceDao.insert(practicePO);
        return practicePO.getId();
    }

    /**
     * 用户再次练习，更新用户的练习情况
     */
    private void updateUnCompletePractice(Long practiceId) {
        PracticePO practicePO = new PracticePO();
        practicePO.setId(practiceId);
        practicePO.setSubmitTime(new Date());
        practiceDao.update(practicePO);
    }

    /**
     * 获取练习题目详细内容
     */
    @Override
    public PracticeSubjectVO getPracticeSubject(PracticeSubjectDTO dto) {
        PracticeSubjectVO practiceSubjectVO = new PracticeSubjectVO();
        SubjectPO subjectPO = subjectDao.selectById(dto.getSubjectId());
        practiceSubjectVO.setSubjectName(subjectPO.getSubjectName());
        practiceSubjectVO.setSubjectType(subjectPO.getSubjectType());
        // 可以改成工厂+策略
        if (dto.getSubjectType() == SubjectInfoTypeEnum.RADIO.getCode()) {
            // 封装单选题
            List<PracticeSubjectOptionVO> optionList = new ArrayList<>();
            List<SubjectRadioPO> radioSubjectPOS = subjectRadioDao.selectBySubjectId(subjectPO.getId());
            radioSubjectPOS.forEach(e -> {
                PracticeSubjectOptionVO practiceSubjectOptionVO = new PracticeSubjectOptionVO();
                practiceSubjectOptionVO.setOptionContent(e.getOptionContent());
                practiceSubjectOptionVO.setOptionType(e.getOptionType());
                optionList.add(practiceSubjectOptionVO);
            });
            practiceSubjectVO.setOptionList(optionList);
        }
        if (dto.getSubjectType() == SubjectInfoTypeEnum.MULTIPLE.getCode()) {
            // 封装多选题
            List<PracticeSubjectOptionVO> optionList = new ArrayList<>();
            List<SubjectMultiplePO> multipleSubjectPOS = subjectMultipleDao.selectBySubjectId(subjectPO.getId());
            multipleSubjectPOS.forEach(e -> {
                PracticeSubjectOptionVO practiceSubjectOptionVO = new PracticeSubjectOptionVO();
                practiceSubjectOptionVO.setOptionContent(e.getOptionContent());
                practiceSubjectOptionVO.setOptionType(e.getOptionType());
                optionList.add(practiceSubjectOptionVO);
            });
            practiceSubjectVO.setOptionList(optionList);
        }
        return practiceSubjectVO;
    }

    /**
     * 获取模拟套题（setType = 2）列表
     */
    @Override
    public PageResult<PracticeSetVO> getPreSetContent(PracticeSetDTO dto) {
        // 分页获取
        PageResult<PracticeSetVO> pageResult = new PageResult<>();
        PageInfo pageInfo = dto.getPageInfo();
        pageResult.setPageNo(pageInfo.getPageNo());
        pageResult.setPageSize(pageInfo.getPageSize());
        int start = (pageInfo.getPageNo() - 1) * pageInfo.getPageSize();

        Integer count = practiceSetDao.getListCount(dto);
        if (count == 0) {
            return pageResult;
        }
        // 在套卷信息表中查询
        List<PracticeSetPO> setPOList = practiceSetDao.getSetList(dto, start, dto.getPageInfo().getPageSize());
        if (log.isInfoEnabled()) {
            log.info("获取的模拟考卷列表{}", JSON.toJSONString(setPOList));
        }
        List<PracticeSetVO> list = new ArrayList<>();
        // 封装返回结果
        setPOList.forEach(e -> {
            PracticeSetVO vo = new PracticeSetVO();
            vo.setSetId(e.getId());
            vo.setSetName(e.getSetName());
            vo.setSetHeat(e.getSetHeat());
            vo.setSetDesc(e.getSetDesc());
            list.add(vo);
        });
        pageResult.setRecords(list);
        pageResult.setTotal(count);
        return pageResult;
    }

    /**
     * 获取未完成套卷列表
     */
    @Override
    public PageResult<UnCompletePracticeSetVO> getUnCompletePractice(GetUnCompletePracticeReq req) {
        // 分页查询未完成套卷列表，每次查询一页
        PageResult<UnCompletePracticeSetVO> pageResult = new PageResult<>();
        PageInfo pageInfo = req.getPageInfo();
        pageResult.setPageNo(pageInfo.getPageNo());
        pageResult.setPageSize(pageInfo.getPageSize());
        int start = (pageInfo.getPageNo() - 1) * pageInfo.getPageSize();
        String loginId = LoginUtil.getLoginId();
        // 在练习信息表中查询未完成套卷数量和详情
        Integer count = practiceDao.getUnCompleteCount(loginId);
        if (count == 0) {
            return pageResult;
        }
        List<PracticePO> poList = practiceDao.getUnCompleteList(loginId, start, req.getPageInfo().getPageSize());
        if (log.isInfoEnabled()) {
            log.info("获取未完成的考卷列表{}", JSON.toJSONString(poList));
        }
        List<UnCompletePracticeSetVO> list = new ArrayList<>();
        // 封装返回结果
        poList.forEach(e -> {
            UnCompletePracticeSetVO vo = new UnCompletePracticeSetVO();
            vo.setSetId(e.getSetId());
            vo.setPracticeId(e.getId());
            vo.setPracticeTime(DateUtils.format(e.getSubmitTime(), "yyyy-MM-dd"));
            PracticeSetPO practiceSetPO = practiceSetDao.selectById(e.getSetId());
            vo.setTitle(practiceSetPO.getSetName());
            list.add(vo);
        });
        pageResult.setRecords(list);
        pageResult.setTotal(count);
        return pageResult;
    }

}
