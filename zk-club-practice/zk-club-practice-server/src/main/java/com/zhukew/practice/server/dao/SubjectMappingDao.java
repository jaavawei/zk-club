package com.zhukew.practice.server.dao;


import com.zhukew.practice.server.entity.po.LabelCountPO;
import com.zhukew.practice.server.entity.po.SubjectMappingPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目分类关系表(SubjectMapping)表数据库访问层
 *
 * @author Wei
 * @since 2023-10-03 22:17:07
 */
public interface SubjectMappingDao {

    List<LabelCountPO> getLabelSubjectCount(@Param("categoryId") Long categoryId,
                                            @Param("subjectTypeList") List<Integer> subjectTypeList);

    List<SubjectMappingPO> getLabelIdsBySubjectId(Long subjectId);

}

