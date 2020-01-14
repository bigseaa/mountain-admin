package com.sea.api.config;

import com.sea.jwt.JwtUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomUserAuthenticationConverter extends DefaultUserAuthenticationConverter {
    @Autowired
    UserDetailsService userDetailsService;

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        LinkedHashMap response = new LinkedHashMap();
        String name = authentication.getName();
        response.put("username", name);

        Object principal = authentication.getPrincipal();
        JwtUser jwtUser = null;
        if(principal instanceof JwtUser){
            jwtUser = (JwtUser) principal;
        }else{
            // refresh_token默认不调用userdetailService获取用户信息，这里手动触发调用获取用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(name);
            jwtUser = (JwtUser) userDetails;
        }
        response.put("id", jwtUser.getId());
        response.put("mobile", jwtUser.getMobile());
        response.put("company_id",jwtUser.getCompanyId());
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put("authorities", AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }
}
