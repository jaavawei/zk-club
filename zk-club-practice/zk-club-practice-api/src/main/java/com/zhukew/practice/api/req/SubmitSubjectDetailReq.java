package com.zhukew.practice.api.req;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 前端提交题目请求：点击下一题时自动提交保存
 *
 * @author: Wei
 */
@Data
public class SubmitSubjectDetailReq implements Serializable {

    /**
     * 练习id
     */
    private Long practiceId;

    /**
     * 题目id
     */
    private Long subjectId;

    /**
     * 题目答案
     */
    private List<Integer> answerContents;

    /**
     * 题目类型
     */
    private Integer subjectType;

    /**
     * 用时
     */
    private String timeUse;

}