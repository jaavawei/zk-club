package com.zhukew.circle.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhukew.circle.api.req.GetShareCommentReq;
import com.zhukew.circle.api.req.RemoveShareCommentReq;
import com.zhukew.circle.api.req.SaveShareCommentReplyReq;
import com.zhukew.circle.api.vo.ShareCommentReplyVO;
import com.zhukew.circle.server.entity.po.ShareCommentReply;

import java.util.List;

/**
 * <p>
 * 评论及回复信息 服务类
 * </p>
 *
 * @author Wei
 * @since 2024/05/16
 */
public interface ShareCommentReplyService extends IService<ShareCommentReply> {

    /**
     * 发布评论
     */
    Boolean saveComment(SaveShareCommentReplyReq req);

    /**
     * 删除评论
     */
    Boolean removeComment(RemoveShareCommentReq req);

    /**
     * 查看评论
     */
    List<ShareCommentReplyVO> listComment(GetShareCommentReq req);

}
