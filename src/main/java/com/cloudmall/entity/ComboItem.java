package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_combo_item")
public class ComboItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long comboId;
    private Long skuId;
    private Integer quantity;
}
