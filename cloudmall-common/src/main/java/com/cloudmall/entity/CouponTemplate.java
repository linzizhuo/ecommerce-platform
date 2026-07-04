package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_coupon_template")
public class CouponTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String name;
    /** 优惠类型: 1满减券 2折扣券 */
    private Integer type;
    /** 使用门槛(分) */
    private Integer threshold;
    /** 优惠值: 满减券=减多少分, 折扣券=打几折(85=8.5折) */
    private Integer value;
    private Integer totalCount;
    private Integer receivedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
