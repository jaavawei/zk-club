package com.zhukew.circle.api.req;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 鸡圈内容信息
 *
 * @author Wei
 * @since 2024/05/16
 */
@Getter
@Setter
public class GetShareCommentReq implements Serializable {

    /**
     * 动态内容id
     */
    private Long id;

}
