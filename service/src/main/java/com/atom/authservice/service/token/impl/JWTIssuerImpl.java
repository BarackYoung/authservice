package com.atom.authservice.service.token.impl;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.alibaba.fastjson2.JSON;
import com.atom.authservice.service.token.model.TokenInfo;
import com.atom.authservice.service.token.JWTIssuer;
import com.atom.authservice.service.token.KeyHolder;
import com.atom.authservice.service.token.model.AuthInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;

/**
 * JWTIssuerImpl
 *
 * @data: 2025/1/20
 * @author: yang lianhuan
 */
@Service
@Slf4j
public class JWTIssuerImpl implements JWTIssuer {

    @Resource
    private KeyHolder keyHolder;

    @Override
    public TokenInfo generateToken(AuthInfo authInfo, Key key) {
        log.info("JWTIssuerImpl.authInfo: {}", JSON.toJSONString(authInfo));
        JWT jwt = JWT.create()
                .setSubject(authInfo.getSubject())
                .setIssuedAt(authInfo.getIssueAt())
                .addPayloads(authInfo.generateClaims())
                .addHeaders(authInfo.generateHeader())
                .setExpiresAt(authInfo.getExpireAt());
        log.info("JWTIssuerImpl.JWT token: {}", JSON.toJSONString(jwt));
        JWTSigner signer = JWTSignerUtil.rs512(keyHolder.getCurrentKeyPair().getPrivateKey());
        String token = jwt.sign(signer);
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setToken(token);
        tokenInfo.setIssuer(authInfo.getIssuer());
        tokenInfo.setExpiresAt(authInfo.getExpireAt());
        tokenInfo.setIssueFor(authInfo.getIssuerFor());
        tokenInfo.setIssuedAt(authInfo.getIssueAt());
        tokenInfo.setAuthInfo(authInfo);
        return tokenInfo;
    }
}
