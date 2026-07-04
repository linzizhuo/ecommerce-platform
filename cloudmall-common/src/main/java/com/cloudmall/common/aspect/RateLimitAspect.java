package com.cloudmall.common.aspect;

import com.cloudmall.common.annotation.RateLimit;
import com.cloudmall.common.exception.BusinessException;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 限流AOP切面
 * 每个 @RateLimit 注解的方法 → 自动生成一个 Redis 令牌桶
 */
@Aspect
@Component
public class RateLimitAspect {

    @Resource
    private RedissonClient redissonClient;

    @Around("@annotation(com.cloudmall.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 生成限流 key
        String key = rateLimit.key();
        if (key.isEmpty()) {
            key = method.getDeclaringClass().getSimpleName() + ":" + method.getName();
        }

        // 获取或创建令牌桶
        RRateLimiter limiter = redissonClient.getRateLimiter("ratelimit:" + key);
        limiter.trySetRate(RateType.OVERALL, rateLimit.qps(), 1, RateIntervalUnit.SECONDS);

        // 尝试获取令牌
        if (!limiter.tryAcquire(rateLimit.timeout(), rateLimit.timeUnit())) {
            throw new BusinessException("请求过于频繁，请稍后再试");
        }

        return pjp.proceed();
    }
}
