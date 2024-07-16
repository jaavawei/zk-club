package com.zhukew.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 专项练习指定内容
 *
 * @author: Wei
 */
@Data
public class SpecialPracticeVO implements Serializable {

    /**
     * 大类名称
     */
    private String primaryCategoryName;

    /**
     * 大类id
     */
    private Long primaryCategoryId;

    /**
     * 大类下内容：包含哪些小类
     */
    private List<SpecialPracticeCategoryVO> categoryList;

}
