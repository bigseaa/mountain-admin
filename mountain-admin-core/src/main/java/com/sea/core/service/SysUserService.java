package com.sea.core.service;

import com.sea.domain.system.SysUser;

public interface SysUserService {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return SysUser
     */
    SysUser findByUsername(String username);

}
