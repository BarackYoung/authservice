package com.atom.authservice.service.wechat.business.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.atom.authservice.dal.entity.AccountEntity;
import com.atom.authservice.dal.entity.AppInfoEntity;
import com.atom.authservice.dal.entity.AuthPatternEntity;
import com.atom.authservice.dal.repository.AccountRepository;
import com.atom.authservice.dal.repository.AppInfoRepository;
import com.atom.authservice.dal.repository.AuthPatternRepository;
import com.atom.authservice.service.account.AccountService;
import com.atom.authservice.service.login.LoginService;
import com.atom.authservice.service.login.enums.AuthTypeEnum;
import com.atom.authservice.service.token.model.TokenInfo;
import com.atom.authservice.service.wechat.business.WechatLoginProcessor;
import com.atom.authservice.service.wechat.business.WechatAccessTokenHolder;
import com.atom.commonsdk.model.ResultCode;
import com.atom.commonsdk.utils.AssertUtils;
import com.atom.commonsdk.wechat.WeUserManageService;
import com.atom.commonsdk.wechat.bean.request.BatchQueryUsrInfoReq;
import com.atom.commonsdk.wechat.bean.response.BatchUserInfoResp;
import com.atom.commonsdk.wechat.message.EventMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * LoginProcessorImpl
 *
 * @data: 2025/8/4
 * @author: yang lianhuan
 */
@Component
@Slf4j
public class WechatLoginProcessorImpl implements WechatLoginProcessor {
    private static final String LOGIN_PREFIX = "LOGIN_";
    private static final String LANG = "zh_CN";

    @Resource
    private AccountService accountService;

    @Resource
    private LoginService loginService;

    @Resource
    private AppInfoRepository appInfoRepository;

    @Resource
    private AuthPatternRepository authPatternRepository;

    @Resource
    private WeUserManageService userManageService;

    @Resource
    private WechatAccessTokenHolder wechatAccessTokenHolder;

    @Resource
    private AccountRepository accountRepository;

    @Override
    public void processLogin(EventMessage eventMessage) {
        log.info("WechatLoginProcessorImpl.processLogin,eventMessage={}", eventMessage);
        String sceneStr = eventMessage.getEventKey();
        if (!StrUtil.startWith(sceneStr, LOGIN_PREFIX)) {
            return;
        }

        // 查询app实例配置
        String[] sceneArr = sceneStr.split(StrUtil.UNDERLINE);
        AssertUtils.assertTrue(sceneArr.length == 4, ResultCode.BUSINESS_ERROR);
        String appShortCode = sceneArr[1];
        AppInfoEntity appInfoEntity = appInfoRepository.findByAppShortCode(appShortCode);
        log.info("WechatLoginProcessorImpl.processLogin,appInfoEntity={}", appInfoEntity);
        AssertUtils.assertNotNull(appInfoEntity, ResultCode.BUSINESS_ERROR);
        AssertUtils.assertNotNull(appInfoEntity.getWePublicAccountAppId(), ResultCode.BUSINESS_ERROR);

        // 查询鉴权信息
        String openId = eventMessage.getFromUserName();
        AuthPatternEntity authPatternEntity = authPatternRepository.findByAppCodeAndIdentifierAndAuthType(
                appInfoEntity.getAppCode(), openId, AuthTypeEnum.WECHAT_PUBLIC_ACCOUNT.name());
        log.info("WechatLoginProcessorImpl.processLogin,authPatternEntity={}", authPatternEntity);

        // 新用户第一次扫码，注册
        if (Objects.isNull(authPatternEntity)) {
            try {
                AccountEntity accountEntity = register(appInfoEntity, openId, sceneStr);
                AssertUtils.assertNotNull(accountEntity, ResultCode.BUSINESS_ERROR);
            } catch (Exception e) {
                log.error("LoginProcessorImpl.register failed", e);
                return;
            }
        }

        // 老用户扫码，更新信息
        authPatternEntity = authPatternRepository.findByAppCodeAndIdentifierAndAuthType(
                appInfoEntity.getAppCode(), openId, AuthTypeEnum.WECHAT_PUBLIC_ACCOUNT.name());
        log.info("WechatLoginProcessorImpl.processLogin,authPatternEntity={}", authPatternEntity);
        AssertUtils.assertNotNull(authPatternEntity, ResultCode.BUSINESS_ERROR);
        updateUserAuthInfo(authPatternEntity, appInfoEntity.getWePublicAccountAppId(), openId, sceneStr);

        // 签发token，并设置登录结果
        TokenInfo tokenInfo = loginService.verifyAndSignToken(appInfoEntity.getAppCode(),
                AuthTypeEnum.WECHAT_PUBLIC_ACCOUNT, openId, sceneStr);
        loginService.setLoginResult(appInfoEntity.getAppCode(), authPatternEntity.getAccountId(), sceneStr, tokenInfo);
    }

