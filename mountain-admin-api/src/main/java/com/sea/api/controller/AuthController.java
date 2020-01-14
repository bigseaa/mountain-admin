package com.sea.api.controller;

import com.sea.core.service.AuthService;
import com.sea.common.entity.Result;
import com.sea.common.entity.ResultCode;
import com.sea.common.exception.CommonException;
import com.sea.common.utils.CookieUtil;
import com.sea.domain.system.AuthToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;

    @Autowired
    private AuthService authService;

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @param verifycode 验证码
     * @return token
     */
    @PostMapping("/login")
    public Result login(@RequestParam String username, @RequestParam String password, @RequestParam(required = false) String verifycode) {
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new CommonException(ResultCode.AUTH_FAIL);
        }
        // 申请令牌
        AuthToken authToken = authService.login(username, password, clientId, clientSecret);
        // 用户身份令牌
        String access_token = authToken.getAccess_token();
        this.saveCookie(access_token);
        return new Result(ResultCode.SUCCESS, access_token);
    }

    /**
     * 登出
     * @return Result
     */
    @PostMapping("/logout")
    public Result logout() {
        //取出cookie中的用户身份令牌
        String uid = getTokenFormCookie();
        // 删除redis中的token
        authService.logout(uid);
        // 清除cookie
        this.clearCookie(uid);
        return Result.SUCCESS();
    }

    /**
     * 根据token获取用户信息
     * @return Result
     */
    @GetMapping("/getJwt")
    public Result getJwt() {
        //取出cookie中的用户身份令牌
        String uid = getTokenFormCookie();
        if(uid == null){
            return new Result(ResultCode.FAIL);
        }

        // 拿身份令牌从redis中查询jwt令牌
        AuthToken userToken = authService.getAuthToken(uid);
        if(userToken != null){
            //将jwt令牌返回给用户
            String jwt_token = userToken.getJwt_token();
            return new Result(ResultCode.SUCCESS, jwt_token);
        }
        return new Result(ResultCode.FAIL);
    }

    /**
     * 将jtl写入cookie
     * @param token String
     */
    private void saveCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //HttpServletResponse response,String domain,String path, String name, String value, int maxAge,boolean httpOnly
        CookieUtil.addCookie(response, cookieDomain,"/","uid", token, cookieMaxAge,false);
    }

    /**
     * 从cookie中获取jtl身份表示
     * @return String
     */
    private String getTokenFormCookie(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if(map!=null && map.get("uid")!=null){
            return map.get("uid");
        }
        return null;
    }

    /**
     * 从cookie中清除jtl
     * @param token String
     */
    private void clearCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token,0,false);
    }
}
