package com.zhukew.subject.application.convert;

import com.zhukew.subject.application.dto.SubjectCategoryDTO;
import com.zhukew.subject.domain.entity.SubjectCategoryBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 题目分类dto转换器
 *
 * @author: Wei
 * @date: 2023/10/8
 */
@Mapper
public interface SubjectCategoryDTOConverter {

    SubjectCategoryDTOConverter INSTANCE = Mappers.getMapper(SubjectCategoryDTOConverter.class);

    List<SubjectCategoryDTO> convertBoToCategoryDTOList(List<SubjectCategoryBO> subjectCategoryDTO);

    SubjectCategoryBO convertDtoToCategoryBO(SubjectCategoryDTO subjectCategoryDTO);

    SubjectCategoryDTO convertBoToCategoryDTO(SubjectCategoryBO subjectCategoryBO);

}
