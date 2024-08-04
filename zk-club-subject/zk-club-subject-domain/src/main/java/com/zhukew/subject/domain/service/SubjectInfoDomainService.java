package com.zhukew.subject.domain.service;

import com.zhukew.subject.common.entity.PageResult;
import com.zhukew.subject.domain.entity.SubjectInfoBO;
import com.zhukew.subject.domain.entity.SubjectLikedBO;
import com.zhukew.subject.infra.basic.entity.SubjectInfoEs;

import java.util.List;

/**
 * 题目领域服务
 * 
 * @author: Wei
 * @date: 2023/10/3
 */
public interface SubjectInfoDomainService {

    /**
     * 新增题目
     */
    void add(SubjectInfoBO subjectInfoBO);

    /**
     * 新增题目时异步更新ES和Redis
     */
    void syncAddByMsg(SubjectInfoBO subjectInfoBO);

    /**
     * 分页查询
     */
    PageResult<SubjectInfoBO> getSubjectPage(SubjectInfoBO subjectInfoBO);

    /**
     * 查询题目信息
     */
    SubjectInfoBO querySubjectInfo(SubjectInfoBO subjectInfoBO);

    /**
     * 全文检索
     */
    PageResult<SubjectInfoEs> getSubjectPageBySearch(SubjectInfoBO subjectInfoBO);

    /**
    * 获取题目贡献榜
    */
    List<SubjectInfoBO> getContributeList();

}

