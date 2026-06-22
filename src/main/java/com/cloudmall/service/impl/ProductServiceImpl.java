package com.cloudmall.service.impl;

import com.cloudmall.entity.Category;
import com.cloudmall.entity.Product;
import com.cloudmall.entity.Sku;
import com.cloudmall.mapper.CategoryMapper;
import com.cloudmall.mapper.ProductMapper;
import com.cloudmall.mapper.SkuMapper;
import com.cloudmall.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MVC架构改造: 商品业务逻辑从Controller下沉到Service
 * 对比MV架构: ProductController.selectListOnShelf() → ProductService.list()
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public List<Product> list(Long categoryId, String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return productMapper.search(keyword);
        }
        if (categoryId != null && categoryId > 0) {
            return productMapper.selectByCategoryId(categoryId);
        }
        return productMapper.selectListOnShelf();
    }

    @Override
    public Product getById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<Sku> getSkuList(Long productId) {
        return skuMapper.selectByProductId(productId);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectList(null);
    }
}
