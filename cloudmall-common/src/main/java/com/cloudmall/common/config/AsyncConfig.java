package com.cloudmall.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 *
 * 核心思想：下单后发短信、更新销量统计这些非关键路径
 * 扔进线程池异步执行，别让用户干等着
 */
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean("orderExecutor")
    public ThreadPoolTaskExecutor orderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数 — 平时就开着等活
        executor.setCorePoolSize(10);
        // 最大线程数 — 高峰期扩容上限
        executor.setMaxPoolSize(50);
        // 队列容量 — 1000个任务排队
        executor.setQueueCapacity(1000);
        // 线程名前缀 — 方便排查问题
        executor.setThreadNamePrefix("order-async-");
        // 拒绝策略: 队列满了让调用线程自己执行（保证不丢任务）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程空闲60秒后回收（非核心线程）
        executor.setKeepAliveSeconds(60);
        // 允许核心线程超时回收
        executor.setAllowCoreThreadTimeOut(false);
        executor.initialize();
        return executor;
    }
}
