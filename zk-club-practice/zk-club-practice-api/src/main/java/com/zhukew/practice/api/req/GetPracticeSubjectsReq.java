package com.zhukew.practice.api.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 前端请求：获取练习套题
 *
 * @author: Wei
 */
@Data
public class GetPracticeSubjectsReq implements Serializable {

    /**
     * 套题id
     */
    private Long setId;

    /**
     * 练习id
     */
    private Long practiceId;

}