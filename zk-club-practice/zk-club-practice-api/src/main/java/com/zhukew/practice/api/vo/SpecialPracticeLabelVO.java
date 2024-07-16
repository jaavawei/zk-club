package com.zhukew.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 专项练习分类下标签信息
 *
 * @author: Wei
 */
@Data
public class SpecialPracticeLabelVO implements Serializable {

    /**
     * 标签id
     */
    private Long id;

    /**
     * 分类id（分类id + 标签id构成唯一标识）
     */
    private String assembleId;

    /**
     * label名称
     */
    private String labelName;

}
