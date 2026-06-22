package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * SKU - 库存量单位
 */
@Data
@TableName("t_sku")
public class Sku {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属商品ID */
    private Long productId;

    /** 规格信息 JSON {"颜色":"红色","尺寸":"M"} */
    private String specInfo;

    /** 价格(分) */
    private Integer price;

    /** 原价(分) */
    private Integer originalPrice;

    /** SKU图片 */
    private String image;

    /** 库存 */
    private Integer stock;
}
