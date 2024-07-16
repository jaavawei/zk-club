package com.zhukew.gateway.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (AuthRole)实体类
 *
 * @author Wei
 * @since 2023-11-02 23:55:19
 */
@Data
public class AuthRole implements Serializable {
    private static final long serialVersionUID = 422256240999600735L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 角色key
     */
    private String roleKey;
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
     * 是否删除标识
     */
    private Integer isDeleted;

}

