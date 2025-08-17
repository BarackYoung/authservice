package com.atom.authservice.service.login.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.alibaba.fastjson2.JSON;
import com.atom.authservice.dal.entity.AccountEntity;
import com.atom.authservice.dal.entity.AppInfoEntity;
import com.atom.authservice.dal.entity.AuthPatternEntity;
import com.atom.authservice.dal.entity.LoginRecordEntity;
import com.atom.authservice.dal.repository.AccountRepository;
import com.atom.authservice.dal.repository.AppInfoRepository;
import com.atom.authservice.dal.repository.AuthPatternRepository;
import com.atom.authservice.dal.repository.LoginRecordRepository;
import com.atom.authservice.service.login.bean.AuthResult;
import com.atom.authservice.service.login.bean.GetSceneQrCodeParam;
import com.atom.authservice.service.login.bean.SceneStrQrcodeInfo;
import com.atom.authservice.service.login.enums.LoginStatusEnum;
import com.atom.authservice.service.token.KeyHolder;
import com.atom.authservice.service.token.model.TokenInfo;
import com.atom.authservice.service.login.LoginService;
import com.atom.authservice.service.login.enums.AuthTypeEnum;
import com.atom.authservice.service.token.JWTIssuer;
import com.atom.authservice.service.token.model.AuthInfo;
import com.atom.authservice.service.wechat.business.WechatAccessTokenHolder;
import com.atom.commonsdk.exception.BusinessException;
import com.atom.commonsdk.model.ResultCode;
import com.atom.commonsdk.utils.AssertUtils;
import com.atom.commonsdk.utils.IpLocationUtils;
import com.atom.commonsdk.wechat.WeQrCodeService;
import com.atom.commonsdk.wechat.bean.request.CreateQrCodeRequest;
import com.atom.commonsdk.wechat.bean.response.CreateQrCodeResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * LoginServiceImpl
 *
 * @data: 2025/8/2
 * @author: yang lianhuan
 */
@Component
@Slf4j
public class LoginServiceImpl implements LoginService {
    private static final String SCENE_STR_PREFIX = "LOGIN_";
    private static final String ACTION_NAME = "QR_STR_SCENE";
    private static final String TIME_PATTERN = "yyyyMMddHHmmss";
    private static final int SCENE_STR_RANDOM_LEN = 10;
    private static final String ISSUER = "authService";
    private static final String EXP = "exp";
    private static final String NBF = "nbf";
    private static final String LOGIN_CACHE_PREFIX = "authsService:LoginServiceImpl:";
    private static final String ISSUER_KW = "issuer";
    private static final String SUBJECT_KW = "subject";
    private static final String ISSUER_FOR_KW = "issuerFor";
    private static final String UID_KW = "uid";

    @Resource
    private WeQrCodeService qrCodeService;

    @Resource
    private AppInfoRepository appInfoRepository;

    @Resource
    private WechatAccessTokenHolder wechatAccessTokenHolder;

    @Resource
    private AccountRepository accountRepository;

    @Resource
    private AuthPatternRepository authPatternRepository;

    @Resource
    private JWTIssuer jwtissuer;

    @Resource
    private KeyHolder keyHolder;

    @Resource
    private LoginRecordRepository loginRecordRepository;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public SceneStrQrcodeInfo getSceneStrQrcode(GetSceneQrCodeParam param) {
        // 获取app配置
        AppInfoEntity appInfoEntity = appInfoRepository.findByAppCode(param.getAppCode());
        if (Objects.isNull(appInfoEntity) || StrUtil.isEmpty(appInfoEntity.getWePublicAccountAppId())) {
            log.error("LoginServiceImpl.getSceneStrQrcode,appCode:{},wechat public account appId is null", param.getAppCode());
            throw new BusinessException(ResultCode.BUSINESS_ERROR);
        }

        // 调用微信获取带场景值的二维码
        String appid = appInfoEntity.getWePublicAccountAppId();
        String accessToken = wechatAccessTokenHolder.getAccessToken(appid);
        AssertUtils.assertNotNull(accessToken, ResultCode.BUSINESS_ERROR);
        String sceneStr = buildSceneStr(appInfoEntity.getAppShortCode());
        CreateQrCodeRequest createQrCodeRequest = buildCreateQrCodeRequest(sceneStr);
        CreateQrCodeResponse createQrCodeResponse = qrCodeService.createQrCode(accessToken, createQrCodeRequest);
        log.info("LoginServiceImpl.getSceneStrQrcode,loginId:{},createQrCodeResponse:{}", sceneStr, createQrCodeResponse);
        AssertUtils.assertAllNotNull(createQrCodeResponse, createQrCodeResponse.getUrl(), createQrCodeResponse.getExpire_seconds());

        // 设置登录记录
        recordLogin(buildLoginRecordEntity(param, sceneStr));

        // 返回登录二维码
        return SceneStrQrcodeInfo.builder()
                .loginId(sceneStr)
                .url(createQrCodeResponse.getUrl())
                .expireTime(createQrCodeResponse.getExpire_seconds())
                .ticket(createQrCodeResponse.getTicket())
                .build();
    }

