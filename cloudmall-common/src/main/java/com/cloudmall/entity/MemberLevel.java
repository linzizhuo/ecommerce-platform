package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_member_level")
public class MemberLevel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String levelName;
    private Integer levelCode;
    private Integer minAmount;
    private Integer discountRate;
    private Integer freeShipping;
    private String description;
}
