package com.cloudmall.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性注解 — 防止重复提交（重复下单/重复支付）
 *
 * 原理: 前端生成idempotentKey(UUID) → 后端Redis SETNX → 成功放行/重复拒绝
 * TTL默认10秒，10秒后自动过期（允许重试）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /** 幂等key前缀 */
    String key() default "";

    /** 幂等key有效期（秒） */
    long ttl() default 10;

    /** 从请求参数中取idempotentKey的参数名 */
    String paramName() default "idempotentKey";
}
