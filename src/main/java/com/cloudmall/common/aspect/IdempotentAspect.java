package com.cloudmall.common.aspect;

import com.cloudmall.common.annotation.Idempotent;
import com.cloudmall.common.exception.BusinessException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Objects;

/**
 * 幂等性AOP切面
 *
 * 流程: 拦截@Idempotent方法 → 从请求参数取idempotentKey → Redis SETNX → 成功放行/失败抛异常
 * 关键: Redis SETNX是原子操作(key不存在才set)，天然支持分布式幂等
 */
@Aspect
@Component
public class IdempotentAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(com.cloudmall.common.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        // 获取请求中的idempotentKey
        String key = getIdempotentKey(idempotent);
        if (key == null || key.isEmpty()) {
            // 没有传幂等key，不拦截（兼容旧请求）
            return pjp.proceed();
        }

        String redisKey = "idempotent:" + (idempotent.key().isEmpty()
                ? method.getDeclaringClass().getSimpleName() + ":" + method.getName()
                : idempotent.key()) + ":" + key;

        // Redis SETNX: 成功=true(首次请求), 失败=false(重复请求)
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofSeconds(idempotent.ttl()));

        if (Boolean.FALSE.equals(success)) {
            throw new BusinessException("请勿重复提交");
        }

        return pjp.proceed();
    }

    /** 从请求参数中提取idempotentKey */
    private String getIdempotentKey(Idempotent idempotent) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;

        HttpServletRequest request = attrs.getRequest();
        String key = request.getParameter(idempotent.paramName());
        if (key != null && !key.isEmpty()) return key;

        // GET用query string, POST/PUT用body — 简单起见都用parameter
        return key;
    }
}
