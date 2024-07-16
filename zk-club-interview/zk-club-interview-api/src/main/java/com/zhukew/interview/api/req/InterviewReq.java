package com.zhukew.interview.api.req;

import com.zhukew.interview.api.enums.EngineEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 简历分析入参
 *
 * @author: Wei
 */
@Getter
@Setter
public class InterviewReq implements Serializable {

    /**
     * 简历ossUrl
     */
    private String url;

    /**
     * 引擎
     */
    private String engine = EngineEnum.ZHU_KE.name();

}
