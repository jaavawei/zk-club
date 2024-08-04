package com.zhukew.subject.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 题目点赞消息
 *
 * @author Wei
 * @since 2024-01-07 23:08:45
 */
@Data
public class SubjectLikedMessage implements Serializable {


    /**
     * 题目id
     */
    private Long subjectId;

    /**
     * 点赞人id
     */
    private String likeUserId;

    /**
     * 点赞状态 1点赞 0不点赞
     */
    private Integer status;

    /**
     * message 唯一 id
     */
    private String id;

}

