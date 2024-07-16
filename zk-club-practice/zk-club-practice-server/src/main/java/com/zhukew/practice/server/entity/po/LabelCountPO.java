package com.zhukew.practice.server.entity.po;

import lombok.Data;

/**
 * 标签PO：包含题目计数
 *
 * @author: Wei
 */
@Data
public class LabelCountPO {

    /**
     * 标签id
     */
    private Long labelId;

    /**
     * 标签下题目数量
     */
    private Integer count;

    /**
     * 标签名称
     */
    private String labelName;

}
