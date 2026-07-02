package com.cloudmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_activity")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type;
    private String rules;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long merchantId;
    private LocalDateTime createTime;
}
