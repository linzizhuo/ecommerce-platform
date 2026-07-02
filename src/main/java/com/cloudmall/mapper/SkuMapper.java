package com.cloudmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.entity.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SkuMapper extends BaseMapper<Sku> {

    @Select("SELECT * FROM t_sku WHERE product_id = #{productId}")
    List<Sku> selectByProductId(Long productId);

    /**
     * 原子扣减库存 — MySQL InnoDB行锁保证并发安全
     * WHERE stock >= quantity 保证不超卖，返回0表示库存不足
     */
    @Update("UPDATE t_sku SET stock = stock - #{quantity} WHERE id = #{skuId} AND stock >= #{quantity}")
    int deductStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    /**
     * 恢复库存 — 取消订单/改单时回滚库存
     */
    @Update("UPDATE t_sku SET stock = stock + #{quantity} WHERE id = #{skuId}")
    int restoreStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);
}
