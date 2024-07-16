package com.zhukew.subject.domain.service;


import com.zhukew.subject.common.entity.PageResult;
import com.zhukew.subject.domain.entity.SubjectLikedBO;

/**
 * 题目点赞表 领域service
 *
 * @author Wei
 * @since 2024-01-07 23:08:45
 */
public interface SubjectLikedDomainService {

    /**
     * 添加 题目点赞表 信息
     */
    void add(SubjectLikedBO subjectLikedBO);

    /**
     * 获取当前是否被点赞过
     */
    Boolean isLiked(String subjectId, String userId);

    /**
     * 获取点赞数量
     */
    Integer getLikedCount(String subjectId);

    /**
     * 更新 题目点赞表 信息
     */
    Boolean update(SubjectLikedBO subjectLikedBO);

    /**
     * 删除 题目点赞表 信息
     */
    Boolean delete(SubjectLikedBO subjectLikedBO);

    /**
     * 同步点赞数据
     */
    void syncLiked();

    /**
     * 分页获取点赞信息
     */
    PageResult<SubjectLikedBO> getSubjectLikedPage(SubjectLikedBO subjectLikedBO);

    /**
     * mq消费者存储点赞到mysql
     */
    void syncLikedByMsg(SubjectLikedBO subjectLikedBO);

}
