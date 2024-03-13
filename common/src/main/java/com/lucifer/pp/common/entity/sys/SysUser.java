package com.lucifer.pp.common.entity.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lucifer.pp.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user")
@AllArgsConstructor
@NoArgsConstructor
public class SysUser extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    private String userCode;
    /**
     * 昵称
     */
    private String name;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 注册时间
     */
    private Long registerTime;
    /**
     * 状态
     */
    private Integer state;
}
