package com.zhukew.practice.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.zhukew.practice.server.dao.*;
import com.zhukew.practice.server.entity.po.*;
import com.zhukew.practice.server.entity.dto.SubjectDTO;
import com.zhukew.practice.server.entity.dto.SubjectDetailDTO;
import com.zhukew.practice.server.entity.dto.SubjectOptionDTO;
import com.zhukew.practice.server.entity.dto.UserInfo;
import com.zhukew.practice.server.rpc.UserRpc;
import com.zhukew.practice.server.service.PracticeDetailService;
import com.zhukew.practice.server.util.DateUtils;
import com.zhukew.practice.server.util.LoginUtil;
import com.zhukew.practice.api.enums.AnswerStatusEnum;
import com.zhukew.practice.api.enums.CompleteStatusEnum;
import com.zhukew.practice.api.enums.IsDeletedFlagEnum;
import com.zhukew.practice.api.enums.SubjectInfoTypeEnum;
import com.zhukew.practice.api.req.*;
import com.zhukew.practice.api.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 练习详情服务
 *
 * @author: Wei
 */
@Service
@Slf4j
public class PracticeDetailServiceImpl implements PracticeDetailService {

    @Resource
    private PracticeDetailDao practiceDetailDao;

    @Resource
    private PracticeSetDao practiceSetDao;

    @Resource
    private PracticeSetDetailDao practiceSetDetailDao;

    @Resource
    private PracticeDao practiceDao;

    @Resource
    private SubjectDao subjectDao;

    @Resource
    private SubjectRadioDao subjectRadioDao;

    @Resource
    private SubjectMultipleDao subjectMultipleDao;

    @Resource
    private SubjectJudgeDao subjectJudgeDao;

    @Resource
    private SubjectMappingDao subjectMappingDao;

    @Resource
    private SubjectLabelDao subjectLabelDao;

    @Resource
    private UserRpc userRpc;

