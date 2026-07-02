package com.cloudmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 数据统计服务 — 看板数据 / 预计算 / Redis计数器
 */
@Service
public class StatisticsService {

    @Resource private StatDailyMapper statDailyMapper;
    @Resource private OrderMapper orderMapper;
    @Resource private UserMapper userMapper;
    @Resource private RedisTemplate<String, Object> redisTemplate;
    @Resource private CouponTemplateMapper couponTemplateMapper;
    @Resource private UserCouponMapper userCouponMapper;
    @Resource private SeckillSessionMapper seckillSessionMapper;
    @Resource private GroupBuyMapper groupBuyMapper;
    @Resource private ActivityMapper activityMapper;
    @Resource private ReviewMapper reviewMapper;
    @Resource private ProductMapper productMapper;
    @Resource private OrderItemMapper orderItemMapper;
    @Resource private CategoryMapper categoryMapper;
    @Resource private SearchHotWordMapper searchHotWordMapper;

    /** 今日实时数据（Redis计数器） */
    public Map<String, Object> todaySnapshot() {
        String today = LocalDate.now().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("orderCount", getRedisCounter("stat:" + today + ":orders"));
        data.put("payAmount", getRedisCounter("stat:" + today + ":amount"));
        data.put("newUsers", getRedisCounter("stat:" + today + ":users"));
        return data;
    }

    private long getRedisCounter(String key) {
        Object v = redisTemplate.opsForValue().get(key);
        return v != null ? Long.parseLong(v.toString()) : 0;
    }

    /** 下单时增加Redis计数器（异步调用） */
    public void incrementOrderStats() {
        String today = LocalDate.now().toString();
        redisTemplate.opsForValue().increment("stat:" + today + ":orders");
    }

    /** 新增用户时增加计数器 */
    public void incrementUserStats() {
        String today = LocalDate.now().toString();
        redisTemplate.opsForValue().increment("stat:" + today + ":users");
    }

    /** 每日统计数据 */
    public StatDaily getDailyStat(LocalDate date) {
        List<StatDaily> list = statDailyMapper.selectList(
                new LambdaQueryWrapper<StatDaily>().eq(StatDaily::getStatDate, date));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 近7天统计 */
    public List<StatDaily> weekStats() {
        LocalDate today = LocalDate.now();
        return statDailyMapper.selectList(
                new LambdaQueryWrapper<StatDaily>()
                        .ge(StatDaily::getStatDate, today.minusDays(7))
                        .orderByAsc(StatDaily::getStatDate));
    }

    /** 近30天统计 */
    public List<StatDaily> monthStats() {
        LocalDate today = LocalDate.now();
        return statDailyMapper.selectList(
                new LambdaQueryWrapper<StatDaily>()
                        .ge(StatDaily::getStatDate, today.minusDays(30))
                        .orderByAsc(StatDaily::getStatDate));
    }

    /** 订单状态分布 */
    public Map<Integer, Long> orderStatusDistribution() {
        List<Order> orders = orderMapper.selectList(null);
        Map<Integer, Long> dist = new LinkedHashMap<>();
        for (Order o : orders) {
            dist.merge(o.getStatus(), 1L, Long::sum);
        }
        return dist;
    }

    /** 用户画像 */
    public Map<String, Object> userProfile() {
        Map<String, Object> result = new HashMap<>();

        // 消费层级分布（累计消费金额分组）
        List<User> users = userMapper.selectList(null);
        int highSpenders = 0, midSpenders = 0, lowSpenders = 0;
        for (User u : users) {
            Long totalSpent = u.getTotalSpent() != null ? u.getTotalSpent() : 0L;
            if (totalSpent > 500000) highSpenders++;
            else if (totalSpent > 100000) midSpenders++;
            else lowSpenders++;
        }
        Map<String, Integer> spendTier = new HashMap<>();
        spendTier.put("高消费(>5000元)", highSpenders);
        spendTier.put("中消费(1000-5000元)", midSpenders);
        spendTier.put("低消费(<1000元)", lowSpenders);
        result.put("spendTier", spendTier);

        // 品类偏好Top5（按订单商品关联）
        List<OrderItem> allItems = orderItemMapper.selectList(null);
        Map<Long, Integer> catCount = new HashMap<>();
        for (OrderItem oi : allItems) {
            if (oi.getProductId() != null) {
                Product p = productMapper.selectById(oi.getProductId());
                if (p != null && p.getCategoryId() != null) {
                    catCount.merge(p.getCategoryId(), oi.getQuantity() != null ? oi.getQuantity() : 1, Integer::sum);
                }
            }
        }
        List<Map<String, Object>> favCats = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : catCount.entrySet()) {
            Category cat = categoryMapper.selectById(e.getKey());
            Map<String, Object> m = new HashMap<>();
            m.put("categoryId", e.getKey());
            m.put("categoryName", cat != null ? cat.getName() : "未知");
            m.put("soldCount", e.getValue());
            favCats.add(m);
        }
        favCats.sort((a, b) -> ((Integer) b.get("soldCount")).compareTo((Integer) a.get("soldCount")));
        result.put("favCategories", favCats.size() > 5 ? favCats.subList(0, 5) : favCats);

        // 复购率：有≥2单的用户占比
        Map<Long, Integer> userOrderCount = new HashMap<>();
        for (Order o : orderMapper.selectList(null)) {
            userOrderCount.merge(o.getUserId(), 1, Integer::sum);
        }
        long repeatBuyers = userOrderCount.values().stream().filter(c -> c >= 2).count();
        double repurchaseRate = userOrderCount.isEmpty() ? 0 :
            Math.round(repeatBuyers * 10000.0 / userOrderCount.size()) / 100.0;
        result.put("repurchaseRate", repurchaseRate);
        result.put("totalUsersWithOrders", userOrderCount.size());

        // 客单价分布
        Map<String, Integer> avgOrderTier = new LinkedHashMap<>();
        avgOrderTier.put("0-100元", 0); avgOrderTier.put("100-500元", 0);
        avgOrderTier.put("500-1000元", 0); avgOrderTier.put("1000元以上", 0);
        for (Order o : orderMapper.selectList(null)) {
            int amt = o.getPayAmount() != null ? o.getPayAmount() : 0;
            if (amt < 10000) avgOrderTier.merge("0-100元", 1, Integer::sum);
            else if (amt < 50000) avgOrderTier.merge("100-500元", 1, Integer::sum);
            else if (amt < 100000) avgOrderTier.merge("500-1000元", 1, Integer::sum);
            else avgOrderTier.merge("1000元以上", 1, Integer::sum);
        }
        result.put("avgOrderTier", avgOrderTier);

        return result;
    }

