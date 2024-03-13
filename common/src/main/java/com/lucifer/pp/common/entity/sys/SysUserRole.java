package com.lucifer.pp.common.entity.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lucifer.pp.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user_role")
@AllArgsConstructor
@NoArgsConstructor
public class SysUserRole extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long userId;
    private Long roleId;
}
