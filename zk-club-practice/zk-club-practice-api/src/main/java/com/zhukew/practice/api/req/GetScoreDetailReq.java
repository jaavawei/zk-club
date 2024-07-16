package com.zhukew.practice.api.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 前端获取得分详情请求
 *
 * @author: Wei
 */
@Data
public class GetScoreDetailReq implements Serializable {

    /**
     * 练习id
     */
    private Long practiceId;

}