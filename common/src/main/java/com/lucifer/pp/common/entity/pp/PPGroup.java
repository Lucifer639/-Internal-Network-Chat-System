package com.lucifer.pp.common.entity.pp;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lucifer.pp.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("pp_group")
@AllArgsConstructor
@NoArgsConstructor
public class PPGroup extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private String avatar;
}
