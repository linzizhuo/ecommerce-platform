package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_logistics")
public class Logistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String company;
    private String trackingNo;
    private String traceData;
    private Integer status;
    private LocalDateTime shipTime;
    private LocalDateTime signTime;
}
