package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_dict_item")
public class DictItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 字典类型ID */
    private Long dictTypeId;
    /** 标签 */
    private String label;
    /** 值 */
    private String value;
    /** 排序 */
    private Integer sort;
    /** CSS样式类 */
    private String cssClass;
    /** 1启用 0停用 */
    private Integer status;
}
