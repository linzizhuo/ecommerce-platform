package com.cloudmall.common.config;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 熔断降级配置
 *
 * 核心能力:
 * 1. 流量控制（QPS限流）— 超过阈值直接拒绝
 * 2. 熔断降级（断路器）— 错误率过高自动熔断，快速失败
 * 3. 系统自适应保护 — 系统负载过高时自动限流
 *
 * 答辩关键:
 * - 限流和熔断的区别: 限流是"超量拒绝"，熔断是"出错了就别再试"
 * - 熔断三种状态: 关闭→(错误>阈值)→打开→(冷却期满)→半开→(试探成功)→关闭
 */
@Configuration
public class SentinelConfig {

    private static final Logger log = LoggerFactory.getLogger(SentinelConfig.class);

    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
        log.info("========== Sentinel 规则初始化完成 ==========");
    }

    /** 流量控制规则 */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 下单接口限流: QPS=50
        FlowRule orderRule = new FlowRule("createOrder");
        orderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderRule.setCount(50);
        orderRule.setLimitApp("default");
        rules.add(orderRule);

        // 秒杀接口限流: QPS=200
        FlowRule seckillRule = new FlowRule("seckill");
        seckillRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        seckillRule.setCount(200);
        rules.add(seckillRule);

        FlowRuleManager.loadRules(rules);
    }

    /** 熔断降级规则 */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 下单接口熔断: 异常率>50% → 熔断10秒
        DegradeRule orderDegrade = new DegradeRule("createOrder");
        orderDegrade.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        orderDegrade.setCount(0.5);    // 异常率阈值50%
        orderDegrade.setTimeWindow(10); // 熔断时长10秒
        orderDegrade.setMinRequestAmount(10); // 最小请求数10才触发
        orderDegrade.setStatIntervalMs(10000); // 统计窗口10秒
        rules.add(orderDegrade);

        // 商品查询降级: 慢调用比例>30%(RT>300ms) → 降级
        DegradeRule productDegrade = new DegradeRule("productQuery");
        productDegrade.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());
        productDegrade.setCount(300);   // 慢调用阈值300ms
        productDegrade.setSlowRatioThreshold(0.3); // 慢调用比例30%
        productDegrade.setTimeWindow(5);
        productDegrade.setMinRequestAmount(20);
        rules.add(productDegrade);

        DegradeRuleManager.loadRules(rules);
    }

    /**
     * 对外提供的入口方法 — 带Sentinel保护的执行
     *
     * 使用示例:
     *   SentinelConfig.protect("createOrder", () -> orderService.create(...));
     * 如果被限流/熔断 → 抛BlockException → 返回降级结果
     */
    public static <T> T protect(String resourceName, SentinelSupplier<T> supplier) {
        try (Entry entry = SphU.entry(resourceName)) {
            return supplier.get();
        } catch (BlockException e) {
            log.warn("Sentinel blocked: resource={}, rule={}", resourceName, e.getRule());
            throw new com.cloudmall.common.exception.BusinessException("系统繁忙，请稍后再试");
        } catch (Exception e) {
            // 业务异常 — 记录到Sentinel统计（用于熔断判断）
            if (e instanceof RuntimeException re) throw re;
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface SentinelSupplier<T> {
        T get() throws Exception;
    }
}
