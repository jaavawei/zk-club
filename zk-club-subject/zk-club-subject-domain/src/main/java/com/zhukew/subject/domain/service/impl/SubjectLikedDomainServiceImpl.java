package com.zhukew.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhukew.subject.common.entity.PageResult;
import com.zhukew.subject.common.enums.IsDeletedFlagEnum;
import com.zhukew.subject.common.enums.SubjectLikedStatusEnum;
import com.zhukew.subject.common.util.LoginUtil;
import com.zhukew.subject.domain.convert.SubjectLikedBOConverter;
import com.zhukew.subject.domain.entity.SubjectLikedBO;
import com.zhukew.subject.domain.entity.SubjectLikedMessage;
import com.zhukew.subject.domain.redis.RedisUtil;
import com.zhukew.subject.domain.service.SubjectLikedDomainService;
import com.zhukew.subject.infra.basic.entity.SubjectInfo;
import com.zhukew.subject.infra.basic.entity.SubjectLiked;
import com.zhukew.subject.infra.basic.service.SubjectInfoService;
import com.zhukew.subject.infra.basic.service.SubjectLikedService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 题目点赞domainService
 *
 * @author Wei
 * @since 2024-01-07 23:08:45
 */
@Service
@Slf4j
public class SubjectLikedDomainServiceImpl implements SubjectLikedDomainService {

    @Resource
    private SubjectLikedService subjectLikedService;

    @Resource
    private SubjectInfoService subjectInfoService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    // redis中的key常量
    // SUBJECT_LIKED_KEY：点赞状态的hash总key（用于定时持久化）
    private static final String SUBJECT_LIKED_KEY = "subject.liked";

    // SUBJECT_LIKED_COUNT_KEY：题目点赞数量key
    private static final String SUBJECT_LIKED_COUNT_KEY = "subject.liked.count";

    // SUBJECT_LIKED_DETAIL_KEY：一个题目是否被一个用户点赞了（用于读取）
    private static final String SUBJECT_LIKED_DETAIL_KEY = "subject.liked.detail";

    /**
     * 增添题目点赞信息
     */
    @Override
    public void add(SubjectLikedBO subjectLikedBO) {
        Long subjectId = subjectLikedBO.getSubjectId();
        String likeUserId = subjectLikedBO.getLikeUserId();
        Integer status = subjectLikedBO.getStatus();

        /*// 存到redis的hash中，表明该用户对该题目是否点赞了
         String hashKey = buildSubjectLikedKey(subjectId.toString(), likeUserId);
         redisUtil.putHash(SUBJECT_LIKED_KEY, hashKey, status);*/

        // 封装 题目id，点赞人，点赞状态（点赞或取消） 等信息到SubjectLikedMessage中
        SubjectLikedMessage subjectLikedMessage = new SubjectLikedMessage();
        subjectLikedMessage.setSubjectId(subjectId);
        subjectLikedMessage.setLikeUserId(likeUserId);
        subjectLikedMessage.setStatus(status);

        // 原本将点赞信息完整地存储在 redis hash 中，然后利用 xxl-job 定时进行持久化
        // 但是 redis 故障可能会丢失信息，改用 rocketMQ 异步直接将点赞信息存储到数据库
        rocketMQTemplate.convertAndSend("subject-liked", JSON.toJSONString(subjectLikedMessage));

        String detailKey = SUBJECT_LIKED_DETAIL_KEY + "." + subjectId + "." + likeUserId;
        String countKey = SUBJECT_LIKED_COUNT_KEY + "." + subjectId;
        if (SubjectLikedStatusEnum.LIKED.getCode() == status) {
            // 点赞处理
            redisUtil.increment(countKey, 1);
            redisUtil.set(detailKey, "1");
        } else {
            // 取消点赞处理
            Integer count = redisUtil.getInt(countKey);
            if (Objects.isNull(count) || count <= 0) {
                return;
            }
            redisUtil.increment(countKey, -1);
            redisUtil.del(detailKey);
        }
    }

    /**
     * 查看点赞状态
     */
    @Override
    public Boolean isLiked(String subjectId, String userId) {
        String detailKey = SUBJECT_LIKED_DETAIL_KEY + "." + subjectId + "." + userId;
        return redisUtil.exist(detailKey);
    }

    /**
     * 获取题目点赞数量
     */
    @Override
    public Integer getLikedCount(String subjectId) {
        String countKey = SUBJECT_LIKED_COUNT_KEY + "." + subjectId;
        Integer count = redisUtil.getInt(countKey);
        if (Objects.isNull(count) || count <= 0) {
            return 0;
        }
        return redisUtil.getInt(countKey);
    }

