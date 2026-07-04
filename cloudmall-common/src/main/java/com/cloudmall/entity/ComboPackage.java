package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_combo_package")
public class ComboPackage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer totalPrice;
    private Integer originalPrice;
    private Long merchantId;
    private Integer status;
    private LocalDateTime createTime;
}
