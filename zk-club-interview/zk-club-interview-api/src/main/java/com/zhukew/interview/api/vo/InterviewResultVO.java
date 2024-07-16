package com.zhukew.interview.api.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * 智能评价结果
 */
@Getter
@Setter
public class InterviewResultVO implements Serializable {

    /**
     * 平均分
     */
    private Double avgScore;

    /**
     * 面试评价
     */
    private String tips;

    /**
     * 平均评价
     */
    private String avgTips;

}
