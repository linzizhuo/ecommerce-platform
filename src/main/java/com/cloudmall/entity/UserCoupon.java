package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_user_coupon")
public class UserCoupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long templateId;
    /** 0未使用 1已使用 2已过期 */
    private Integer status;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}