    private void updateUserAuthInfo(AuthPatternEntity authPatternEntity, String wePublicAppId, String openId, String sceneStr) {
        // 更新鉴权信息
        authPatternEntity.setCredential(sceneStr);
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime + 360000;
        authPatternEntity.setExpireTime(new Date(expireTime));
        authPatternRepository.save(authPatternEntity);

        // 更新用户信息
        BatchUserInfoResp batchUserInfoResp = queryUserInfo(wePublicAppId, openId);
        AccountEntity accountEntity = accountRepository.findByAccountId(authPatternEntity.getAccountId());
        if (Objects.nonNull(batchUserInfoResp) && CollectionUtil.isNotEmpty(batchUserInfoResp.getUser_info_list())) {
            BatchUserInfoResp.UserInfo userInfo = batchUserInfoResp.getUser_info_list().get(0);
            accountEntity.setCity(userInfo.getCity());
            accountEntity.setProvince(userInfo.getProvince());
            accountEntity.setCountry(userInfo.getCountry());
            accountEntity.setUsername(userInfo.getNickname());
            accountEntity.setAvatarUrl(userInfo.getHeadimgurl());
            accountEntity.setSex(userInfo.getSex());
            accountRepository.save(accountEntity);
        }
    }

    private BatchUserInfoResp queryUserInfo(String wePublicAccountAppId, String openId) {
        String accessToken = wechatAccessTokenHolder.getAccessToken(wePublicAccountAppId);
        List<BatchQueryUsrInfoReq.User> users = new ArrayList<>();
        BatchQueryUsrInfoReq.User user = BatchQueryUsrInfoReq.User.builder()
                .lang(LANG)
                .openid(openId)
                .build();
        users.add(user);
        BatchQueryUsrInfoReq batchQueryUsrInfoReq = new BatchQueryUsrInfoReq();
        batchQueryUsrInfoReq.setUser_list(users);
        return userManageService.batchQueryUserInfo(accessToken, LANG, batchQueryUsrInfoReq);
    }

    private AccountEntity register(AppInfoEntity appInfoEntity, String openId, String sceneStr) {
        // 查询用户信息
        BatchUserInfoResp batchUserInfoResp = queryUserInfo(appInfoEntity.getWePublicAccountAppId(), openId);
        log.info("WechatLoginProcessorImpl.register,batchUserInfoResp={}", batchUserInfoResp);
        AssertUtils.assertNotNull(batchUserInfoResp, ResultCode.BUSINESS_ERROR);
        AssertUtils.assertNotNull(batchUserInfoResp.getUser_info_list(), ResultCode.BUSINESS_ERROR);

        // 生成账户和登录方式
        BatchUserInfoResp.UserInfo userInfo = batchUserInfoResp.getUser_info_list().get(0);
        AccountEntity accountEntity = new AccountEntity();
        String uid = accountService.generateUid();
        String accountId = accountService.generateAccountId(appInfoEntity.getAppShortCode());
        accountEntity.setUid(uid);
        accountEntity.setAccountId(accountId);
        accountEntity.setCity(userInfo.getCity());
        accountEntity.setProvince(userInfo.getProvince());
        accountEntity.setCountry(userInfo.getCountry());
        accountEntity.setUsername(userInfo.getNickname());
        accountEntity.setAvatarUrl(userInfo.getHeadimgurl());
        accountEntity.setPlatform(appInfoEntity.getAppCode());
        accountEntity.setSex(userInfo.getSex());

        AuthPatternEntity authPatternEntity = new AuthPatternEntity();
        authPatternEntity.setIdentifier(openId);
        authPatternEntity.setAppCode(appInfoEntity.getAppCode());
        authPatternEntity.setAuthType(AuthTypeEnum.WECHAT_PUBLIC_ACCOUNT.name());
        authPatternEntity.setCredential(sceneStr);
        authPatternEntity.setAccountId(accountId);
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime + 360000;
        authPatternEntity.setExpireTime(new Date(expireTime));

        // 注册账户
        return loginService.registerAccount(accountEntity, authPatternEntity);
    }
}
