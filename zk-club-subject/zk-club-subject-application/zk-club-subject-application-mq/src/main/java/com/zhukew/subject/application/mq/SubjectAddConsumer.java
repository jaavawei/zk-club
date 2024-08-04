package com.zhukew.subject.application.mq;

import com.alibaba.fastjson.JSON;
import com.zhukew.subject.domain.entity.SubjectInfoBO;
import com.zhukew.subject.domain.entity.SubjectLikedBO;
import com.zhukew.subject.domain.redis.RedisUtil;
import com.zhukew.subject.domain.service.SubjectInfoDomainService;
import com.zhukew.subject.domain.service.SubjectLikedDomainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * mq消费者：异步更新ES和redis
 *
 * @auther: Wei
 */
@Component
@RocketMQMessageListener(topic = "subject-add", consumerGroup = "subject-add-consumer")
@Slf4j
public class SubjectAddConsumer implements RocketMQListener<Message> {

    @Resource
    private SubjectInfoDomainService subjectInfoDomainService;

    @Resource
    private RedisUtil redisUtil;

    public final String MESSAGE_DONES = "message-dones";

    @Override
    public void onMessage(Message message) {
        String msg = new String(message.getBody());
        String id = message.getKeys();
        // 判断该条消息是否处理过
        if(redisUtil.isSetMember(MESSAGE_DONES, id)) {
            return;
        }
        redisUtil.addSet(MESSAGE_DONES, id);
        if (log.isInfoEnabled()) {
            log.info("Received message: {}", msg);
        }

        SubjectInfoBO subjectInfoBO = JSON.parseObject(msg, SubjectInfoBO.class);
        if (subjectInfoBO == null) {
            log.error("Message format error, cannot parse to SubjectInfoBO: {}", msg);
            return;
        }
        subjectInfoDomainService.syncAddByMsg(subjectInfoBO);
    }
}

