package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_presale")
public class Presale {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long skuId;
    private Integer deposit;
    private Integer finalAmount;
    private LocalDateTime depositStart;
    private LocalDateTime depositEnd;
    private LocalDateTime finalStart;
    private LocalDateTime finalEnd;
    private Integer status;
    private LocalDateTime createTime;

    /** 非数据库字段：关联商品信息 */
    @TableField(exist = false)
    private Long productId;
    @TableField(exist = false)
    private String productName;
}
