package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    @Resource private ProductMapper productMapper;
    @Resource private SkuMapper skuMapper;
    @Resource private OrderMapper orderMapper;
    @Resource private OrderItemMapper orderItemMapper;
    @Resource private PaymentMapper paymentMapper;

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
}
