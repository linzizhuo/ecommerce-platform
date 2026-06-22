package com.cloudmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.entity.Sku;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SkuMapper extends BaseMapper<Sku> {

    @Select("SELECT * FROM t_sku WHERE product_id = #{productId}")
    List<Sku> selectByProductId(Long productId);
}
