package com.zhukew.practice.api.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 前端评估报告请求
 *
 * @author: Wei
 */
@Data
public class GetReportReq implements Serializable {

    /**
     * 练习id
     */
    private Long practiceId;

}