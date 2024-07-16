package com.zhukew.subject.infra.config;

import com.zhukew.subject.common.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 填充createBy,createTime等公共字段的拦截器
 *
 * @author: Wei
 * @date: 2024/1/5
 */
@Component
@Slf4j
@Intercepts({@Signature(type = Executor.class, method = "update", args = {
        MappedStatement.class, Object.class
})})
public class MybatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 拿到sql类型
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        // 拿到sql的对象参数
        Object parameter = invocation.getArgs()[1];
        if (parameter == null) {
            return invocation.proceed();
        }
        //获取当前登录用户的id
        String loginId = LoginUtil.getLoginId();
        if (StringUtils.isBlank(loginId)) {
            return invocation.proceed();
        }

        // 对更新和插入的sql请求进行处理
        if (SqlCommandType.INSERT == sqlCommandType || SqlCommandType.UPDATE == sqlCommandType) {
            replaceEntityProperty(parameter, loginId, sqlCommandType);
        }
        return invocation.proceed();
    }

    /**
     * 更新sql参数
     */
    private void replaceEntityProperty(Object parameter, String loginId, SqlCommandType sqlCommandType) {
        if (parameter instanceof Map) {
            replaceMap((Map) parameter, loginId, sqlCommandType);
        } else {
            replace(parameter, loginId, sqlCommandType);
        }
    }

    /**
     * 更新sql map类型参数
     */
    private void replaceMap(Map parameter, String loginId, SqlCommandType sqlCommandType) {
        for (Object val : parameter.values()) {
            replace(val, loginId, sqlCommandType);
        }
    }

    /**
     * 更新sql对象类型参数
     */
    private void replace(Object parameter, String loginId, SqlCommandType sqlCommandType) {
        if (SqlCommandType.INSERT == sqlCommandType) {
            dealInsert(parameter, loginId);
        } else {
            dealUpdate(parameter, loginId);
        }
    }

    /**
     * 处理更新请求
     */
    private void dealUpdate(Object parameter, String loginId) {
        Field[] fields = getAllFields(parameter);
        // 遍历所有字段，并对目标字段进行处理
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object o = field.get(parameter);
                if (Objects.nonNull(o)) {
                    field.setAccessible(false);
                    continue;
                }
                if ("updateBy".equals(field.getName())) {
                    field.set(parameter, loginId);
                } else if ("updateTime".equals(field.getName())) {
                    field.set(parameter, new Date());
                }
                field.setAccessible(false);
            } catch (Exception e) {
                log.error("dealUpdate.error:{}", e.getMessage(), e);
            }
        }
    }
    /**
     * 处理插入请求
     */
    private void dealInsert(Object parameter, String loginId) {
        Field[] fields = getAllFields(parameter);
        // 遍历所有字段，并对目标字段进行处理
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object o = field.get(parameter);
                if (Objects.nonNull(o)) {
                    field.setAccessible(false);
                    continue;
                }
                if ("isDeleted".equals(field.getName())) {
                    field.set(parameter, 0);
                } else if ("createdBy".equals(field.getName())) {
                    field.set(parameter, loginId);
                } else if ("createdTime".equals(field.getName())) {
                    field.set(parameter, new Date());
                }
                field.setAccessible(false);
            } catch (Exception e) {
                log.error("dealInsert.error:{}", e.getMessage(), e);
            }
        }
    }

    /**
     * 反射获取对象参数的所有字段
     */
    private Field[] getAllFields(Object object) {
        Class<?> clazz = object.getClass();
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
