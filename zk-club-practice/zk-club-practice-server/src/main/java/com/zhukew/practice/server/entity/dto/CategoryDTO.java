package com.zhukew.practice.server.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class CategoryDTO {

    /**
     * 题目类型列表
     */
    private List<Integer> subjectTypeList;

    /**
     * 分类类型（大类或小类）
     */
    private Integer categoryType;

    /**
     * 所属大类id
     */
    private Long parentId;

}
