package com.atom.authservice.service.token;

import com.atom.authservice.service.token.model.TokenInfo;
import com.atom.authservice.service.token.model.AuthInfo;

import java.security.Key;

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
     * @param key 私钥
     * @return token
     */
    TokenInfo generateToken(AuthInfo authInfo, Key key);
}
