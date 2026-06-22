package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单主表
 * status: 0待付款 1已付款/待发货 2已发货 3已完成 4已取消 5退款中 6已退款
 */
@Data
@TableName("t_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Integer totalAmount;    // 分
    private Integer payAmount;      // 实付(分)
    private Integer discountAmount; // 优惠金额(分)
    private Integer status;
    private Long couponId;
    private String addressSnapshot; // JSON
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
