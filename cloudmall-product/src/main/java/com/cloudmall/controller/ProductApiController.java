package com.cloudmall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.result.R;
import com.cloudmall.entity.*;
import com.cloudmall.mapper.*;
import com.cloudmall.service.ProductService;
import com.cloudmall.service.impl.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
public class ProductApiController {

    @Resource
    private ProductService productService;
    @Resource
    private ConfigService configService;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderItemMapper orderItemMapper;

    /** 商品列表 */
    @GetMapping("/list")
    public R<List<Product>> list(@RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) String keyword) {
        return R.ok(productService.list(categoryId, keyword));
    }

    /** 商品详情 */
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) return R.fail("商品不存在");
        List<Sku> skuList = productService.getSkuList(id);
        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        result.put("skuList", skuList);
        return R.ok(result);
    }

    /** 类目列表 */
    @GetMapping("/category/list")
    public R<List<Category>> categories() {
        return R.ok(productService.getAllCategories());
    }

    /** 商家商品列表 */
    @GetMapping("/merchant/list")
    public R<List<Product>> merchantList(@RequestAttribute(value = "userId", required = false) Long userId) {
        // 简化：查所有上架商品
        return R.ok(productService.list(null, null));
    }

    /** 商家订单列表 */
    @GetMapping("/merchant/orders")
    public R<List<Map<String, Object>>> merchantOrders() {
        List<Order> orders = orderMapper.selectList(
            new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("userId", o.getUserId());
            m.put("payAmount", o.getPayAmount());
            m.put("totalAmount", o.getTotalAmount());
            m.put("status", o.getStatus());
            m.put("createTime", o.getCreateTime());
            m.put("items", orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId())));
            result.add(m);
        }
        return R.ok(result);
    }

    /** 首页配置（banner + 推荐商品） */
    @GetMapping("/home/config")
    public R<Map<String, Object>> homeConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("bannerText", configService.getValue("home_banner_text"));
        result.put("bannerLink", configService.getValue("home_banner_link"));

        // 推荐商品
        String idsStr = configService.getValue("home_featured_product_ids");
        List<Map<String, Object>> featuredProducts = new ArrayList<>();
        if (idsStr != null && !idsStr.isEmpty()) {
            List<Long> ids = Arrays.stream(idsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            for (Long pid : ids) {
                Product p = productService.getById(pid);
                if (p != null && p.getStatus() != null && p.getStatus() == 2) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", p.getId());
                    item.put("name", p.getName());
                    item.put("brand", p.getBrand());
                    item.put("categoryId", p.getCategoryId());
                    // 取最低SKU价格
                    List<Sku> skus = skuMapper.selectByProductId(pid);
                    int minPrice = skus.stream().mapToInt(Sku::getPrice).min().orElse(0);
                    item.put("minPrice", minPrice);
                    featuredProducts.add(item);
                }
            }
        }
        result.put("featuredProducts", featuredProducts);
        return R.ok(result);
    }
}