    /** 营销效果统计 */
    public Map<String, Object> marketingStats() {
        Map<String, Object> result = new HashMap<>();

        // 优惠券统计
        List<CouponTemplate> templates = couponTemplateMapper.selectList(null);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(null);
        long totalIssued = templates.stream().mapToLong(t -> t.getTotalCount() != null ? t.getTotalCount() : 0).sum();
        long totalReceived = userCoupons.size();
        long totalUsed = userCoupons.stream().filter(c -> c.getStatus() != null && c.getStatus() == 1).count();
        Map<String, Object> couponStats = new HashMap<>();
        couponStats.put("totalIssued", totalIssued);
        couponStats.put("totalReceived", totalReceived);
        couponStats.put("totalUsed", totalUsed);
        couponStats.put("usageRate", totalIssued > 0 ? Math.round(totalUsed * 10000.0 / totalIssued) / 100.0 : 0);
        result.put("coupon", couponStats);

        // 秒杀统计
        List<SeckillSession> seckills = seckillSessionMapper.selectList(null);
        int seckillTotalStock = 0, seckillSold = 0;
        for (SeckillSession s : seckills) {
            if (s.getTotalStock() != null) seckillTotalStock += s.getTotalStock();
            if (s.getSoldCount() != null) seckillSold += s.getSoldCount();
        }
        Map<String, Object> seckillStats = new HashMap<>();
        seckillStats.put("sessionCount", seckills.size());
        seckillStats.put("totalStock", seckillTotalStock);
        seckillStats.put("soldCount", seckillSold);
        seckillStats.put("sellThroughRate", seckillTotalStock > 0 ?
            Math.round(seckillSold * 10000.0 / seckillTotalStock) / 100.0 : 0);
        result.put("seckill", seckillStats);

        // 拼团统计
        List<GroupBuy> groups = groupBuyMapper.selectList(null);
        long successGroups = groups.stream().filter(g -> g.getStatus() != null && g.getStatus() == 2).count();
        long failGroups = groups.stream().filter(g -> g.getStatus() != null && g.getStatus() == 3).count();
        Map<String, Object> groupStats = new HashMap<>();
        groupStats.put("totalGroups", groups.size());
        groupStats.put("successCount", successGroups);
        groupStats.put("failCount", failGroups);
        groupStats.put("successRate", groups.size() > 0 ?
            Math.round(successGroups * 10000.0 / groups.size()) / 100.0 : 0);
        result.put("groupBuy", groupStats);

        // 活动统计（进行中/已结束的活动数）
        List<Activity> activities = activityMapper.selectList(null);
        long activeActivities = activities.stream().filter(a -> a.getStatus() != null && a.getStatus() == 2).count();
        Map<String, Object> activityStats = new HashMap<>();
        activityStats.put("totalActivities", activities.size());
        activityStats.put("activeCount", activeActivities);
        result.put("activity", activityStats);

        return result;
    }

    /** 流量来源分析 */
    public Map<String, Object> trafficAnalysis() {
        Map<String, Object> result = new HashMap<>();

        // 近7天PV/UV趋势
        List<StatDaily> weekStats = statDailyMapper.selectList(
            new LambdaQueryWrapper<StatDaily>()
                .ge(StatDaily::getStatDate, LocalDate.now().minusDays(7))
                .orderByAsc(StatDaily::getStatDate));
        result.put("weekTraffic", weekStats);

        // 热门搜索词Top10
        List<SearchHotWord> hotWords = searchHotWordMapper.selectList(
            new LambdaQueryWrapper<SearchHotWord>()
                .orderByDesc(SearchHotWord::getSearchCount)
                .last("LIMIT 10"));
        result.put("hotWords", hotWords);

        // 商品访问排行Top10（按销量）
        List<Product> topProducts = productMapper.selectList(
            new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getSalesCount)
                .last("LIMIT 10"));
        List<Map<String, Object>> productRank = new ArrayList<>();
        for (Product p : topProducts) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("salesCount", p.getSalesCount() != null ? p.getSalesCount() : 0);
            productRank.add(m);
        }
        result.put("topProducts", productRank);

        return result;
    }
}
