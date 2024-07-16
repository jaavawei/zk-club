package com.zhukew.interview.api.req;

import com.zhukew.interview.api.enums.EngineEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


/**
 * 开始面试入参
 */
@Getter
@Setter
public class StartReq implements Serializable {

    /**
     * 引擎
     */
    private String engine = EngineEnum.ZHU_KE.name();

    /**
     * 面试题目list
     */
    private List<Key> questionList;

    /**
     * 面试题目信息
     */
    @Data
    public static class Key {
        private String keyWord;
        private Long categoryId;
        private Long labelId;
    }

}
