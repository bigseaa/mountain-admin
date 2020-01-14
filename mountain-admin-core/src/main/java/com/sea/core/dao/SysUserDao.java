package com.sea.core.dao;

import com.sea.domain.system.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserDao extends JpaRepository<SysUser,String>,JpaSpecificationExecutor<SysUser> {
    SysUser findByUsername(String username);
}
