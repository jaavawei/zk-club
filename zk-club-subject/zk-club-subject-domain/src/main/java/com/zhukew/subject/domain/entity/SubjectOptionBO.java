package com.zhukew.subject.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题目dto
 *
 * @author: Wei
 * @date: 2023/10/5
 */
@Data
public class SubjectOptionBO implements Serializable {

    /**
     * 题目答案
     */
    private String subjectAnswer;

    /**
     * 答案选项
     */
    private List<SubjectAnswerBO> optionList;

}

