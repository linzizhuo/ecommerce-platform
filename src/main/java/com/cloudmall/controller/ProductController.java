package com.cloudmall.controller;

import com.cloudmall.entity.Category;
import com.cloudmall.entity.Product;
import com.cloudmall.entity.Sku;
import com.cloudmall.mapper.CategoryMapper;
import com.cloudmall.mapper.ProductMapper;
import com.cloudmall.mapper.SkuMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 商品控制器 — MV架构: Controller直接调用Mapper, 无Service层
 */
@Controller
public class ProductController {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private CategoryMapper categoryMapper;

    /**
     * 商品列表页
     */
    @GetMapping({"/", "/product/list"})
    public String list(@RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        List<Product> products;
        if (keyword != null && !keyword.isEmpty()) {
            products = productMapper.search(keyword);
        } else if (categoryId != null && categoryId > 0) {
            products = productMapper.selectByCategoryId(categoryId);
        } else {
            products = productMapper.selectListOnShelf();
        }

        // 类目导航
        List<Category> categories = categoryMapper.selectList(null);

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        return "product/list";
    }

    /**
     * 商品详情页
     */
    @GetMapping("/product/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return "error/404";
        }
        List<Sku> skuList = skuMapper.selectByProductId(id);
        List<Category> categories = categoryMapper.selectList(null);

        model.addAttribute("product", product);
        model.addAttribute("skuList", skuList);
        model.addAttribute("categories", categories);
        return "product/detail";
    }
}