    /**
     * 交卷
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Boolean submit(SubmitPracticeDetailReq req) {
        PracticePO practicePO = new PracticePO();
        Long practiceId = req.getPracticeId();
        Long setId = req.getSetId();
        practicePO.setSetId(setId);
        String timeUse = req.getTimeUse();
        String hour = timeUse.substring(0, 2);
        String minute = timeUse.substring(2, 4);
        String second = timeUse.substring(4, 6);
        // 封装交卷信息
        practicePO.setTimeUse(hour + ":" + minute + ":" + second);
        practicePO.setSubmitTime(DateUtils.parseStrToDate(req.getSubmitTime()));
        practicePO.setCompleteStatus(CompleteStatusEnum.COMPLETE.getCode());
        practicePO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        practicePO.setCreatedBy(LoginUtil.getLoginId());
        practicePO.setCreatedTime(new Date());
        //计算答题正确率
        Integer correctCount = practiceDetailDao.selectCorrectCount(practiceId);
        List<PracticeSetDetailPO> practiceSetDetailPOS = practiceSetDetailDao.selectBySetId(setId);
        Integer totalCount = practiceSetDetailPOS.size();
        BigDecimal correctRate = new BigDecimal(correctCount).divide(new BigDecimal(totalCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100.00"));
        practicePO.setCorrectRate(correctRate);
        PracticePO po = practiceDao.selectById(practiceId);
        // 记录练习情况
        if (Objects.isNull(po)) {
            practiceDao.insert(practicePO);
        } else {
            practicePO.setId(practiceId);
            practiceDao.update(practicePO);
        }
        practiceSetDao.updateHeat(setId);
        // 补充剩余题目的记录，未答的题目
        List<PracticeDetailPO> practiceDetailPOList = practiceDetailDao.selectByPracticeId(practiceId);
        List<PracticeSetDetailPO> minusList = practiceSetDetailPOS.stream()
                .filter(item -> !practiceDetailPOList.stream()
                        .map(e -> e.getSubjectId())
                        .collect(Collectors.toList())
                        .contains(item.getSubjectId()))
                .collect(Collectors.toList());
        if (log.isInfoEnabled()) {
            log.info("题目差集{}", JSON.toJSONString(minusList));
        }
        if (CollectionUtils.isNotEmpty(minusList)) {
            minusList.forEach(e -> {
                PracticeDetailPO practiceDetailPO = new PracticeDetailPO();
                practiceDetailPO.setPracticeId(practiceId);
                practiceDetailPO.setSubjectType(e.getSubjectType());
                practiceDetailPO.setSubjectId(e.getSubjectId());
                practiceDetailPO.setAnswerStatus(0);
                practiceDetailPO.setAnswerContent("");
                practiceDetailPO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
                practiceDetailPO.setCreatedTime(new Date());
                practiceDetailPO.setCreatedBy(LoginUtil.getLoginId());
                practiceDetailDao.insertSingle(practiceDetailPO);
            });
        }
        return true;
    }

    /**
     * 提交题目
     * 用户每修改一道题目就调用该接口
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitSubject(SubmitSubjectDetailReq req) {
        // 封装用时
        String timeUse = req.getTimeUse();
        if (timeUse.equals("0")) {
            timeUse = "000000";
        }
        String hour = timeUse.substring(0, 2);
        String minute = timeUse.substring(2, 4);
        String second = timeUse.substring(4, 6);

        // 更新本次练习情况
        PracticePO practicePO = new PracticePO();
        practicePO.setId(req.getPracticeId());
        practicePO.setTimeUse(hour + ":" + minute + ":" + second);
        practicePO.setSubmitTime(new Date());
        practiceDao.update(practicePO);

        // 更新该题练习情况
        PracticeDetailPO practiceDetailPO = new PracticeDetailPO();
        practiceDetailPO.setPracticeId(req.getPracticeId());
        practiceDetailPO.setSubjectId(req.getSubjectId());
        practiceDetailPO.setSubjectType(req.getSubjectType());
        String answerContent = "";
        //排序用户答案
        if (CollectionUtils.isNotEmpty(req.getAnswerContents())) {
            List<Integer> answerContents = req.getAnswerContents();
            Collections.sort(answerContents);
            answerContent = StringUtils.join(answerContents, ",");
        }
        practiceDetailPO.setAnswerContent(answerContent);
        SubjectDTO subjectDTO = new SubjectDTO();
        subjectDTO.setSubjectId(req.getSubjectId());
        subjectDTO.setSubjectType(req.getSubjectType());
        //获取正确答案，并与用户答案进行比对
        SubjectDetailDTO subjectDetail = getSubjectDetail(subjectDTO);
        StringBuffer correctAnswer = new StringBuffer();
        if (req.getSubjectType().equals(SubjectInfoTypeEnum.JUDGE.getCode())) {
            Integer isCorrect = subjectDetail.getIsCorrect();
            correctAnswer.append(isCorrect);
        } else {
            // 封装单选和多选的正确答案
            subjectDetail.getOptionList().forEach(e -> {
                if (Objects.equals(e.getIsCorrect(), 1)) {
                    correctAnswer.append(e.getOptionType()).append(",");
                }
            });
            if (correctAnswer.length() > 0) {
                correctAnswer.deleteCharAt(correctAnswer.length() - 1);
            }
        }
        // 判断用户答案与正确答案是否一致
        if (Objects.equals(correctAnswer.toString(), answerContent)) {
            practiceDetailPO.setAnswerStatus(1);
        } else {
            practiceDetailPO.setAnswerStatus(0);
        }
        practiceDetailPO.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        practiceDetailPO.setCreatedBy(LoginUtil.getLoginId());
        practiceDetailPO.setCreatedTime(new Date());
        // 更新练习detail表
        PracticeDetailPO existDetail = practiceDetailDao.selectDetail(req.getPracticeId(), req.getSubjectId(), LoginUtil.getLoginId());
        if (Objects.isNull(existDetail)) {
            // 如果练习详情中没有对该题的记录就插入，否则更新
            practiceDetailDao.insertSingle(practiceDetailPO);
        } else {
            practiceDetailPO.setId(existDetail.getId());
            practiceDetailDao.update(practiceDetailPO);
        }
        return true;
    }


    /**
     * 获取题目详细信息
     */
    public SubjectDetailDTO getSubjectDetail(SubjectDTO dto) {
        SubjectDetailDTO subjectDetailDTO = new SubjectDetailDTO();
        SubjectPO subjectPO = subjectDao.selectById(dto.getSubjectId());
        if (dto.getSubjectType() == SubjectInfoTypeEnum.RADIO.getCode()) {
            // 封装单选题
            List<SubjectOptionDTO> optionList = new ArrayList<>();
            List<SubjectRadioPO> radioSubjectPOS = subjectRadioDao.selectBySubjectId(subjectPO.getId());
            radioSubjectPOS.forEach(e -> {
                SubjectOptionDTO subjectOptionDTO = new SubjectOptionDTO();
                subjectOptionDTO.setOptionContent(e.getOptionContent());
                subjectOptionDTO.setOptionType(e.getOptionType());
                subjectOptionDTO.setIsCorrect(e.getIsCorrect());
                optionList.add(subjectOptionDTO);
            });
            subjectDetailDTO.setOptionList(optionList);
        }
        if (dto.getSubjectType() == SubjectInfoTypeEnum.MULTIPLE.getCode()) {
            // 封装多选题
            List<SubjectOptionDTO> optionList = new ArrayList<>();
            List<SubjectMultiplePO> multipleSubjectPOS = subjectMultipleDao.selectBySubjectId(subjectPO.getId());
            multipleSubjectPOS.forEach(e -> {
                SubjectOptionDTO subjectOptionDTO = new SubjectOptionDTO();
                subjectOptionDTO.setOptionContent(e.getOptionContent());
                subjectOptionDTO.setOptionType(e.getOptionType());
                subjectOptionDTO.setIsCorrect(e.getIsCorrect());
                optionList.add(subjectOptionDTO);
            });
            subjectDetailDTO.setOptionList(optionList);
        }
        if (dto.getSubjectType() == SubjectInfoTypeEnum.JUDGE.getCode()) {
            // 封装判断题
            SubjectJudgePO judgeSubjectPO = subjectJudgeDao.selectBySubjectId(subjectPO.getId());
            subjectDetailDTO.setIsCorrect(judgeSubjectPO.getIsCorrect());
        }
        subjectDetailDTO.setSubjectParse(subjectPO.getSubjectParse());
        subjectDetailDTO.setSubjectName(subjectPO.getSubjectName());
        return subjectDetailDTO;
    }

