package com.agrodrone.mapper;

import com.agrodrone.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("""
            SELECT id, username, password_hash, real_name, role, status, created_at
            FROM sys_user
            WHERE username = #{username}
            LIMIT 1
            """)
    SysUser selectByUsername(@Param("username") String username);
}
