package com.sea.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.sea.core.service.AuthService;
import com.sea.common.entity.ResultCode;
import com.sea.common.exception.CommonException;
import com.sea.common.utils.CookieUtil;
import com.sea.domain.system.AuthToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    private static final String TOKEN_PREFIX = "USER_TOKEN";

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RestTemplate restTemplate;

    //用户认证申请令牌，将令牌存储到redis
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        //请求spring security申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if(authToken == null){
            throw new CommonException(ResultCode.AUTH_FAIL);
        }
        //用户身份令牌
        String access_token = authToken.getAccess_token();
        //存储到redis中的内容
        String jsonString = JSON.toJSONString(authToken);
        //将令牌存储到redis
        boolean result = this.saveToken(access_token, jsonString, tokenValiditySeconds);
        if (!result) {
            throw new CommonException(ResultCode.AUTH_FAIL);
        }
        return authToken;
    }

    @Override
    public void logout(String accessToken) {
        String key = TOKEN_PREFIX + ":" + accessToken;
        stringRedisTemplate.delete(key);
    }

    @Override
    public AuthToken getAuthToken(String token) {
        String key = TOKEN_PREFIX + ":" + token;
        //从redis中取到令牌信息
        String value = stringRedisTemplate.opsForValue().get(key);
        //转成对象
        try {
            return JSON.parseObject(value, AuthToken.class);
        } catch (Exception e) {
            log.error("转为json数据失败");
            return null;
        }
    }

    @Override
    public String getJwtFromHeader(HttpServletRequest request) {
        //取出头信息
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            return null;
        }
        if(!authorization.startsWith("Bearer ")){
            return null;
        }
        //取到jwt令牌
        String jwt = authorization.substring(7);
        return jwt;
    }

    @Override
    public String getTokenFromCookie(HttpServletRequest request) {
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String access_token = cookieMap.get("uid");
        if(StringUtils.isEmpty(access_token)){
            return null;
        }
        return access_token;
    }

    @Override
    public long getExpire(String accessToken) {
        String key = TOKEN_PREFIX + ":" + accessToken;
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire;
    }

    /**
     *
     * @param access_token 用户身份令牌
     * @param content  内容就是AuthToken对象的内容
     * @param ttl 过期时间
     * @return
     */
    private boolean saveToken(String access_token, String content, long ttl){
        String key = TOKEN_PREFIX + ":" + access_token;
        stringRedisTemplate.boundValueOps(key).set(content,ttl, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire > 0;
    }

    //申请令牌
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret){
        String authUrl = "http://localhost:8080/admin/oauth/token";
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic(clientId, clientSecret);
        header.add("Authorization",httpBasic);

        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables

        //设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> exchange;
        try {
            exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        } catch (ResourceAccessException e) {
            log.error("账号或密码错误", e);
            throw new CommonException(ResultCode.MOBILEORPASSWORD_ERROR);
        } catch (Exception e) {
            log.error("服务器发生错误", e);
            throw new CommonException(ResultCode.SERVER_ERROR);
        }


        //申请令牌信息
        Map bodyMap = exchange.getBody();
        // 返回值为空，表示登录失败
        if(bodyMap == null || bodyMap.get("access_token") == null || bodyMap.get("refresh_token") == null || bodyMap.get("jti") == null){
            throw new CommonException(ResultCode.MOBILEORPASSWORD_ERROR);
        }
        // 解析内容包含密码错误信息，登录失败
        if(bodyMap.get("error_description") != null) {
            String error_description = (String) bodyMap.get("error_description");
            if(error_description.contains("UserDetailsService returned null")){
                throw new CommonException(ResultCode.MOBILEORPASSWORD_ERROR);
            }else if(error_description.contains("坏的凭证")){
                throw new CommonException(ResultCode.MOBILEORPASSWORD_ERROR);
            }
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("jti"));//用户身份令牌
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));//刷新令牌
        authToken.setJwt_token((String) bodyMap.get("access_token"));//jwt令牌
        return authToken;
    }

    //获取httpbasic的串
    private String getHttpBasic(String clientId,String clientSecret){
        String string = clientId+":"+clientSecret;
        //将串进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic "+new String(encode);
    }
}