    /**
     * 每题得分/每题对错
     */
    @Override
    public List<ScoreDetailVO> getScoreDetail(GetScoreDetailReq req) {
        Long practiceId = req.getPracticeId();
        List<ScoreDetailVO> list = new ArrayList<>();
        // 查询本次练习的所有题目情况
        List<PracticeDetailPO> practiceDetailPOList = practiceDetailDao.selectByPracticeId(practiceId);
        if (CollectionUtils.isEmpty(practiceDetailPOList)) {
            return Collections.emptyList();
        }
        practiceDetailPOList.forEach(po -> {
            ScoreDetailVO scoreDetailVO = new ScoreDetailVO();
            scoreDetailVO.setSubjectId(po.getSubjectId());
            scoreDetailVO.setSubjectType(po.getSubjectType());
            scoreDetailVO.setIsCorrect(po.getAnswerStatus());
            list.add(scoreDetailVO);
        });
        return list;
    }

    /**
     * 答题详情：获取题目答案解析，自己答案
     */
    @Override
    public SubjectDetailVO getSubjectDetail(GetSubjectDetailReq req) {
        SubjectDetailVO subjectDetailVO = new SubjectDetailVO();
        Long subjectId = req.getSubjectId();
        Integer subjectType = req.getSubjectType();
        SubjectDTO subjectDTO = new SubjectDTO();
        subjectDTO.setSubjectId(subjectId);
        subjectDTO.setSubjectType(subjectType);
        SubjectDetailDTO subjectDetail = getSubjectDetail(subjectDTO);
        List<SubjectOptionDTO> optionList = subjectDetail.getOptionList();
        List<PracticeSubjectOptionVO> optionVOList = new ArrayList<>();
        List<Integer> correctAnswer = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(optionList)) {
            // 封装题目详情
            optionList.forEach(option -> {
                PracticeSubjectOptionVO optionVO = new PracticeSubjectOptionVO();
                optionVO.setOptionType(option.getOptionType());
                optionVO.setOptionContent(option.getOptionContent());
                optionVO.setIsCorrect(option.getIsCorrect());
                optionVOList.add(optionVO);
                if (option.getIsCorrect() == 1) {
                    correctAnswer.add(option.getOptionType());
                }
            });
        }
        // 判断题数据格式特殊，单独处理
        if (subjectType.equals(SubjectInfoTypeEnum.JUDGE.getCode())) {
            Integer isCorrect = subjectDetail.getIsCorrect();
            PracticeSubjectOptionVO correctOption = new PracticeSubjectOptionVO();
            correctOption.setOptionType(1);
            correctOption.setOptionContent("正确");
            correctOption.setIsCorrect(isCorrect == 1 ? 1 : 0);
            PracticeSubjectOptionVO errorOptionVO = new PracticeSubjectOptionVO();
            errorOptionVO.setOptionType(2);
            errorOptionVO.setOptionContent("错误");
            errorOptionVO.setIsCorrect(isCorrect == 0 ? 1 : 0);
            optionVOList.add(correctOption);
            optionVOList.add(errorOptionVO);
            correctAnswer.add(subjectDetail.getIsCorrect());
        }
        subjectDetailVO.setOptionList(optionVOList);
        subjectDetailVO.setSubjectParse(subjectDetail.getSubjectParse());
        subjectDetailVO.setSubjectName(subjectDetail.getSubjectName());
        subjectDetailVO.setCorrectAnswer(correctAnswer);

