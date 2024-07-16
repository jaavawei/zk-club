package com.zhukew.practice.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 练习套卷VO
 *
 * @author: Wei
 */
@Data
public class PracticeSetVO implements Serializable {

    /**
     * 套题id
     */
    private Long setId;

    /**
     * 套题名称
     */
    private String setName;

    /**
     * 套题热度
     */
    private Integer setHeat;

    /**
     * 套题描述
     */
    private String setDesc;

}