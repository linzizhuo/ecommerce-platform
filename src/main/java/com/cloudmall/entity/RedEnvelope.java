package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_red_envelope")
public class RedEnvelope {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 发送人用户ID */
    private Long senderId;
    /** 接收人用户ID */
    private Long receiverId;
    /** 关联订单ID */
    private Long orderId;
    /** 金额(分) */
    private Integer amount;
    /** 1普通红包 2订单返利红包 */
    private Integer type;
    /** 0未领取 1已领取 2已过期 */
    private Integer status;
    /** 祝福语 */
    private String message;
    /** 过期时间 */
    private LocalDateTime expireTime;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 领取时间 */
    private LocalDateTime receiveTime;
}
