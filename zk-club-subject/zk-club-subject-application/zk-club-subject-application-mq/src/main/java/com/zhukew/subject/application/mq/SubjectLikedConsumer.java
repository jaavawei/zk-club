package com.zhukew.subject.application.mq;

import com.alibaba.fastjson.JSON;
import com.zhukew.subject.domain.convert.SubjectLikedBOConverter;
import com.zhukew.subject.domain.entity.SubjectLikedBO;
import com.zhukew.subject.domain.entity.SubjectLikedMessage;
import com.zhukew.subject.domain.redis.RedisUtil;
import com.zhukew.subject.domain.service.SubjectLikedDomainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * RocketMq消费者：异步持久化点赞信息到数据库
 *
 * @auther: Wei
 */
@Component
@RocketMQMessageListener(topic = "subject-liked", consumerGroup = "subject-liked-consumer")
@Slf4j
public class SubjectLikedConsumer implements RocketMQListener<String> {

    @Resource
    private SubjectLikedDomainService subjectLikedDomainService;

    @Resource
    private RedisUtil redisUtil;

    public final String MESSAGE_DONES = "message-dones";

    @Override
    public void onMessage(String s) {
        SubjectLikedMessage subjectLikedMessage = JSON.parseObject(s, SubjectLikedMessage.class);
        if (subjectLikedMessage == null) {
            log.error("Message format error, cannot parse to subjectLikedMessage: {}", subjectLikedMessage);
            return;
        }
        String id = subjectLikedMessage.getId();
        // 判断该条消息是否处理过
        if(redisUtil.isSetMember(MESSAGE_DONES, id)) {
            return;
        }
        redisUtil.addSet(MESSAGE_DONES, id);
        SubjectLikedBO subjectLikedBO = SubjectLikedBOConverter.INSTANCE.convertMessageToBO(subjectLikedMessage);
        subjectLikedDomainService.syncLikedByMsg(subjectLikedBO);
    }

}

