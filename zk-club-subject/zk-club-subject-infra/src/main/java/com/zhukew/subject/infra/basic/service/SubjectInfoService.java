package com.zhukew.subject.infra.basic.service;

import com.zhukew.subject.infra.basic.entity.SubjectInfo;

import java.util.List;

/**
 * 题目信息表(SubjectInfo)表服务接口
 *
 * @author Wei
 * @since 2023-10-05 21:28:58
 */
public interface SubjectInfoService {

    /**
     * 通过ID查询单条数据
     */
    SubjectInfo queryById(Long id);

    /**
     * 新增数据
     */
    SubjectInfo insert(SubjectInfo subjectInfo);

    /**
     * 修改数据
     */
    SubjectInfo update(SubjectInfo subjectInfo);

    /**
     * 通过主键删除数据
     */
    boolean deleteById(Long id);

    /**
     * 根据条件计数
     */
    int countByCondition(SubjectInfo subjectInfo, Long categoryId, Long labelId);

    /**
     * 分页查询
     */
    List<SubjectInfo> queryPage(SubjectInfo subjectInfo, Long categoryId, Long labelId, int start, Integer pageSize);

    /**
     * 查询贡献题目数量
     */
    List<SubjectInfo> getContributeCount();

    /**
     * 查询题目游标
     */
    Long querySubjectIdCursor(Long subjectId, Long categoryId, Long labelId, int cursor);

}
