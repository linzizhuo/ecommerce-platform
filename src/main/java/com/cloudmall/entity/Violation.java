package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_violation")
public class Violation {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 商家ID */
    private Long merchantId;
    /** 违规类型: 1商品违规 2虚假发货 3欺诈 */
    private Integer type;
    /** 违规原因 */
    private String reason;
    /** 处罚类型: 1警告 2罚款 3下架商品 4封店 */
    private Integer penaltyType;
    /** 罚金(分) */
    private Integer penaltyAmount;
    /** 0待执行 1已执行 2已申诉 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
}
