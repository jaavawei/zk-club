package com.zhukew.circle.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zhukew.circle.api.enums.IsDeletedFlagEnum;
import com.zhukew.circle.api.req.RemoveShareCircleReq;
import com.zhukew.circle.api.req.SaveShareCircleReq;
import com.zhukew.circle.api.req.UpdateShareCircleReq;
import com.zhukew.circle.api.vo.ShareCircleVO;
import com.zhukew.circle.server.dao.ShareCircleMapper;
import com.zhukew.circle.server.entity.po.ShareCircle;
import com.zhukew.circle.server.service.ShareCircleService;
import com.zhukew.circle.server.util.LoginUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 圈子信息 服务实现类
 *
 * @author Wei
 * @since 2024/05/16
 */
@Service
public class ShareCircleServiceImpl extends ServiceImpl<ShareCircleMapper, ShareCircle> implements ShareCircleService {

    /**
     * Caffeine本地缓存
     */
    private static final Cache<Integer, List<ShareCircleVO>> CACHE = Caffeine.newBuilder().initialCapacity(1)
            .maximumSize(1).expireAfterWrite(Duration.ofSeconds(30)).build();

    /**
     * 查看所有圈子
     */
    @Override
    public List<ShareCircleVO> listResult() {
        // 从父到子，按树状返回
        List<ShareCircleVO> res = CACHE.getIfPresent(1);
        return Optional.ofNullable(res).orElseGet(() -> {
            List<ShareCircle> list = super.list(Wrappers.<ShareCircle>lambdaQuery().eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode()));
            // 先过滤出最父级的圈子
            List<ShareCircle> parentList = list.stream().filter(item -> item.getParentId() == -1L).collect(Collectors.toList());
            // 根据parentId分组
            Map<Long, List<ShareCircle>> map = list.stream().collect(Collectors.groupingBy(ShareCircle::getParentId));
            List<ShareCircleVO> collect = parentList.stream().map(item -> {
                // 组装
                ShareCircleVO vo = new ShareCircleVO();
                vo.setId(item.getId());
                vo.setCircleName(item.getCircleName());
                vo.setIcon(item.getIcon());
                List<ShareCircle> shareCircles = map.get(item.getId());
                if (CollectionUtils.isEmpty(shareCircles)) {
                    vo.setChildren(Collections.emptyList());
                } else {
                    List<ShareCircleVO> children = shareCircles.stream().map(cItem -> {
                        ShareCircleVO cVo = new ShareCircleVO();
                        cVo.setId(cItem.getId());
                        cVo.setCircleName(cItem.getCircleName());
                        cVo.setIcon(cItem.getIcon());
                        cVo.setChildren(Collections.emptyList());
                        return cVo;
                    }).collect(Collectors.toList());
                    vo.setChildren(children);
                }
                return vo;
            }).collect(Collectors.toList());
            CACHE.put(1, collect);
            return collect;
        });
    }

    /**
     * 新增圈子
     */
    @Override
    public Boolean saveCircle(SaveShareCircleReq req) {
        // mybatisPlus也太简单了吧
        ShareCircle circle = new ShareCircle();
        circle.setCircleName(req.getCircleName());
        circle.setIcon(req.getIcon());
        circle.setParentId(req.getParentId());
        circle.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        circle.setCreatedTime(new Date());
        circle.setCreatedBy(LoginUtil.getLoginId());
        CACHE.invalidateAll();
        return save(circle);
    }

    /**
     * 更新圈子
     */
    @Override
    public Boolean updateCircle(UpdateShareCircleReq req) {
        LambdaUpdateWrapper<ShareCircle> update = Wrappers.<ShareCircle>lambdaUpdate().eq(ShareCircle::getId, req.getId())
                .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .set(Objects.nonNull(req.getParentId()), ShareCircle::getParentId, req.getParentId())
                .set(Objects.nonNull(req.getIcon()), ShareCircle::getIcon, req.getIcon())
                .set(Objects.nonNull(req.getCircleName()), ShareCircle::getCircleName, req.getCircleName())
                .set(ShareCircle::getUpdateBy, LoginUtil.getLoginId())
                .set(ShareCircle::getUpdateTime, new Date());
        boolean res = super.update(update);
        CACHE.invalidateAll();
        return res;
    }

    /**
     * 删除圈子
     */
    @Override
    public Boolean removeCircle(RemoveShareCircleReq req) {
        boolean res = super.update(Wrappers.<ShareCircle>lambdaUpdate().eq(ShareCircle::getId, req.getId())
                .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .set(ShareCircle::getIsDeleted, IsDeletedFlagEnum.DELETED.getCode()));
        super.update(Wrappers.<ShareCircle>lambdaUpdate().eq(ShareCircle::getParentId, req.getId())
                .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .set(ShareCircle::getIsDeleted, IsDeletedFlagEnum.DELETED.getCode()));
        CACHE.invalidateAll();
        return res;
    }
}
