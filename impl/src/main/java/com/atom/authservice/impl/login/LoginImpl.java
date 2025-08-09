package com.atom.authservice.impl.login;

import cn.hutool.core.bean.BeanUtil;
import com.atom.authservice.api.login.LoginAPI;
import com.atom.authservice.api.login.bean.LoginResult;
import com.atom.authservice.api.login.bean.WechatLoginQrInfo;
import com.atom.authservice.service.login.bean.GetSceneQrCodeParam;
import com.atom.authservice.service.login.bean.SceneStrQrcodeInfo;
import com.atom.authservice.service.login.impl.LoginServiceImpl;
import com.atom.authservice.service.token.model.TokenInfo;
import com.atom.commonsdk.model.CommonResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 登录接口的实现
 *
 * @data: 2025/7/31
 * @author: yang lianhuan
 */
@DubboService
@Slf4j
public class LoginImpl implements LoginAPI {

    @Resource
    private LoginServiceImpl loginService;

    @Override
    public CommonResponse<WechatLoginQrInfo> getPALoginQRCode(String ip, String deviceMac, String deviceName, String appcode) {
        GetSceneQrCodeParam param = new GetSceneQrCodeParam();
        param.setAppCode(appcode);
        param.setDeviceMac(deviceMac);
        param.setDeviceName(deviceName);
        param.setIp(ip);
        SceneStrQrcodeInfo sceneStrQrcodeInfo = loginService.getSceneStrQrcode(param);
        WechatLoginQrInfo loginQrInfo = new WechatLoginQrInfo();
        BeanUtil.copyProperties(sceneStrQrcodeInfo, loginQrInfo);
        return CommonResponse.ofSuccess(loginQrInfo);
    }

    @Override
    public CommonResponse<LoginResult> checkLoginResult(String loginId) {
        TokenInfo tokenInfo = loginService.checkLoginResult(loginId);
        LoginResult loginResult = new LoginResult();
        loginResult.setLoginId(loginId);
        loginResult.setToken(tokenInfo.getToken());
        loginResult.setExpireAt(tokenInfo.getExpiresAt());
        return CommonResponse.ofSuccess(loginResult);
    }
}
