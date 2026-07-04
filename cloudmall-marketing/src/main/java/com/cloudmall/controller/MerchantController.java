package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import com.cloudmall.service.impl.SeckillService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    @Resource private ProductMapper productMapper;
    @Resource private SkuMapper skuMapper;
    @Resource private OrderMapper orderMapper;
    @Resource private OrderItemMapper orderItemMapper;
    @Resource private PaymentMapper paymentMapper;
    @Resource private PresaleMapper presaleMapper;
    @Resource private ComboPackageMapper comboPackageMapper;
    @Resource private ComboItemMapper comboItemMapper;
    @Resource private DistributionMapper distributionMapper;
    @Resource private ActivityMapper activityMapper;
    @Resource private StatDailyMapper statDailyMapper;
    @Resource private GroupBuyMapper groupBuyMapper;
    @Resource private AfterSaleMapper afterSaleMapper;
    @Resource private SeckillSessionMapper seckillSessionMapper;
    @Resource private SeckillService seckillService;

    /** 商家商品列表 */
    @GetMapping("/products")
    public R<List<Product>> products() {
        return R.ok(productMapper.selectList(
            new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreateTime)));
    }

    /** 保存商品(发布/编辑) */
    @PostMapping("/product")
    public R<Void> saveProduct(@RequestBody Map<String, Object> body) {
        Product p = new Product();
        p.setName((String) body.get("name"));
        p.setBrand((String) body.get("brand"));
        p.setDescription((String) body.get("description"));
        p.setCategoryId(body.get("categoryId") != null ? Long.valueOf(body.get("categoryId").toString()) : null);
        p.setStatus(2); // 直接上架
        p.setCreateTime(LocalDateTime.now());
        productMapper.insert(p);
        // SKU
        List<Map<String, Object>> skus = (List<Map<String, Object>>) body.get("skus");
        if (skus != null) {
            for (Map<String, Object> s : skus) {
                Sku sku = new Sku();
                sku.setProductId(p.getId());
                sku.setSpecInfo((String) s.get("specInfo"));
                sku.setPrice(Integer.parseInt(s.get("price").toString()));
                sku.setOriginalPrice(s.get("originalPrice") != null ? Integer.parseInt(s.get("originalPrice").toString()) : sku.getPrice());
                sku.setStock(s.get("stock") != null ? Integer.parseInt(s.get("stock").toString()) : 0);
                skuMapper.insert(sku);
            }
        }
        return R.ok();
    }

    /** 删除商品 */
    @DeleteMapping("/product/{id}")
    public R<Void> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id);
        return R.ok();
    }

    /** 商家订单列表 */
    @GetMapping("/orders")
    public R<List<Map<String, Object>>> orders() {
        List<Order> orders = orderMapper.selectList(
            new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("payAmount", o.getPayAmount());
            m.put("status", o.getStatus());
            m.put("createTime", o.getCreateTime());
            m.put("items", orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId())));
            result.add(m);
        }
        return R.ok(result);
    }


    // ===== 预售管理 =====
    @GetMapping("/presale/list")
    public R<List<Presale>> presaleList() {
        return R.ok(presaleMapper.selectList(
            new LambdaQueryWrapper<Presale>().orderByDesc(Presale::getCreateTime)));
    }

    @PostMapping("/presale")
    public R<Void> createPresale(@RequestBody Map<String, Object> body) {
        Presale p = new Presale();
        p.setSkuId(Long.valueOf(body.get("skuId").toString()));
        p.setDeposit(Integer.parseInt(body.get("deposit").toString()));
        p.setFinalAmount(Integer.parseInt(body.get("finalAmount").toString()));
        p.setDepositStart(LocalDateTime.parse((String) body.get("depositStart"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        p.setDepositEnd(LocalDateTime.parse((String) body.get("depositEnd"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        p.setFinalStart(LocalDateTime.parse((String) body.get("finalStart"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        p.setFinalEnd(LocalDateTime.parse((String) body.get("finalEnd"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        p.setStatus(1);
        p.setCreateTime(LocalDateTime.now());
        presaleMapper.insert(p);
        return R.ok();
    }

    @PutMapping("/presale/{id}")
    public R<Void> updatePresale(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Presale p = presaleMapper.selectById(id);
        if (p != null) {
            if (body.containsKey("deposit")) p.setDeposit(Integer.parseInt(body.get("deposit").toString()));
            if (body.containsKey("finalAmount")) p.setFinalAmount(Integer.parseInt(body.get("finalAmount").toString()));
            if (body.containsKey("status")) p.setStatus((Integer) body.get("status"));
            presaleMapper.updateById(p);
        }
        return R.ok();
    }

    @DeleteMapping("/presale/{id}")
    public R<Void> deletePresale(@PathVariable Long id) {
        presaleMapper.deleteById(id);
        return R.ok();
    }

    // ===== 套餐管理 =====
    @GetMapping("/combo/list")
    public R<List<Map<String, Object>>> comboList() {
        List<ComboPackage> list = comboPackageMapper.selectList(
            new LambdaQueryWrapper<ComboPackage>().orderByDesc(ComboPackage::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ComboPackage cp : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", cp.getId());
            m.put("name", cp.getName());
            m.put("description", cp.getDescription());
            m.put("totalPrice", cp.getTotalPrice());
            m.put("discountPrice", cp.getOriginalPrice());
            m.put("status", cp.getStatus());
            m.put("items", comboItemMapper.selectList(
                new LambdaQueryWrapper<ComboItem>().eq(ComboItem::getComboId, cp.getId())));
            result.add(m);
        }
        return R.ok(result);
    }

    @PostMapping("/combo")
    public R<Void> createCombo(@RequestBody Map<String, Object> body) {
        ComboPackage cp = new ComboPackage();
        cp.setName((String) body.get("name"));
        cp.setDescription((String) body.get("description"));
        cp.setTotalPrice(Integer.parseInt(body.get("totalPrice").toString()));
        cp.setOriginalPrice(Integer.parseInt(body.get("discountPrice").toString()));
        cp.setStatus(1);
        cp.setCreateTime(LocalDateTime.now());
        comboPackageMapper.insert(cp);

        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items != null) {
            for (Map<String, Object> item : items) {
                ComboItem ci = new ComboItem();
                ci.setComboId(cp.getId());
                ci.setSkuId(Long.valueOf(item.get("skuId").toString()));
                ci.setQuantity((Integer) item.getOrDefault("quantity", 1));
                comboItemMapper.insert(ci);
            }
        }
        return R.ok();
    }

    @DeleteMapping("/combo/{id}")
    public R<Void> deleteCombo(@PathVariable Long id) {
        comboItemMapper.delete(new LambdaQueryWrapper<ComboItem>().eq(ComboItem::getComboId, id));
        comboPackageMapper.deleteById(id);
        return R.ok();
    }

    // ===== 分销管理 =====
    @GetMapping("/distribution/list")
    public R<List<Distribution>> distributionList() {
        return R.ok(distributionMapper.selectList(
            new LambdaQueryWrapper<Distribution>().orderByDesc(Distribution::getCreateTime)));
    }

    @GetMapping("/distribution/settings")
    public R<Map<String, Object>> distributionSettings() {
        List<Distribution> list = distributionMapper.selectList(null);
        int totalDistributors = list.size();
        int totalCommission = list.stream()
                .mapToInt(d -> d.getTotalCommission() != null ? d.getTotalCommission() : 0).sum();
        Map<String, Object> data = new HashMap<>();
        data.put("totalDistributors", totalDistributors);
        data.put("totalCommission", totalCommission);
        return R.ok(data);
    }

    // ===== 增强看板（含访客/转化/趋势） =====
    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        List<Product> products = productMapper.selectList(null);
        List<Order> orders = orderMapper.selectList(null);
        long todayOrders = orders.stream().filter(o -> o.getCreateTime() != null &&
            o.getCreateTime().toLocalDate().equals(LocalDateTime.now().toLocalDate())).count();
        int revenue = orders.stream().filter(o -> o.getStatus() != null && o.getStatus() >= 1)
            .mapToInt(o -> o.getPayAmount() != null ? o.getPayAmount() : 0).sum();

        // 近7天访客数据（按日期聚合所有商家）
        List<StatDaily> weekStats = statDailyMapper.selectList(
            new LambdaQueryWrapper<StatDaily>()
                .ge(StatDaily::getStatDate, LocalDate.now().minusDays(7))
                .orderByAsc(StatDaily::getStatDate));
        int totalVisits = weekStats.stream().mapToInt(s -> s.getVisitCount() != null ? s.getVisitCount() : 0).sum();
        int totalVisitors = weekStats.stream().mapToInt(s -> s.getVisitUserCount() != null ? s.getVisitUserCount() : 0).sum();
        double conversionRate = totalVisitors > 0 ? Math.round(todayOrders * 10000.0 / totalVisitors) / 100.0 : 0;

        // 近7天趋势数据
        List<Map<String, Object>> trend = new ArrayList<>();
        for (StatDaily s : weekStats) {
            Map<String, Object> point = new HashMap<>();
            point.put("statDate", s.getStatDate() != null ? s.getStatDate().toString() : "");
            point.put("orderCount", s.getOrderCount() != null ? s.getOrderCount() : 0);
            point.put("payAmount", s.getPayAmount() != null ? s.getPayAmount() : 0);
            point.put("visitCount", s.getVisitCount() != null ? s.getVisitCount() : 0);
            trend.add(point);
        }

        // 库存预警：SKU库存低于10
        List<Sku> lowStockSkus = skuMapper.selectList(
            new LambdaQueryWrapper<Sku>().lt(Sku::getStock, 10));
        int lowStockCount = lowStockSkus.size();

        Map<String, Object> data = new HashMap<>();
        data.put("productCount", products.size());
        data.put("orderCount", orders.size());
        data.put("todayOrders", todayOrders);
        data.put("revenue", revenue);
        data.put("visitCount", totalVisits);
        data.put("visitUserCount", totalVisitors);
        data.put("conversionRate", conversionRate);
        data.put("lowStockCount", lowStockCount);
        data.put("trend", trend);
        return R.ok(data);
    }

    // ===== 对账报表 =====
    @GetMapping("/reconciliation")
    public R<List<Map<String, Object>>> reconciliation(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .ge(Order::getStatus, 1)
                .orderByDesc(Order::getCreateTime);
        if (startDate != null && !startDate.isEmpty()) {
            qw.ge(Order::getPayTime, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            qw.le(Order::getPayTime, LocalDateTime.parse(endDate + "T23:59:59"));
        }
        List<Order> orders = orderMapper.selectList(qw);
        Map<String, Map<String, Object>> dailyMap = new LinkedHashMap<>();
        for (Order o : orders) {
            String dateKey = o.getPayTime() != null ?
                    o.getPayTime().toLocalDate().toString() :
                    o.getCreateTime().toLocalDate().toString();
            dailyMap.computeIfAbsent(dateKey, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("date", k);
                m.put("orderCount", 0);
                m.put("totalAmount", 0);
                return m;
            });
            Map<String, Object> d = dailyMap.get(dateKey);
            d.put("orderCount", (Integer) d.get("orderCount") + 1);
            d.put("totalAmount", (Integer) d.get("totalAmount") +
                    (o.getPayAmount() != null ? o.getPayAmount() : 0));
        }
        return R.ok(new ArrayList<>(dailyMap.values()));
    }

    // ===== 库存报表 =====
    @GetMapping("/stock-report")
    public R<List<Map<String, Object>>> stockReport() {
        List<Product> products = productMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product p : products) {
            List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getProductId, p.getId()));
            int totalStock = skus.stream().mapToInt(s -> s.getStock() != null ? s.getStock() : 0).sum();
            boolean alert = skus.stream().anyMatch(s -> s.getStock() != null && s.getStock() < 10);
            Map<String, Object> m = new HashMap<>();
            m.put("productId", p.getId());
            m.put("productName", p.getName());
            m.put("totalStock", totalStock);
            m.put("skuCount", skus.size());
            m.put("lowStockAlert", alert);
            m.put("skus", skus);
            result.add(m);
        }
        return R.ok(result);
    }

    // ===== 商家活动管理 =====
    @GetMapping("/activities")
    public R<List<Activity>> activities() {
        return R.ok(activityMapper.selectList(
            new LambdaQueryWrapper<Activity>().orderByDesc(Activity::getCreateTime)));
    }

    @PostMapping("/activity")
    public R<Void> createActivity(@RequestBody Map<String, Object> body) {
        Activity a = new Activity();
        a.setName((String) body.get("name"));
        a.setType((Integer) body.get("type"));
        a.setRules((String) body.get("rules"));
        a.setStartTime(LocalDateTime.parse((String) body.get("startTime"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        a.setEndTime(LocalDateTime.parse((String) body.get("endTime"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        a.setStatus(1);
        a.setCreateTime(LocalDateTime.now());
        activityMapper.insert(a);
        return R.ok();
    }

    @PutMapping("/activity/{id}")
    public R<Void> updateActivity(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Activity a = activityMapper.selectById(id);
        if (a != null) {
            if (body.containsKey("status")) a.setStatus((Integer) body.get("status"));
            if (body.containsKey("name")) a.setName((String) body.get("name"));
            if (body.containsKey("rules")) a.setRules((String) body.get("rules"));
            activityMapper.updateById(a);
        }
        return R.ok();
    }

    @DeleteMapping("/activity/{id}")
    public R<Void> deleteActivity(@PathVariable Long id) {
        activityMapper.deleteById(id);
        return R.ok();
    }

    // ===== 商家售后列表 =====
    @GetMapping("/aftersales")
    public R<List<AfterSale>> aftersales() {
        return R.ok(afterSaleMapper.selectList(
            new LambdaQueryWrapper<AfterSale>().orderByDesc(AfterSale::getCreateTime)));
    }

    @PutMapping("/aftersale/{id}")
    public R<Void> handleAftersale(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        AfterSale a = afterSaleMapper.selectById(id);
        if (a != null) {
            a.setStatus((Integer) body.get("status"));
            afterSaleMapper.updateById(a);
        }
        return R.ok();
    }

    // ===== 秒杀场次管理 =====
    @GetMapping("/seckill/list")
    public R<List<SeckillSession>> seckillList() {
        return R.ok(seckillSessionMapper.selectList(
            new LambdaQueryWrapper<SeckillSession>().orderByDesc(SeckillSession::getCreateTime)));
    }

    @PostMapping("/seckill")
    public R<Void> createSeckill(@RequestBody Map<String, Object> body) {
        SeckillSession session = new SeckillSession();
        session.setSkuId(Long.valueOf(body.get("skuId").toString()));
        session.setSeckillPrice(Integer.parseInt(body.get("seckillPrice").toString()));
        session.setStock(Integer.parseInt(body.get("stock").toString()));
        session.setTotalStock(Integer.parseInt(body.get("stock").toString()));
        session.setSoldCount(0);
        session.setStartTime(LocalDateTime.parse((String) body.get("startTime"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        session.setEndTime(LocalDateTime.parse((String) body.get("endTime"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        session.setStatus(0); // 0=未开始
        session.setCreateTime(LocalDateTime.now());
        seckillSessionMapper.insert(session);
        // 创建后立即预热到Redis
        seckillService.warmUp(session.getId());
        return R.ok();
    }

    @DeleteMapping("/seckill/{id}")
    public R<Void> deleteSeckill(@PathVariable Long id) {
        seckillSessionMapper.deleteById(id);
        return R.ok();
    }

    // ===== 拼团管理 =====
    @GetMapping("/groupbuy/list")
    public R<List<GroupBuy>> groupbuyList() {
        return R.ok(groupBuyMapper.selectList(
            new LambdaQueryWrapper<GroupBuy>().orderByDesc(GroupBuy::getCreateTime)));
    }
}
