package com.atom.authservice.service.token;

import cn.hutool.crypto.asymmetric.RSA;

import java.util.List;

/**
 * 密钥管理，密钥对会定期更换，以免泄漏风险，客户端验证token应以多个公钥验证为准
 *
 * @data: 2025/1/20
 * @author: yang lianhuan
 */
public interface KeyHolder {
    /**
     * 获取当前私钥
     *
     * @return 私钥
     */
    String getCurrentPrivateKey();

    /**
     * 获取当前公钥
     *
     * @return 公钥
     */
    String getCurrentPublicKey();

    /**
     * 获取仍然可用的公钥列表
     *
     * @return 公钥列表
     */
    List<String> getValidPublicKeys();

    /**
     * 获取当前密钥对
     *
     * @return RSA密钥
     */
    RSA getCurrentKeyPair();

    /**
     * 获取签发refreshToken的密钥对
     *
     * @return RSA密钥
     */
    RSA getRefreshKeyPair();
}
