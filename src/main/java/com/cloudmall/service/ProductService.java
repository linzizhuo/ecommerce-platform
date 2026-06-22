package com.cloudmall.service;

import com.cloudmall.entity.Category;
import com.cloudmall.entity.Product;
import com.cloudmall.entity.Sku;

import java.util.List;

public interface ProductService {

    List<Product> list(Long categoryId, String keyword);

    Product getById(Long id);

    List<Sku> getSkuList(Long productId);

    List<Category> getAllCategories();
}
