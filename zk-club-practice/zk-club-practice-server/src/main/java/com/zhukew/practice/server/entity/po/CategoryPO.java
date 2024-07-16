package com.zhukew.practice.server.entity.po;

import lombok.Data;

/**
 * 分类PO
 *
 * @author: Wei
 */
@Data
public class CategoryPO {

    /**
     * 分类id
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类类型：大类或小类
     */
    private Integer categoryType;

    /**
     * 所属大类id
     */
    private Long parentId;

}
