package com.sea.api.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.sea.core.service.AuthService;
import com.sea.common.entity.Result;
import com.sea.common.entity.ResultCode;
import com.sea.common.utils.JwtUtil;
import com.sea.jwt.JwtUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 过滤器，用来认证和授权
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        StringBuffer requestURL = httpServletRequest.getRequestURL();
        String url = requestURL.toString();
        if(url.contains("/auth") || url.contains("/oauth")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        //取cookie中的身份令牌
        String tokenFromCookie = authService.getTokenFromCookie(httpServletRequest);
        if(StringUtils.isEmpty(tokenFromCookie)){
            //拒绝访问
            accessDenied(httpServletRequest, httpServletResponse);
            return;
        }
        //从header中取jwt
        String jwtFromHeader = authService.getJwtFromHeader(httpServletRequest);
        if(StringUtils.isEmpty(jwtFromHeader)){
            //拒绝访问
            accessDenied(httpServletRequest, httpServletResponse);
            return;
        }
        //从redis取出jwt的过期时间
        long expire = authService.getExpire(tokenFromCookie);
        if(expire < 0){
            //拒绝访问
            accessDenied(httpServletRequest, httpServletResponse);
            return;
        }
        this.setAuthentication(httpServletRequest, httpServletResponse);
        // 若2020年1月11日无法找到框架自己解析的方式，采用手动编写代码
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    //拒绝访问
    private void accessDenied(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        Result result = new Result(ResultCode.UNAUTHENTICATED);
        String resultJsonStr = JSON.toJSONString(result);
        try {
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            outputStream.write(resultJsonStr.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Map jwtClaimsFromHeader = JwtUtil.getJwtClaimsFromHeader(httpServletRequest);
        Object usernameObj = jwtClaimsFromHeader.get("username");
        if(usernameObj == null) {
            this.accessDenied(httpServletRequest, httpServletResponse);
        }
        String username = String.valueOf(usernameObj);
        JSONArray authoriteArray = (JSONArray) jwtClaimsFromHeader.get("authorities");
        List<GrantedAuthority> authorities = new ArrayList<>();
        if(authoriteArray.size() > 0) {
            for(int i = 0; i < authoriteArray.size(); i++) {
                String permission = (String) authoriteArray.get(i);
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(permission);
                authorities.add(grantedAuthority);
            }
        }
        JwtUser jwtUser = new JwtUser(username, "", authorities);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jwtUser, null, jwtUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
