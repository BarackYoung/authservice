package com.atom.authservice.service.wechat.business;

/**
 * AccessToken持有，管理微信accessToken的，避免每次都要调接口
 *
 * @data: 2025/8/5
 * @author: yang lianhuan
 */
public interface WechatAccessTokenHolder {

    /**
     * 通过appId获取微信accessToken，注意并发风险
     *
     * @param appid appid
     * @return accessToken
     */
    String getAccessToken(String appid);
}
