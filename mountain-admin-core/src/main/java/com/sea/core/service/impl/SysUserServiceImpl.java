package com.sea.core.service.impl;

import com.sea.core.dao.SysUserDao;
import com.sea.core.service.SysUserService;
import com.sea.domain.system.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserDao sysUserDao;

    @Override
    public SysUser findByUsername(String username) {
        return sysUserDao.findByUsername(username);
    }
}
