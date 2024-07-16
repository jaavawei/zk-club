package com.zhukew.interview.server.service;

import com.zhukew.interview.api.req.InterviewReq;
import com.zhukew.interview.api.req.InterviewSubmitReq;
import com.zhukew.interview.api.req.StartReq;
import com.zhukew.interview.api.vo.InterviewQuestionVO;
import com.zhukew.interview.api.vo.InterviewResultVO;
import com.zhukew.interview.api.vo.InterviewVO;

public interface InterviewService {

    /**
     * 简历解析
     */
    InterviewVO analyse(InterviewReq req);

    /**
     * 开始面试
     */
    InterviewQuestionVO start(StartReq req);

    /**
     * 提交面试，生成面试评价
     */
    InterviewResultVO submit(InterviewSubmitReq req);
}