        //自己的答题答案
        List<Integer> respondAnswer = new ArrayList<>();
        PracticeDetailPO practiceDetailPO = practiceDetailDao.selectAnswer(req.getPracticeId(), subjectId);
        String answerContent = practiceDetailPO.getAnswerContent();
        if (StringUtils.isNotBlank(answerContent)) {
            String[] split = answerContent.split(",");
            for (String s : split) {
                respondAnswer.add(Integer.valueOf(s));
            }
        }
        subjectDetailVO.setRespondAnswer(respondAnswer);
        List<SubjectMappingPO> subjectMappingPOList = subjectMappingDao.getLabelIdsBySubjectId(subjectId);
        List<Long> labelIdList = new ArrayList<>();
        subjectMappingPOList.forEach(subjectMappingPO -> {
            labelIdList.add(subjectMappingPO.getLabelId());
        });
        List<String> labelNameList = subjectLabelDao.getLabelNameByIds(labelIdList);
        subjectDetailVO.setLabelNames(labelNameList);
        return subjectDetailVO;
    }

    /**
     * 评估报告
     */
    @Override
    public ReportVO getReport(GetReportReq req) {
        ReportVO reportVO = new ReportVO();
        Long practiceId = req.getPracticeId();
        // 获取练习情况
        PracticePO practicePO = practiceDao.selectById(practiceId);
        Long setId = practicePO.getSetId();
        // 获取套卷详情
        PracticeSetPO practiceSetPO = practiceSetDao.selectById(setId);
        reportVO.setTitle(practiceSetPO.getSetName());
        List<PracticeDetailPO> practiceDetailPOList = practiceDetailDao.selectByPracticeId(practiceId);
        if (CollectionUtils.isEmpty(practiceDetailPOList)) {
            return null;
        }
        int totalCount = practiceDetailPOList.size();
        // 正确题目数
        List<PracticeDetailPO> correctPoList = practiceDetailPOList.stream().filter(e ->
                Objects.equals(e.getAnswerStatus(), AnswerStatusEnum.CORRECT.getCode())).collect(Collectors.toList());
        reportVO.setCorrectSubject(correctPoList.size() + "/" + totalCount);
        // 封装智能图谱
        List<ReportSkillVO> reportSkillVOS = new ArrayList<>();
        // <标签id，所有题目数>
        Map<Long, Integer> totalMap = getSubjectLabelMap(practiceDetailPOList);
        // <标签id，正确题目数>
        Map<Long, Integer> correctMap = getSubjectLabelMap(correctPoList);
        totalMap.forEach((key, val) -> {
            // 根据标签下题目的得分封装对该标签的表现
            ReportSkillVO skillVO = new ReportSkillVO();
            SubjectLabelPO labelPO = subjectLabelDao.queryById(key);
            String labelName = labelPO.getLabelName();
            Integer correctCount = correctMap.get(key);
            if (Objects.isNull(correctCount)) {
                correctCount = 0;
            }
            skillVO.setName(labelName);
            BigDecimal rate = BigDecimal.ZERO;
            if (!Objects.equals(val, 0)) {
                // 将得分比例作为能力（star）
                rate = new BigDecimal(correctCount.toString()).divide(new BigDecimal(val.toString()), 4,
                        BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            }
            skillVO.setStar(rate);
            reportSkillVOS.add(skillVO);
        });
        if (log.isInfoEnabled()) {
            log.info("获取到的正确率{}", JSON.toJSONString(reportSkillVOS));
        }
        reportVO.setSkill(reportSkillVOS);
        return reportVO;
    }


    /**
     * 标签 ：题目
     */
    private Map<Long, Integer> getSubjectLabelMap(List<PracticeDetailPO> practiceDetailPOList) {
        if (CollectionUtils.isEmpty(practiceDetailPOList)) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> map = new HashMap<>();
        practiceDetailPOList.forEach(detail -> {
            Long subjectId = detail.getSubjectId();
            List<SubjectMappingPO> labelIdPO = subjectMappingDao.getLabelIdsBySubjectId(subjectId);
            labelIdPO.forEach(po -> {
                Long labelId = po.getLabelId();
                if (Objects.isNull(map.get(labelId))) {
                    map.put(labelId, 1);
                    return;
                }
                map.put(labelId, map.get(labelId) + 1);
            });
        });
        if (log.isInfoEnabled()) {
            log.info("获取到的题目对应的标签map{}", JSON.toJSONString(map));
        }
        return map;
    }

    /**
     * 练题榜（基于数据库实现）
     */
    @Override
    public List<RankVO> getPracticeRankList() {
        List<RankVO> list = new ArrayList<>();
        List<PracticeRankPO> poList = practiceDetailDao.getPracticeCount();
        if (CollectionUtils.isEmpty(poList)) {
            return list;
        }
        poList.forEach(e -> {
            RankVO rankVO = new RankVO();
            rankVO.setCount(e.getCount());
            UserInfo userInfo = userRpc.getUserInfo(e.getCreatedBy());
            rankVO.setName(userInfo.getNickName());
            rankVO.setAvatar(userInfo.getAvatar());
            list.add(rankVO);
        });
        return list;
    }

    /**
     * 放弃练习
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean giveUp(Long practiceId) {
        practiceDetailDao.deleteByPracticeId(practiceId);
        practiceDao.deleteById(practiceId);
        return true;
    }


}