    @Override
    public AuthResult checkLoginResult(String sceneStr) {
        String loginCacheKey = LOGIN_CACHE_PREFIX + sceneStr;
        return (AuthResult) redisTemplate.opsForValue().get(loginCacheKey);
    }

    @Override
    public void setLoginResult(String appCode, String accountId, String sceneStr, AuthResult authResult) {
        log.info("LoginServiceImpl.setLoginResult,appCode:{},accountId:{},sceneStr:{},authResult:{}", appCode,
                accountId, sceneStr, authResult);
        // 设置缓存
        String loginCacheKey = LOGIN_CACHE_PREFIX + sceneStr;
        try {
            redisTemplate.opsForValue().set(loginCacheKey, authResult, 180, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("LoginServiceImpl.setLoginResult,error,loginCacheKey:{}", loginCacheKey, e);
            redisTemplate.opsForValue().set(loginCacheKey, authResult, 180, TimeUnit.SECONDS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountEntity registerAccount(AccountEntity accountEntity, AuthPatternEntity authPatternEntity) {
        log.info("LoginServiceImpl.registerAccount,accountEntity:{}，authPatternEntity:{}", accountEntity, authPatternEntity);
        AssertUtils.assertAllNotNull(accountEntity, authPatternEntity);
        AssertUtils.assertNull(accountEntity.getId(), ResultCode.BUSINESS_ERROR);
        AssertUtils.assertNull(authPatternEntity.getId(), ResultCode.BUSINESS_ERROR);
        AccountEntity savedAccountEntity = accountRepository.save(accountEntity);
        AuthPatternEntity savedAuthPatternEntity = authPatternRepository.save(authPatternEntity);
        AssertUtils.assertNotNull(savedAccountEntity, ResultCode.BUSINESS_ERROR);
        AssertUtils.assertNotNull(savedAuthPatternEntity, ResultCode.BUSINESS_ERROR);
        return savedAccountEntity;
    }

    @Override
    public AuthPatternEntity newAuthPattern(AuthPatternEntity authPatternEntity) {
        return authPatternRepository.save(authPatternEntity);
    }

    @Override
    public AuthPatternEntity queryAuthPattern(String appCode, String identifier, AuthTypeEnum authTypeEnum) {
        return authPatternRepository.findByAppCodeAndIdentifierAndAuthType(appCode, identifier, authTypeEnum.name());
    }

    @Override
    public AuthPatternEntity updateAuthPattern(AuthPatternEntity authPatternEntity) {
        AssertUtils.assertNotNull(authPatternEntity.getId(), ResultCode.BUSINESS_ERROR);
        return authPatternRepository.save(authPatternEntity);
    }

    @Override
    public AuthResult verifyAndSignToken(String appCode, AuthTypeEnum authType, String identifier, String credential) {
        AuthPatternEntity authPattern = authPatternRepository.findByAppCodeAndIdentifierAndAuthType(appCode, identifier, authType.name());
        log.info("LoginServiceImpl.verifyAndSignToken,authPattern:{}", authPattern);
        if (Objects.isNull(authPattern)) {
            return null;
        }

        AccountEntity accountEntity = accountRepository.findByAccountId(authPattern.getAccountId());
        AssertUtils.assertNotNull(accountEntity, ResultCode.BUSINESS_ERROR);

        // 校验通过，签发token
        if (verifyAuthPattern(authPattern, credential)) {
            AuthInfo authInfo = new AuthInfo();
            authInfo.setIssuerFor(authPattern.getAccountId());
            authInfo.setUid(accountEntity.getUid());
            authInfo.setSubject(authPattern.getAppCode());
            authInfo.setIssuer(ISSUER);
            return getAuthResult(authInfo);
        }
        return null;
    }

    @Override
    public AuthResult refreshToken(String refreshToken) {
        AssertUtils.assertTrue(verifyRefreshToken(refreshToken), ResultCode.INVALID_PARAMS, "refreshToken不正确或已过期");
        JWT jwt = JWTUtil.parseToken(refreshToken);
        log.info("AuthGlobalFilter.filter,jwt token:{}", JSON.toJSONString(jwt));
        AuthInfo authInfo = new AuthInfo();
        authInfo.setIssuer(String.valueOf(jwt.getPayload(ISSUER)));
        authInfo.setSubject(String.valueOf(jwt.getPayload(SUBJECT_KW)));
        authInfo.setIssuerFor(String.valueOf(jwt.getPayload(ISSUER_FOR_KW)));
        authInfo.setUid(String.valueOf(jwt.getPayload(UID_KW)));
        log.info("LoginServiceImpl.refreshToken,authInfo:{}", JSON.toJSONString(authInfo));
        return getAuthResult(authInfo);
    }

    private AuthResult getAuthResult(AuthInfo authInfo) {
        long currentTime = System.currentTimeMillis();
        authInfo.setIssueAt(new Date(currentTime));
        long expireTime = currentTime + 3600000;
        authInfo.setIssueAt(new Date(currentTime));
        authInfo.setExpireAt(new Date(expireTime));
        TokenInfo authToken = jwtissuer.generateToken(authInfo, keyHolder.getCurrentKeyPair().getPrivateKey());

        AuthInfo refreshAuthInfo = new AuthInfo();
        BeanUtils.copyProperties(authToken, refreshAuthInfo);
        long refreshExpireTime = currentTime + 24 * 60 * 60 * 1000;
        refreshAuthInfo.setExpireAt(new Date(refreshExpireTime));
        TokenInfo refreshTokenInfo = jwtissuer.generateToken(refreshAuthInfo, keyHolder.getRefreshKeyPair().getPrivateKey());

        return new AuthResult(authToken, refreshTokenInfo);
    }

    @Override
    public void updateLoginRecord(String loginId, String accountId, String identifier, LoginStatusEnum loginStatus) {
        LoginRecordEntity loginRecordEntity = loginRecordRepository.findByLoginId(loginId);
        if (Objects.isNull(loginRecordEntity)) {
            return;
        }
        if (StrUtil.isNotBlank(accountId)) {
            loginRecordEntity.setAccount_id(accountId);
        }
        if (StrUtil.isNotBlank(identifier)) {
            loginRecordEntity.setIdentifier(identifier);
        }
        if (loginStatus != null) {
            loginRecordEntity.setStatus(loginStatus.getStatus());

        }
        loginRecordRepository.save(loginRecordEntity);
    }

    @Override
    public boolean verifyRefreshToken(String token) {
        log.info("LoginServiceImpl.verifyRefreshToken,token:{}", token);
        // 1. 解析 JWT
        JWT jwt;
        try {
            jwt = JWT.of(token);
            log.info("verifyRefreshToken:{}", JSON.toJSONString(jwt));
        } catch (Exception e) {
            return false;
        }

        // 2. 验证签名
        JWTSigner signer = JWTSignerUtil.rs512(keyHolder.getRefreshKeyPair().getPublicKey());
        if (!jwt.verify(signer)) {
            log.info("verifyRefreshToken.signer:{}", JSON.toJSONString(jwt));
            return false;
        }

        // 3. 手动解析时间字段
        Date now = new Date();
        Date exp = jwt.getPayloads().getDate(EXP);
        Date nbf = jwt.getPayloads().getDate(NBF);

        // 4. 检查 Token 是否已过期
        if (exp != null && now.after(exp)) {
            log.info("verifyRefreshToken.exp:{},nbf:{}", exp, nbf);
            return false;
        }

        // 5. 检查 Token 是否已生效
        if (nbf != null && now.before(nbf)) {
            log.info("verifyRefreshToken.exp:{},nbf:{}", exp, nbf);
            return false;
        }

        return true;
    }

    private LoginRecordEntity buildLoginRecordEntity(GetSceneQrCodeParam request, String sceneStr) {
        LoginRecordEntity loginRecordEntity = new LoginRecordEntity();
        loginRecordEntity.setAuthType(AuthTypeEnum.WECHAT_PUBLIC_ACCOUNT.name());
        loginRecordEntity.setIp(request.getIp());
        loginRecordEntity.setDeviceMac(request.getDeviceMac());
        loginRecordEntity.setDeviceName(request.getDeviceName());
        loginRecordEntity.setIp(request.getIp());
        String location = IpLocationUtils.getLocation(loginRecordEntity.getIp());
        loginRecordEntity.setLocation(location);
        loginRecordEntity.setLoginId(sceneStr);
        loginRecordEntity.setStatus(LoginStatusEnum.INIT.getStatus());
        return loginRecordEntity;
    }

    private CreateQrCodeRequest buildCreateQrCodeRequest(String sceneStr) {
        CreateQrCodeRequest.Scene scene = CreateQrCodeRequest.Scene.builder()
                .scene_str(sceneStr)
                .build();
        CreateQrCodeRequest.ActionInfo actionInfo = CreateQrCodeRequest.ActionInfo.builder()
                .scene(scene)
                .build();
        return CreateQrCodeRequest.builder()
                .expire_seconds(180)
                .action_name(ACTION_NAME)
                .action_info(actionInfo)
                .build();
    }

    private void recordLogin(LoginRecordEntity loginRecordEntity) {
        loginRecordRepository.save(loginRecordEntity);
    }

    private boolean verifyAuthPattern(AuthPatternEntity authPattern, String credential) {
        AuthTypeEnum authTypeEnum = AuthTypeEnum.valueOf(authPattern.getAuthType());
        if (authTypeEnum == AuthTypeEnum.PASSWORD) {
            return BCrypt.checkpw(credential, authPattern.getCredential());
        }
        Date expireTime = authPattern.getExpireTime();
        Date now = new Date();
        return now.before(expireTime) && StrUtil.equals(credential, authPattern.getCredential());
    }

    private String buildSceneStr(String appShortCode) {
        LocalDateTime now = LocalDateTime.now();
        String randomString = RandomUtil.randomString(SCENE_STR_RANDOM_LEN);
        String timestamp = now.format(DateTimeFormatter.ofPattern(TIME_PATTERN));
        return SCENE_STR_PREFIX + appShortCode + StrUtil.UNDERLINE + timestamp + StrUtil.UNDERLINE + randomString;
    }
}
