package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String payNo;
    private Integer amount;
    /** 支付方式: 1支付宝 2微信 */
    private Integer payMethod;
    /** 0待支付 1已支付 */
    private Integer status;
    private LocalDateTime payTime;
}
