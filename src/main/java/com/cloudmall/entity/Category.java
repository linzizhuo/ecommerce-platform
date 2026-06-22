package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品类目
 */
@Data
@TableName("t_category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类目名称 */
    private String name;

    /** 父类目ID, 0为顶级 */
    private Long parentId;

    /** 层级 1/2 */
    private Integer level;

    /** 排序 */
    private Integer sort;
}
