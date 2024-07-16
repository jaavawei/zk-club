package com.zhukew.circle.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhukew.circle.api.common.PageResult;
import com.zhukew.circle.api.req.GetShareMomentReq;
import com.zhukew.circle.api.req.RemoveShareMomentReq;
import com.zhukew.circle.api.req.SaveMomentCircleReq;
import com.zhukew.circle.api.vo.ShareMomentVO;
import com.zhukew.circle.server.entity.po.ShareMoment;

/**
 * <p>
 * 动态信息 服务类
 * </p>
 *
 * @author Wei
 * @since 2024/05/16
 */
public interface ShareMomentService extends IService<ShareMoment> {

    Boolean saveMoment(SaveMomentCircleReq req);

    PageResult<ShareMomentVO> getMoments(GetShareMomentReq req);

    Boolean removeMoment(RemoveShareMomentReq req);

    void incrReplyCount(Long id, int count);

}
