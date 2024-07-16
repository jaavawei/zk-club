package com.zhukew.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ReportSkillVO implements Serializable {

    /**
     * 分数
     */
    private BigDecimal star;

    /**
     * 标签名
     */
    private String name;

}