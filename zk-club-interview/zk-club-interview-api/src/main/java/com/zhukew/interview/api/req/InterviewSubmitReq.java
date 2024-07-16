package com.zhukew.interview.api.req;

import com.zhukew.interview.api.enums.EngineEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


/**
 * 提交面试，智能生成评价
 */
@Getter
@Setter
public class InterviewSubmitReq implements Serializable {

    /**
     * 引擎
     */
    private String engine = EngineEnum.ZHU_KE.name();

    /**
     * 这是啥，俺不知道
     */
    private String interviewUrl;

    /**
     * 提交的题目列表
     */
    private List<Submit> questionList;

    /**
     * 提交的题目信息
     */
    @Data
    public static class Submit {

        private String labelName;

        private String subjectName;

        private String subjectAnswer;

        private String userAnswer;

        private Double userScore;

    }

}
