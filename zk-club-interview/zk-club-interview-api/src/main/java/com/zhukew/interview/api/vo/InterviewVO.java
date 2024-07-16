package com.zhukew.interview.api.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 简历解析出参
 */
@Getter
@Setter
public class InterviewVO implements Serializable {

    /**
     * 可选面试分类list
     */
    private List<Interview> questionList;

    /**
     * 面试关键词等信息
     */
    @Data
    public static class Interview {
        private String keyWord;
        private Long categoryId;
        private Long labelId;
    }

}
