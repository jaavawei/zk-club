package com.zhukew.practice.server.dao;

import com.zhukew.practice.server.entity.dto.PracticeSubjectDTO;
import com.zhukew.practice.server.entity.po.SubjectPO;

import java.util.List;

public interface SubjectDao {


    /**
     * 获取练习面试题目
     */
    List<SubjectPO> getPracticeSubject(PracticeSubjectDTO dto);

    /**
     * 根据id查询题目
     */
    SubjectPO selectById(Long subjectId);


}