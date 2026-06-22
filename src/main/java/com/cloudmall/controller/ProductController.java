package com.cloudmall.controller;

import com.cloudmall.entity.Category;
import com.cloudmall.entity.Product;
import com.cloudmall.entity.Sku;
import com.cloudmall.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 商品控制器 — MVC架构: Controller调Service, 不直接碰Mapper
 * 对比MV架构: Controller中 productMapper.xxx() → productService.xxx()
 */
@Controller
public class ProductController {

    @Resource
    private ProductService productService;

    /**
     * 商品列表页
     */
    @GetMapping({"/", "/product/list"})
    public String list(@RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        // MVC: 业务逻辑在Service, Controller只负责传参+渲染
        List<Product> products = productService.list(categoryId, keyword);
        List<Category> categories = productService.getAllCategories();

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
        Product product = productService.getById(id);
        if (product == null) {
            return "error/404";
        }
        List<Sku> skuList = productService.getSkuList(id);
        List<Category> categories = productService.getAllCategories();

        model.addAttribute("product", product);
        model.addAttribute("skuList", skuList);
        model.addAttribute("categories", categories);
        return "product/detail";
    }
}
