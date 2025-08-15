package com.atom.authservice.service.token.impl;

import cn.hutool.crypto.asymmetric.RSA;
import com.atom.authservice.service.token.KeyHolder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * KeyHolderImpl，密钥对暂时以配置文件形式明文保存，后续须优化，支持密钥对定期轮换
 *
 * @data: 2025/1/20
 * @author: yang lianhuan
 */
@Service
public class KeyHolderImpl implements KeyHolder {

    @Value("${authservice.auth.privateKeyBase64}")
    private String privateKeyBase64;

    @Value("${authservice.auth.publicKeyBase64}")
    private String publicKeyBase64;

    @Value("${authservice.auth.refresh.token.privateKeyBase64}")
    private String refreshPrivateKeyBase64;

    @Value("${authservice.auth.refresh.token.publicKeyBase64}")
    private String refreshPublicKeyBase64;

    private RSA keyPair;

    private RSA refreshKeyPair;

    @PostConstruct
    public void setUp() {
        this.keyPair = new RSA(privateKeyBase64, publicKeyBase64);
        this.refreshKeyPair = new RSA(refreshPrivateKeyBase64, refreshPublicKeyBase64);
    }

    @Override
    public String getCurrentPrivateKey() {
        return this.privateKeyBase64;
    }

    @Override
    public String getCurrentPublicKey() {
        return this.privateKeyBase64;
    }

    @Override
    public List<String> getValidPublicKeys() {
        return List.of();
    }

    @Override
    public RSA getCurrentKeyPair() {
        return this.keyPair;
    }

    @Override
    public RSA getRefreshKeyPair() {
        return this.refreshKeyPair;
    }
}
