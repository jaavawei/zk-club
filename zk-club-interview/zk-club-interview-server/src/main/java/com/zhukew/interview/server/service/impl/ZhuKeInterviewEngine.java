package com.zhukew.interview.server.service.impl;

import com.zhukew.interview.api.enums.EngineEnum;
import com.zhukew.interview.api.req.InterviewSubmitReq;
import com.zhukew.interview.api.req.StartReq;
import com.zhukew.interview.api.vo.InterviewQuestionVO;
import com.zhukew.interview.api.vo.InterviewResultVO;
import com.zhukew.interview.api.vo.InterviewVO;
import com.zhukew.interview.server.dao.SubjectDao;
import com.zhukew.interview.server.entity.po.SubjectCategory;
import com.zhukew.interview.server.entity.po.SubjectInfo;
import com.zhukew.interview.server.entity.po.SubjectLabel;
import com.zhukew.interview.server.service.InterviewEngine;
import com.zhukew.interview.server.util.EvaluateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 猪客club本地引擎
 *
 * @author: Wei
 */
@Service
public class ZhuKeInterviewEngine implements InterviewEngine {


    private List<SubjectLabel> labels;
    private Map<Long, SubjectCategory> categoryMap;

    @PostConstruct
    public void init() {
        // 所有标签
        labels = subjectDao.listAllLabel();
        // id ：category
        categoryMap = subjectDao.listAllCategory().stream().collect(Collectors.toMap(SubjectCategory::getId, Function.identity()));
    }

    @Resource
    private SubjectDao subjectDao;

    @Override
    public EngineEnum engineType() {
        return EngineEnum.ZHU_KE;
    }

    /**
     * 简历分析
     */
    @Override
    public InterviewVO analyse(List<String> KeyWords) {

        if (CollectionUtils.isEmpty(KeyWords)) {
            return new InterviewVO();
        }
        List<InterviewVO.Interview> views = this.labels.stream().filter(item -> KeyWords.contains(item.getLabelName())).map(item -> {
            // 封装成 interview
            InterviewVO.Interview interview = new InterviewVO.Interview();
            SubjectCategory subjectCategory = categoryMap.get(item.getCategoryId());
            if (Objects.nonNull(subjectCategory)) {
                // 封装关键词成前端需要的样式，例如：数据库-Redis
                interview.setKeyWord(String.format("%s-%s", subjectCategory.getCategoryName(), item.getLabelName()));
            } else {
                interview.setKeyWord(item.getLabelName());
            }
            interview.setCategoryId(item.getCategoryId());
            interview.setLabelId(item.getId());
            return interview;
        }).collect(Collectors.toList());

        InterviewVO vo = new InterviewVO();
        vo.setQuestionList(views);
        return vo;

    }

    /**
     * 开始面试，获取题目
     */
    @Override
    public InterviewQuestionVO start(StartReq req) {

        // 拿到所有标签id
        List<Long> ids = req.getQuestionList().stream().map(StartReq.Key::getLabelId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return new InterviewQuestionVO();
        }
        // 查询题目
        List<SubjectInfo> subjectInfos = subjectDao.listSubjectByLabelIds(ids);
        List<InterviewQuestionVO.Interview> views = subjectInfos.stream().map(item -> {
            // 封装成返回的 interview
            InterviewQuestionVO.Interview view = new InterviewQuestionVO.Interview();
            view.setSubjectName(item.getSubjectName());
            view.setSubjectAnswer(item.getSubjectAnswer());
            view.setLabelName(item.getLabelName());
            view.setKeyWord(String.format("%s-%s", item.getCategoryName(), item.getLabelName()));
            return view;
        }).collect(Collectors.toList());
        // 只需要八道题，题目多了就随机抽取
        if (views.size() > 8) {
            Collections.shuffle(views);
            views = views.subList(0, 8);
        }
        InterviewQuestionVO vo = new InterviewQuestionVO();
        vo.setQuestionList(views);
        return vo;

    }

    /**
     * 提交面试，生成评价
     */
    @Override
    public InterviewResultVO submit(InterviewSubmitReq req) {

        List<InterviewSubmitReq.Submit> submits = req.getQuestionList();
        double total = submits.stream().mapToDouble(InterviewSubmitReq.Submit::getUserScore).sum();
        double avg = total / submits.size();
        // 获取评价
        String avtTips = EvaluateUtils.avgEvaluate(avg);
        String tips = submits.stream().map(item -> {
            String keyWord = item.getLabelName();
            String evaluate = EvaluateUtils.evaluate(item.getUserScore());
            return String.format(evaluate, keyWord);
        }).distinct().collect(Collectors.joining(";"));
        InterviewResultVO vo = new InterviewResultVO();
        vo.setAvgScore(avg);
        vo.setTips(tips);
        vo.setAvgTips(avtTips);
        return vo;

    }

}
