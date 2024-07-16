package com.zhukew.interview.server.service.impl;

import com.google.common.base.Preconditions;
import com.zhukew.interview.api.req.InterviewReq;
import com.zhukew.interview.api.req.InterviewSubmitReq;
import com.zhukew.interview.api.req.StartReq;
import com.zhukew.interview.api.vo.InterviewQuestionVO;
import com.zhukew.interview.api.vo.InterviewResultVO;
import com.zhukew.interview.api.vo.InterviewVO;
import com.zhukew.interview.server.dao.SubjectDao;
import com.zhukew.interview.server.entity.po.SubjectLabel;
import com.zhukew.interview.server.service.InterviewEngine;
import com.zhukew.interview.server.service.InterviewService;
import com.zhukew.interview.server.util.PDFUtil;
import com.zhukew.interview.server.util.keyword.KeyWordUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewServiceImpl implements InterviewService, ApplicationContextAware {

    private static final Map<String, InterviewEngine> engineMap = new HashMap<>();

    @Resource
    private SubjectDao subjectLabelDao;


    /**
     * 实现 ApplicationContextAware 接口
     * 在项目启动时，封装 engineMap
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取所有的enginesBean，并封装到 map 中
        Collection<InterviewEngine> engines = applicationContext.getBeansOfType(InterviewEngine.class).values();
        for (InterviewEngine engine : engines) {
            engineMap.put(engine.engineType().name(), engine);
        }
    }

    /**
     * 简历解析
     */
    @Override
    public InterviewVO analyse(InterviewReq req) {
        // 命中了哪些标签关键词
        List<String> keyWords = buildKeyWords(req.getUrl());
        // 选取引擎进行封装
        InterviewEngine engine = engineMap.get(req.getEngine());
        Preconditions.checkArgument(!Objects.isNull(engine), "引擎不能为空！");
        return engine.analyse(keyWords);
    }

    /**
     * 开始面试
     */
    @Override
    public InterviewQuestionVO start(StartReq req) {
        InterviewEngine engine = engineMap.get(req.getEngine());
        Preconditions.checkArgument(!Objects.isNull(engine), "引擎不能为空！");
        return engine.start(req);
    }


    /**
     * 面试提交，生成面试评价
     */
    @Override
    public InterviewResultVO submit(InterviewSubmitReq req) {
        InterviewEngine engine = engineMap.get(req.getEngine());
        Preconditions.checkArgument(!Objects.isNull(engine), "引擎不能为空！");
        return engine.submit(req);
    }

    /**
     * 解析pdf生成关键词
     */
    private List<String> buildKeyWords(String url) {
        // 获取 pdf 的 text
        String pdfText = PDFUtil.getPdfText(url);
        if (!KeyWordUtil.isInit()) {
            // 使用关键词工具类筛选标签关键词
            List<String> list = subjectLabelDao.listAllLabel().stream().map(SubjectLabel::getLabelName).collect(Collectors.toList());
            KeyWordUtil.addWord(list);
        }
        return KeyWordUtil.buildKeyWordsLists(pdfText);
    }

}
