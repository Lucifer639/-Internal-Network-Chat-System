package com.lucifer.pp.common.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseEntity implements Serializable {
    @TableId
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private Long createdDt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateDt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Long version;

    @TableLogic(value = "1",delval = "0")
    @TableField(fill = FieldFill.INSERT)
    private Integer status;
}
