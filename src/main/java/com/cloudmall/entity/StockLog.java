package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_stock_log")
public class StockLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long skuId;
    private Long orderId;
    private Integer beforeStock;
    private Integer changeCount;
    private Integer afterStock;
    private Integer type;
    private LocalDateTime createTime;
}
