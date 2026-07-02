package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("t_stat_daily")
public class StatDaily {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Long merchantId;
    private Integer orderCount;
    private Long orderAmount;
    private Integer payCount;
    private Long payAmount;
    private Integer newUserCount;
    private Integer visitCount;
    private Integer visitUserCount;
}
