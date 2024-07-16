package com.zhukew.circle.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhukew.circle.api.req.RemoveShareCircleReq;
import com.zhukew.circle.api.req.SaveShareCircleReq;
import com.zhukew.circle.api.req.UpdateShareCircleReq;
import com.zhukew.circle.api.vo.ShareCircleVO;
import com.zhukew.circle.server.entity.po.ShareCircle;

import java.util.List;

/**
 * 圈子信息 服务类
 *
 * @author Wei
 * @since 2024/05/16
 */
public interface ShareCircleService extends IService<ShareCircle> {

    List<ShareCircleVO> listResult();

    Boolean saveCircle(SaveShareCircleReq req);

    Boolean updateCircle(UpdateShareCircleReq req);

    Boolean removeCircle(RemoveShareCircleReq req);
}
