package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_dict_type")
public class DictType {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 字典名称 */
    private String dictName;
    /** 字典编码 */
    private String dictCode;
    /** 描述 */
    private String description;
    /** 1启用 0停用 */
    private Integer status;
}
