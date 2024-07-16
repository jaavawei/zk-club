package com.zhukew.interview.api.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


/**
 * 开始面试出参
 *
 * @author: Wei
 */
@Getter
@Setter
public class InterviewQuestionVO implements Serializable {

    /**
     * 面试题目集合
     */
    private List<Interview> questionList;

    /**
     * 面试题目信息
     */
    @Data
    public static class Interview {
        private String labelName;

        private String keyWord;

        private String subjectName;

        private String subjectAnswer;

    }

}
