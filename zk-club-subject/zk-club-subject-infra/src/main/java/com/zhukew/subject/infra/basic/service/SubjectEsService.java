package com.zhukew.subject.infra.basic.service;

import com.zhukew.subject.common.entity.PageResult;
import com.zhukew.subject.infra.basic.entity.SubjectInfoEs;

public interface SubjectEsService {

    boolean insert(SubjectInfoEs subjectInfoEs);

    PageResult<SubjectInfoEs> querySubjectList(SubjectInfoEs subjectInfoEs);

}
