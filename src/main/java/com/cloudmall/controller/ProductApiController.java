package com.cloudmall.controller;

import com.cloudmall.common.result.R;
import com.cloudmall.entity.Category;
import com.cloudmall.entity.Product;
import com.cloudmall.entity.Sku;
import com.cloudmall.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/product")
public class ProductApiController {

    @Resource
    private ProductService productService;

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
    public R<List<?>> merchantOrders() {
        // 简化：返回空列表，后续完善
        return R.ok(Collections.emptyList());
    }
}
