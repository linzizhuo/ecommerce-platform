package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_group_buy")
public class GroupBuy {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long skuId;
    private Long leaderId;
    private Integer requiredCount;
    private Integer currentCount;
    private Integer groupPrice;
    private Integer status;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}
