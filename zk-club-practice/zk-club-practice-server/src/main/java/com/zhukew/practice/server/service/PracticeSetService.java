package com.zhukew.practice.server.service;

import com.zhukew.practice.api.common.PageResult;
import com.zhukew.practice.api.req.GetPracticeSubjectsReq;
import com.zhukew.practice.api.req.GetUnCompletePracticeReq;
import com.zhukew.practice.server.entity.dto.PracticeSetDTO;
import com.zhukew.practice.server.entity.dto.PracticeSubjectDTO;
import com.zhukew.practice.api.vo.*;

import java.util.List;

/**
 * 练习套卷service服务
 *
 * @author: Wei
 */
public interface PracticeSetService {

    /**
     * 获取专项练习内容
     */
    List<SpecialPracticeVO> getSpecialPracticeContent();

    /**
     * 开始练习
     */
    PracticeSetVO addPractice(PracticeSubjectDTO dto);

    /**
     * 获取练习题
     */
    PracticeSubjectListVO getSubjects(GetPracticeSubjectsReq req);

    /**
     * 获取题目
     */
    PracticeSubjectVO getPracticeSubject(PracticeSubjectDTO dto);

    /**
     * 获取模拟套题内容
     */
    PageResult<PracticeSetVO> getPreSetContent(PracticeSetDTO dto);

    /**
     * 获取未完成练习内容
     */
    PageResult<UnCompletePracticeSetVO> getUnCompletePractice(GetUnCompletePracticeReq req);

}
