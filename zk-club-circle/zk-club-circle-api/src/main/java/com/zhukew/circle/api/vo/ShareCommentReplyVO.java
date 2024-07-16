package com.zhukew.circle.api.vo;

import com.zhukew.circle.api.common.TreeNode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 评论及回复信息
 * </p>
 *
 * @author Wei
 * @since 2024/05/16
 */
@Getter
@Setter
public class ShareCommentReplyVO extends TreeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 原始动态ID
     */
    private Long momentId;

    /**
     * 回复类型 1评论 2回复
     */
    private Integer replyType;

    /**
     * 内容
     */
    private String content;

    /**
     * 图片内容
     */
    private List<String> picUrlList;

    /**
     * 回复发出者
     */
    private String fromId;

    /**
     * 回复目标者
     */
    private String toId;

    /**
     * 更上层回复
     */
    private Long parentId;

    /**
     * 回复者用户名
     */
    private String userName;

    /**
     * 回复者头像
     */
    private String avatar;

    /**
     * 创建时间
     */
    private long createdTime;

    @Override
    public Long getNodeId() {
        return id;
    }

    @Override
    public Long getNodePId() {
        return parentId;
    }

}
