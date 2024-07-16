package com.zhukew.practice.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 得分详情VO
 *
 * @author: Wei
 */
@Data
public class ScoreDetailVO implements Serializable {

    /**
     * 题目id
     */
    private Long subjectId;

    /**
     * 题目类型
     */
    private Integer subjectType;

    /**
     * 是否正确
     */
    private Integer isCorrect;


}