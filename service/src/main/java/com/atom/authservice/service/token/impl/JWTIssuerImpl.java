package com.atom.authservice.service.token.impl;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.atom.authservice.service.token.model.TokenInfo;
import com.atom.authservice.service.token.JWTIssuer;
import com.atom.authservice.service.token.KeyHolder;
import com.atom.authservice.service.token.model.AuthInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * JWTIssuerImpl
 *
 * @data: 2025/1/20
 * @author: yang lianhuan
 */
@Service
public class JWTIssuerImpl implements JWTIssuer {

    @Resource
    private KeyHolder keyHolder;

    @Override
    public TokenInfo generateToken(AuthInfo authInfo) {
        JWT jwt = JWT.create()
                .setSubject(authInfo.getSubject())
                .setIssuedAt(authInfo.getIssueAt())
                .addPayloads(authInfo.getClaims())
                .addHeaders(authInfo.getHeader())
                .setExpiresAt(authInfo.getExpireAt());
        JWTSigner signer = JWTSignerUtil.es512(keyHolder.getCurrentKeyPair().getPrivateKey());
        String token = jwt.sign(signer);
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setToken(token);
        tokenInfo.setIssuer(authInfo.getIssuer());
        tokenInfo.setExpiresAt(authInfo.getExpireAt());
        tokenInfo.setIssueFor(authInfo.getIssuerFor());
        tokenInfo.setIssuedAt(authInfo.getIssueAt());
        return tokenInfo;
    }
}