    /**
     * 构建hash小key：被点赞题目 + 点赞人
     */
    private String buildSubjectLikedKey(String subjectId, String userId) {
        return subjectId + ":" + userId;
    }

    /**
     * 数据库更新题目点赞信息
     */
    @Override
    public Boolean update(SubjectLikedBO subjectLikedBO) {
        SubjectLiked subjectLiked = SubjectLikedBOConverter.INSTANCE.convertBOToEntity(subjectLikedBO);
        return subjectLikedService.update(subjectLiked) > 0;
    }

    /**
     * 数据库删除题目点赞
     */
    @Override
    public Boolean delete(SubjectLikedBO subjectLikedBO) {
        SubjectLiked subjectLiked = new SubjectLiked();
        subjectLiked.setId(subjectLikedBO.getId());
        subjectLiked.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        return subjectLikedService.update(subjectLiked) > 0;
    }

    /**
     * 缓存持久化（由xxl-job定时执行）
     */
    @Override
    public void syncLiked() {
        // 将 redis hash 存储的点赞数据转移出来
        Map<Object, Object> subjectLikedMap = redisUtil.getHashAndDelete(SUBJECT_LIKED_KEY);
        if (log.isInfoEnabled()) {
            log.info("syncLiked.subjectLikedMap:{}", JSON.toJSONString(subjectLikedMap));
        }
        if (MapUtils.isEmpty(subjectLikedMap)) {
            return;
        }
        //批量同步到数据库
        List<SubjectLiked> subjectLikedList = new ArrayList<>();
        subjectLikedMap.forEach((key, val) -> {
            SubjectLiked subjectLiked = new SubjectLiked();
            String[] keyArr = key.toString().split(":");
            String subjectId = keyArr[0];
            String likedUser = keyArr[1];
            subjectLiked.setSubjectId(Long.valueOf(subjectId));
            subjectLiked.setLikeUserId(likedUser);
            subjectLiked.setStatus(Integer.valueOf(val.toString()));
            subjectLiked.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
            subjectLikedList.add(subjectLiked);
        });
        subjectLikedService.batchInsertOrUpdate(subjectLikedList);
    }

    /**
     * 分页查询点赞信息
     */
    @Override
    public PageResult<SubjectLikedBO> getSubjectLikedPage(SubjectLikedBO subjectLikedBO) {
        PageResult<SubjectLikedBO> pageResult = new PageResult<>();
        pageResult.setPageNo(subjectLikedBO.getPageNo());
        pageResult.setPageSize(subjectLikedBO.getPageSize());
        int start = (subjectLikedBO.getPageNo() - 1) * subjectLikedBO.getPageSize();
        SubjectLiked subjectLiked = SubjectLikedBOConverter.INSTANCE.convertBOToEntity(subjectLikedBO);
        subjectLiked.setLikeUserId(LoginUtil.getLoginId());
        int count = subjectLikedService.countByCondition(subjectLiked);
        if (count == 0) {
            return pageResult;
        }
        List<SubjectLiked> subjectLikedList = subjectLikedService.queryPage(subjectLiked, start,
                subjectLikedBO.getPageSize());
        List<SubjectLikedBO> subjectInfoBOS = SubjectLikedBOConverter.INSTANCE.convertListInfoToBO(subjectLikedList);
        subjectInfoBOS.forEach(info -> {
            SubjectInfo subjectInfo = subjectInfoService.queryById(info.getSubjectId());
            info.setSubjectName(subjectInfo.getSubjectName());
        });
        pageResult.setRecords(subjectInfoBOS);
        pageResult.setTotal(count);
        return pageResult;
    }

    /**
     * mq消费者存储点赞到mysql
     */
    @Override
    public void syncLikedByMsg(SubjectLikedBO subjectLikedBO) {
        List<SubjectLiked> subjectLikedList = new ArrayList<>();
        SubjectLiked subjectLiked = new SubjectLiked();
        subjectLiked.setSubjectId(Long.valueOf(subjectLikedBO.getSubjectId()));
        subjectLiked.setLikeUserId(subjectLikedBO.getLikeUserId());
        subjectLiked.setStatus(subjectLikedBO.getStatus());
        subjectLiked.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectLikedList.add(subjectLiked);
        subjectLikedService.batchInsertOrUpdate(subjectLikedList);
    }

}
