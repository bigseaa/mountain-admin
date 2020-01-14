package com.sea.core.service;


import com.sea.domain.system.AuthToken;

import javax.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthToken login(String username, String password, String clientId, String clientSecret);

    void logout(String accessToken);

    AuthToken getAuthToken(String token);

    //从头取出jwt令牌
    String getJwtFromHeader(HttpServletRequest request);

    //从cookie取出token
    //查询身份令牌
    String getTokenFromCookie(HttpServletRequest request);

    //查询令牌的有效期
    long getExpire(String accessToken);
}
