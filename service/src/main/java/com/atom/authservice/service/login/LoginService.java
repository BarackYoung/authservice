package com.atom.authservice.service.login;

import com.atom.authservice.dal.entity.AccountEntity;
import com.atom.authservice.dal.entity.AuthPatternEntity;
import com.atom.authservice.service.login.bean.GetSceneQrCodeParam;
import com.atom.authservice.service.login.bean.SceneStrQrcodeInfo;
import com.atom.authservice.service.token.model.TokenInfo;
import com.atom.authservice.service.login.enums.AuthTypeEnum;

/**
 * 登录服务
 *
 * @data: 2025/8/2
 * @author: yang lianhuan
 */
public interface LoginService {
    /**
     * 获取一个带场景值的二维码
     *
     * @param request 获取对应场景值二维码请求
     * @return 二维码
     */
    SceneStrQrcodeInfo getSceneStrQrcode(GetSceneQrCodeParam request);

    /**
     * 检查登录结果
     *
     * @param sceneStr 场景码
     * @return 登录结果
     */
    TokenInfo checkLoginResult(String sceneStr);

    /**
     * 设置登录结果
     *
     * @param sceneStr 场景值
     * @param appCode appCode
     * @param accountId 账号信息
     * @param tokenInfo token信息
     */
    void setLoginResult(String appCode, String accountId, String sceneStr, TokenInfo tokenInfo);

    /**
     * 注册账号，并添加一个认证方法
     *
     * @param accountEntity 账号实体
     * @param authPatternEntity 认证方法
     * @return 是否成功
     */
    AccountEntity registerAccount(AccountEntity accountEntity, AuthPatternEntity authPatternEntity);

    /**
     * 新创建一个认证方式
     *
     * @param authPatternEntity 认证方式实体
     * @return 认证方式
     */
    AuthPatternEntity newAuthPattern(AuthPatternEntity authPatternEntity);

    /**
     * 验证并签发token
     *
     * @param appCode appCode
     * @param authTypeEnum 校验方式
     * @param identifier 账户
     * @param credential 校验码
     * @return Token
     */
    TokenInfo verifyAndSignToken(String appCode, AuthTypeEnum authTypeEnum, String identifier, String credential);
}
