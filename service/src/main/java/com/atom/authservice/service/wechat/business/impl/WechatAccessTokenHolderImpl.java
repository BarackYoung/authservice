package com.atom.authservice.service.wechat.business.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.atom.authservice.service.wechat.business.WechatAccessTokenHolder;
import com.atom.commonsdk.wechat.WeBasicService;
import com.atom.commonsdk.wechat.bean.request.AccessTokenRequest;
import com.atom.commonsdk.wechat.bean.response.AccessTokenResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * WechatAccessTokenHolderImpl
 *
 * @data: 2025/8/5
 * @author: yang lianhuan
 */
@Slf4j
@Component
public class WechatAccessTokenHolderImpl implements WechatAccessTokenHolder {
    private static final String ACCESS_TOKEN_KEY = "authservice:WechatAccessTokenHolderImpl:access_token:";
    private static final String GRANT_TYPE = "client_credential";
    private Map<String, String> secretMap = new HashMap<>();

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private WeBasicService weBasicService;

    @Value("${wechat.public.account.secretMap}")
    private String accountSecretMapStr;

    @PostConstruct
    public void init() {
        secretMap = JSON.parseObject(accountSecretMapStr, new TypeReference<>() {});
    }

    @Override
    public String getAccessToken(String appid) {
        String cacheKey = ACCESS_TOKEN_KEY + appid;
        String cachedToken = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cachedToken)) {
            return cachedToken;
        }
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        accessTokenRequest.setAppid(appid);
        accessTokenRequest.setSecret(secretMap.get(appid));
        accessTokenRequest.setGrant_type(GRANT_TYPE);
        AccessTokenResponse accessTokenResponse = weBasicService.getAccessToken(accessTokenRequest);
        if (Objects.isNull(accessTokenResponse)) {
            log.error("WechatAccessTokenHolderImpl.getAccessToken failed");
            return null;
        }
        int expireIn = accessTokenResponse.getExpires_in();
        String accessToken = accessTokenResponse.getAccess_token();
        redisTemplate.opsForValue().setIfAbsent(cacheKey, accessToken, expireIn - 30, TimeUnit.SECONDS);
        return accessToken;
    }
}
