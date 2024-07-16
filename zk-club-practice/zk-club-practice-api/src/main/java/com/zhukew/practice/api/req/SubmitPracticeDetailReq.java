package com.zhukew.practice.api.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 前端提交练习请求
 *
 * @author: Wei
 */
@Data
public class SubmitPracticeDetailReq implements Serializable {

    /**
     * 套题id
     */
    private Long setId;

    /**
     * 练习id（不需要题目id，每次答题情况发生变化时都会发生请求）
     */
    private Long practiceId;

    /**
     * 用时
     */
    private String timeUse;

    /**
     * 交卷时间
     */
    private String submitTime;


}