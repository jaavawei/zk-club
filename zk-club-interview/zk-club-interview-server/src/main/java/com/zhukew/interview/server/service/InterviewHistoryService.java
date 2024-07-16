package com.zhukew.interview.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhukew.interview.api.common.PageResult;
import com.zhukew.interview.api.req.InterviewHistoryReq;
import com.zhukew.interview.api.req.InterviewSubmitReq;
import com.zhukew.interview.api.vo.InterviewHistoryVO;
import com.zhukew.interview.api.vo.InterviewResultVO;
import com.zhukew.interview.server.entity.po.InterviewHistory;

/**
 * 面试汇总记录表(InterviewHistory)表服务接口
 *
 * @author Wei
 * @since 2024-05-23 22:56:03
 */
public interface InterviewHistoryService extends IService<InterviewHistory> {

    /**
     * 面试评价
     */
    void logInterview(InterviewSubmitReq req, InterviewResultVO submit);


    PageResult<InterviewHistoryVO> getHistory(InterviewHistoryReq req);

}
