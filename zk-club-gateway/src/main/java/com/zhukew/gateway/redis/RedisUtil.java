package com.zhukew.gateway.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RedisUtil工具类
 *
 * @author: Wei
 * @date: 2023/10/28
 */
@Component
@Slf4j
public class RedisUtil {

    /**
     * 自定义的redisTemplate
     */
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 缓存key分隔符
     */
    private static final String CACHE_KEY_SEPARATOR = ".";

    /**
     * 封装缓存key
     */
    public String buildKey(String... strObjs) {
        return Stream.of(strObjs).collect(Collectors.joining(CACHE_KEY_SEPARATOR));
    }

    /**
     * 是否存在key
     */
    public boolean exist(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除key
     */
    public boolean del(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * set(不带过期)
     */
    public void set(String key, String value) {
        // opsForValue: 操作 String 类型
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * set(带过期)
     * 对应 redix 命令: SETNX (set if not exists)
     */
    public boolean setNx(String key, String value, Long time, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, time, timeUnit);
    }

    /**
     * 获取string类型缓存
     */
    public String get(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * 插入zset
     * 对应 redix 命令: ZADD
     */
    public Boolean zAdd(String key, String value, Long score) {
        return redisTemplate.opsForZSet().add(key, value, Double.valueOf(String.valueOf(score)));
    }

    /**
     * zset 大小
     * 对应 redix 命令: ZCARD
     */
    public Long countZset(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 根据位置获取 zset中 元素
     * 对应 redix 命令: ZRANGE
     */
    public Set<String> rangeZset(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 移除 zset 中指定元素
     * 对应 redix 命令: ZREM
     */
    public Long removeZset(String key, Object value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * 批量删除 zset 中指定元素
     */
    public void removeZsetList(String key, Set<String> value) {
        value.stream().forEach((val) -> redisTemplate.opsForZSet().remove(key, val));
    }

    /**
     * 获取 zset 中指定元素的 score
     * 对应 redix 命令: ZSCORE
     */
    public Double score(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 获取 zset 中分数在指定范围内的元素
     * 对应 Redis 命令：ZRANGEBYSCORE
     */
    public Set<String> rangeByScore(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeByScore(key, Double.valueOf(String.valueOf(start)), Double.valueOf(String.valueOf(end)));
    }

    /**
     * 为 zset 中元素增加指定 score
     * 对应 redix 命令: ZINCRBY
     */
    public Object addScore(String key, Object obj, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, obj, score);
    }

    /**
     * 获取 zset 中指定成员 排名
     * 对应 Redis 命令：ZRANK
     */
    public Object rank(String key, Object obj) {
        return redisTemplate.opsForZSet().rank(key, obj);
    }

    /**
     * 以分数从高到低的顺序获取 zset 中的元素及其分数
     * 对应 Redis 命令：ZRANK
     */
    public Set<ZSetOperations.TypedTuple<String>> rankWithScore(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        return set;
    }

    /**
     * 向 hash 中插入元素
     */
    public void putHash(String key, String hashKey, Object hashVal) {
        redisTemplate.opsForHash().put(key, hashKey, hashVal);
    }

    /**
     * 获取指定 key 中存储的数字值
     */
    public Integer getInt(String key) {
        return (Integer) redisTemplate.opsForValue().get(key);
    }

    /**
     * 将指定 key 中储存的数字值增一
     * 对应 Redis 命令: INCR
     */
    public void increment(String key, Integer count) {
        redisTemplate.opsForValue().increment(key, count);
    }


}
