package com.zhukew.practice.server.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 练题情况明细PO
 *
 * @author: Wei
 */
@Data
public class PracticeDetailPO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 练题id
     */
    private Long practiceId;

    /**
     * 题目id：做了哪些题
     */
    private Long subjectId;

    /**
     * 题目类型
     */
    private Integer subjectType;

    /**
     * 是否正确 1正确 0错误
     */
    private Integer answerStatus;

    /**
     * 答案内容
     */
    private String answerContent;

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

    private Integer isDeleted;

    /**
     * 更新时间
     */
    private Date updateTime;

}
