package com.zhukew.auth.infra.basic.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (AuthUserRole)实体类
 *
 * @author Wei
 * @since 2023-11-03 00:18:09
 */
@Data
public class AuthUserRole implements Serializable {
    private static final long serialVersionUID = -34579360091831908L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 角色id
     */
    private Long roleId;
    /**
     * 创建人
     */
    private String createdBy;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标识
     */
    private Integer isDeleted;



}

