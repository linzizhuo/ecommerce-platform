package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    /** 数据看板 */
    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        List<Product> products = productMapper.selectList(null);
        List<Order> orders = orderMapper.selectList(null);
        long todayOrders = orders.stream().filter(o -> o.getCreateTime() != null &&
            o.getCreateTime().toLocalDate().equals(LocalDateTime.now().toLocalDate())).count();
        int revenue = orders.stream().filter(o -> o.getStatus() != null && o.getStatus() >= 1)
            .mapToInt(o -> o.getPayAmount() != null ? o.getPayAmount() : 0).sum();
        Map<String, Object> data = new HashMap<>();
        data.put("productCount", products.size());
        data.put("orderCount", orders.size());
        data.put("todayOrders", todayOrders);
        data.put("revenue", revenue);
        return R.ok(data);
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
}
