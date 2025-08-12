package com.atom.authservice.service.wechat.business.impl;

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
import com.atom.authservice.service.login.enums.LoginStatusEnum;
import com.atom.authservice.service.token.model.TokenInfo;
import com.atom.authservice.service.wechat.business.WePublicAccountLoginProcessor;
import com.atom.authservice.service.wechat.business.WechatAccessTokenHolder;
import com.atom.commonsdk.model.ResultCode;
import com.atom.commonsdk.utils.AssertUtils;
import com.atom.commonsdk.wechat.WeUserManageService;
import com.atom.commonsdk.wechat.bean.request.BatchQueryUsrInfoReq;
import com.atom.commonsdk.wechat.bean.response.BatchUserInfoResp;
import com.atom.commonsdk.wechat.message.EventMessage;
import com.atom.commonsdk.wechat.utils.XmlMessageUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
public class WePublicAccountLoginProcessorImpl implements WePublicAccountLoginProcessor {
    private static final String LANG = "zh_CN";
    private static final String DEFAULT_USERNAME_PREFIX = "海豚";
    private static final String LOGIN_FAIL_MSG = "登录失败";
    private static final String LOGIN_SUCCESS_MSG = "登录成功";

    @Resource
    private AccountService accountService;

    @Resource
    private LoginService loginService;

    @Resource
    private AppInfoRepository appInfoRepository;

    @Resource
    private WeUserManageService userManageService;

    @Resource
    private WechatAccessTokenHolder wechatAccessTokenHolder;

    @Override
    public String processLogin(EventMessage eventMessage) {
        log.info("WechatLoginProcessorImpl.processLogin,eventMessage={}", eventMessage);
        String sceneStr = eventMessage.getEventKey();

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
        AuthPatternEntity authPatternEntity = loginService.queryAuthPattern(appInfoEntity.getAppCode(), openId,
                AuthTypeEnum.WECHAT_PUBLIC_ACCOUNT);
        log.info("WechatLoginProcessorImpl.processLogin,authPatternEntity={}", authPatternEntity);

        // 新用户第一次扫码，注册
        if (Objects.isNull(authPatternEntity)) {
            try {
                AccountEntity accountEntity = register(appInfoEntity, openId, sceneStr);
                AssertUtils.assertNotNull(accountEntity, ResultCode.BUSINESS_ERROR);
            } catch (Exception e) {
                log.error("LoginProcessorImpl.register failed", e);
                loginService.updateLoginRecord(sceneStr, null, null, LoginStatusEnum.FAIL);
                return XmlMessageUtil.parse2TextXmlMsg(eventMessage.getToUserName(), eventMessage.getFromUserName(), LOGIN_FAIL_MSG);
            }
        } else {
            // 老用户扫码，更新信息
            updateUserAuthInfo(authPatternEntity, sceneStr);
        }

        // 签发token，并设置登录结果
        authPatternEntity = loginService.queryAuthPattern(appInfoEntity.getAppCode(), openId, AuthTypeEnum.WECHAT_PUBLIC_ACCOUNT);
        AssertUtils.assertNotNull(authPatternEntity, ResultCode.BUSINESS_ERROR);
        TokenInfo tokenInfo = loginService.verifyAndSignToken(appInfoEntity.getAppCode(),
                AuthTypeEnum.WECHAT_PUBLIC_ACCOUNT, openId, sceneStr);
        loginService.setLoginResult(appInfoEntity.getAppCode(), authPatternEntity.getAccountId(), sceneStr, tokenInfo);
        loginService.updateLoginRecord(sceneStr, authPatternEntity.getAccountId(), authPatternEntity.getIdentifier(), LoginStatusEnum.ISSUED);
        return XmlMessageUtil.parse2TextXmlMsg(eventMessage.getToUserName(), eventMessage.getFromUserName(), LOGIN_SUCCESS_MSG);
    }

    private void updateUserAuthInfo(AuthPatternEntity authPatternEntity, String sceneStr) {
        // 更新鉴权信息
        authPatternEntity.setCredential(sceneStr);
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime + 360000;
        authPatternEntity.setExpireTime(new Date(expireTime));
        loginService.updateAuthPattern(authPatternEntity);
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
        accountEntity.setUsername(generateDefaultUserName(uid));
        accountEntity.setAppCode(appInfoEntity.getAppCode());
        accountEntity.setWeUnionid(userInfo.getUnionid());

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

    private String generateDefaultUserName(String uid) {
        String suffix = uid.substring(10);
        return DEFAULT_USERNAME_PREFIX + suffix;
    }
}
