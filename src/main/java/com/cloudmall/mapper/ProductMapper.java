package com.cloudmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.entity.Product;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM t_product WHERE status = 1 ORDER BY create_time DESC")
    List<Product> selectListOnShelf();

    @Select("SELECT * FROM t_product WHERE category_id = #{categoryId} AND status = 1 ORDER BY create_time DESC")
    List<Product> selectByCategoryId(Long categoryId);

    @Select("SELECT * FROM t_product WHERE name LIKE CONCAT('%', #{keyword}, '%') AND status = 1")
    List<Product> search(String keyword);
}
