package com.zhukew.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 专项练习分类信息
 *
 * @author: Wei
 */
@Data
public class SpecialPracticeCategoryVO implements Serializable {

    /**
     * 小类名称
     */
    private String categoryName;

    /**
     * 小类id
     */
    private Long categoryId;

    /**
     * 小类下内容：包含哪些标签
     */
    private List<SpecialPracticeLabelVO> labelList;

}
