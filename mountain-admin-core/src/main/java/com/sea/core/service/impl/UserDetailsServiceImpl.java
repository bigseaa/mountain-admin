package com.sea.core.service.impl;

import com.sea.core.service.SysUserService;
import com.sea.common.entity.ResultCode;
import com.sea.common.exception.CommonException;
import com.sea.domain.system.SysUser;
import com.sea.jwt.JwtUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication==null){
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if(clientDetails!=null){
                //密码
                String clientSecret = clientDetails.getClientSecret();
                return new User(username,clientSecret,AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        if (StringUtils.isEmpty(username)) {
            return null;
        }

        SysUser sysUser = sysUserService.findByUsername(username);
        // 如果没有通过该用户名查到用户，登录失败
        if(sysUser == null){
            throw new CommonException(ResultCode.MOBILEORPASSWORD_ERROR);
        }
        // 取出正确密码（hash值）
        String password = sysUser.getPassword();

        List<String> permissionList = new ArrayList<>();
        permissionList.add("test_test_001");
        permissionList.add("test_test_002");
        String permissionStr  = StringUtils.join(permissionList.toArray(), ",");
        JwtUser jwtUser = new JwtUser(username, password,
                AuthorityUtils.commaSeparatedStringToAuthorityList(permissionStr));
        jwtUser.setId(sysUser.getId());
        jwtUser.setUsername(sysUser.getUsername());//用户名称
        jwtUser.setMobile(sysUser.getMobile());
        jwtUser.setCompanyId(sysUser.getCompanyId());
        return jwtUser;
    }
}
