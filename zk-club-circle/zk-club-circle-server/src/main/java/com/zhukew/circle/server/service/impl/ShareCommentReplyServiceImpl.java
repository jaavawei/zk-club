package com.zhukew.circle.server.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhukew.circle.api.enums.IsDeletedFlagEnum;
import com.zhukew.circle.api.req.GetShareCommentReq;
import com.zhukew.circle.api.req.RemoveShareCommentReq;
import com.zhukew.circle.api.req.SaveShareCommentReplyReq;
import com.zhukew.circle.api.vo.ShareCommentReplyVO;
import com.zhukew.circle.server.dao.ShareCommentReplyMapper;
import com.zhukew.circle.server.dao.ShareMomentMapper;
import com.zhukew.circle.server.entity.dto.UserInfo;
import com.zhukew.circle.server.entity.po.ShareCommentReply;
import com.zhukew.circle.server.entity.po.ShareMoment;
import com.zhukew.circle.server.rpc.UserRpc;
import com.zhukew.circle.server.service.ShareCommentReplyService;
import com.zhukew.circle.server.util.LoginUtil;
import com.zhukew.circle.server.util.TreeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论及回复信息 服务实现类
 *
 * @author Wei
 * @since 2024/05/16
 */
@Service
public class ShareCommentReplyServiceImpl extends ServiceImpl<ShareCommentReplyMapper, ShareCommentReply> implements ShareCommentReplyService {

    @Resource
    private ShareMomentMapper shareMomentMapper;
    @Resource
    private UserRpc userRpc;

    /**
     * 发布评论内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveComment(SaveShareCommentReplyReq req) {
        ShareMoment moment = shareMomentMapper.selectById(req.getMomentId());
        ShareCommentReply comment = new ShareCommentReply();
        comment.setMomentId(req.getMomentId());
        comment.setReplyType(req.getReplyType());
        String loginId = LoginUtil.getLoginId();
        // 1评论 2回复
        if (req.getReplyType() == 1) {
            // 直接评论动态，设为最顶层
            comment.setParentId(-1L);
            comment.setToId(req.getTargetId());
            comment.setToUser(loginId);
            comment.setToUserAuthor(Objects.nonNull(moment.getCreatedBy()) && loginId.equals(moment.getCreatedBy()) ? 1 : 0);
        } else {
            // 回复别人评论
            comment.setParentId(req.getTargetId());
            comment.setReplyId(req.getTargetId());
            comment.setReplyUser(loginId);
            comment.setReplayAuthor(Objects.nonNull(moment.getCreatedBy()) && loginId.equals(moment.getCreatedBy()) ? 1 : 0);
        }
        comment.setContent(req.getContent());
        if (!CollectionUtils.isEmpty(req.getPicUrlList())) {
            comment.setPicUrls(JSON.toJSONString(req.getPicUrlList()));
        }
        comment.setCreatedBy(loginId);
        comment.setCreatedTime(new Date());
        comment.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        // 当前动态评论数量+1
        shareMomentMapper.incrReplyCount(moment.getId(), 1);
        return super.save(comment);
    }

    /**
     * 删除评论及所有回复
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeComment(RemoveShareCommentReq req) {
        ShareCommentReply comment = getById(req.getId());
        // 构造查询条件
        LambdaQueryWrapper<ShareCommentReply> query = Wrappers.<ShareCommentReply>lambdaQuery()
                .eq(ShareCommentReply::getMomentId, comment.getMomentId())
                .eq(ShareCommentReply::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode()).select(ShareCommentReply::getId,
                        ShareCommentReply::getMomentId,
                        ShareCommentReply::getReplyType,
                        ShareCommentReply::getContent,
                        ShareCommentReply::getPicUrls,
                        ShareCommentReply::getCreatedBy,
                        ShareCommentReply::getToUser,
                        ShareCommentReply::getParentId);
        List<ShareCommentReply> list = list(query);
        List<ShareCommentReply> replyList = new ArrayList<>();
        // 利用树工具查到所有孩子
        List<ShareCommentReply> tree = TreeUtils.buildTree(list);
        for (ShareCommentReply reply : tree) {
            TreeUtils.findAll(replyList, reply, req.getId());
        }
        // 关联子级对象及 moment 的回复数量
        Set<Long> ids = replyList.stream().map(ShareCommentReply::getId).collect(Collectors.toSet());
        LambdaUpdateWrapper<ShareCommentReply> update = Wrappers.<ShareCommentReply>lambdaUpdate()
                .eq(ShareCommentReply::getMomentId, comment.getMomentId())
                .in(ShareCommentReply::getId, ids);
        ShareCommentReply updateEntity = new ShareCommentReply();
        updateEntity.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        int count = getBaseMapper().update(updateEntity, update);
        shareMomentMapper.incrReplyCount(comment.getMomentId(), -count);
        return true;
    }

    /**
     * 查看动态下所有评论
     */
    @Override
    public List<ShareCommentReplyVO> listComment(GetShareCommentReq req) {
        // 查出该动态下所有内容
        LambdaQueryWrapper<ShareCommentReply> query = Wrappers.<ShareCommentReply>lambdaQuery()
                .eq(ShareCommentReply::getMomentId, req.getId())
                .eq(ShareCommentReply::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .select(ShareCommentReply::getId,
                        ShareCommentReply::getMomentId,
                        ShareCommentReply::getReplyType,
                        ShareCommentReply::getContent,
                        ShareCommentReply::getPicUrls,
                        ShareCommentReply::getCreatedBy,
                        ShareCommentReply::getToUser,
                        ShareCommentReply::getCreatedTime,
                        ShareCommentReply::getParentId);
        List<ShareCommentReply> list = list(query);
        List<String> userNameList = list.stream().map(ShareCommentReply::getCreatedBy).distinct().collect(Collectors.toList());
        // 封装用户完整信息（名称和头像）
        Map<String, UserInfo> userInfoMap = userRpc.batchGetUserInfo(userNameList);
        UserInfo defaultUser = new UserInfo();
        // 组装 list，并转换成树形结构返回
        List<ShareCommentReplyVO> voList = list.stream().map(item -> {
            ShareCommentReplyVO vo = new ShareCommentReplyVO();
            vo.setId(item.getId());
            vo.setMomentId(item.getMomentId());
            vo.setReplyType(item.getReplyType());
            vo.setContent(item.getContent());
            if (Objects.nonNull(item.getPicUrls())) {
                vo.setPicUrlList(JSONArray.parseArray(item.getPicUrls(), String.class));
            }
            if (item.getReplyType() == 2) {
                vo.setFromId(item.getCreatedBy());
                vo.setToId(item.getToUser());
            }
            vo.setParentId(item.getParentId());
            UserInfo user = userInfoMap.getOrDefault(item.getCreatedBy(), defaultUser);
            vo.setUserName(user.getNickName());
            vo.setAvatar(user.getAvatar());
            vo.setCreatedTime(item.getCreatedTime().getTime());
            return vo;
        }).collect(Collectors.toList());
        return TreeUtils.buildTree(voList);
    }

}
