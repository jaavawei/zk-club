package com.zhukew.subject.common.context;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录上下文对象
 *
 * @author: Wei
 * @date: 2023/11/26
 */
public class LoginContextHolder {

    /**
     * 用 ThreadLocal 记录上下文数据
     */
    private static final InheritableThreadLocal<Map<String, Object>> THREAD_LOCAL
            = new InheritableThreadLocal<>();

    /**
     * 存储上下文数据
     */
    public static void set(String key, Object val) {
        Map<String, Object> map = getThreadLocalMap();
        map.put(key, val);
    }

    /**
     * 读取上下文数据
     */
    public static Object get(String key){
        Map<String, Object> threadLocalMap = getThreadLocalMap();
        return threadLocalMap.get(key);
    }

    /**
     * 读取当前用户唯一id
     */
    public static String getLoginId(){
        return (String) getThreadLocalMap().get("loginId");
    }

    /**
     * 移除所有数据
     */
    public static void remove(){
        THREAD_LOCAL.remove();
    }

    /**
     * 从 ThreadLocal 中取出 map
     */
    public static Map<String, Object> getThreadLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (Objects.isNull(map)) {
            map = new ConcurrentHashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }


}
