package com.cloudmall.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解 — 基于 Redisson RRateLimiter
 * 用于防止恶意刷接口、突发流量保护
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 限流key前缀，默认用类名+方法名 */
    String key() default "";

    /** 每秒允许的请求数 */
    long qps() default 100;

    /** 获取许可的等待时间 */
    long timeout() default 1;

    /** 时间单位 */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
