package com.atom.authservice.service.token;

import com.atom.authservice.service.token.model.TokenInfo;
import com.atom.authservice.service.token.model.AuthInfo;

/**
 * JWT 签发器
 *
 * @data: 2025/1/20
 * @author: yang lianhuan
 */
public interface JWTIssuer {
    String DEFAULT_ISSUER = "authService";

    /**
     * 签发一个token
     *
     * @param authInfo 鉴权信息
     * @return token
     */
    TokenInfo generateToken(AuthInfo authInfo);

    /**
     * 未登录状态下，生成常规接口调用token，非用户专用token，用于保护非登录状态下接口调用
     *
     * @return 常规token
     */
    default String generateAppToken(String clientId) {
        AuthInfo authInfo = new AuthInfo();
        authInfo.setIssuer(DEFAULT_ISSUER);
        authInfo.setIssuerFor(clientId);
        return generateToken(authInfo).getToken();
    }
}
