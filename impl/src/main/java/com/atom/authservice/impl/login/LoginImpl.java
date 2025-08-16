package com.atom.authservice.impl.login;

import cn.hutool.core.bean.BeanUtil;
import com.atom.authservice.api.login.LoginAPI;
import com.atom.authservice.api.login.bean.LoginResultResp;
import com.atom.authservice.api.login.bean.WechatLoginQrcodeResp;
import com.atom.authservice.service.login.bean.AuthResult;
import com.atom.authservice.service.login.bean.GetSceneQrCodeParam;
import com.atom.authservice.service.login.bean.SceneStrQrcodeInfo;
import com.atom.authservice.service.login.enums.LoginStatusEnum;
import com.atom.authservice.service.login.impl.LoginServiceImpl;
import com.atom.commonsdk.model.CommonResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Objects;

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
    public CommonResponse<WechatLoginQrcodeResp> getPALoginQRCode(String ip, String deviceMac, String deviceName, String appcode) {
        GetSceneQrCodeParam param = new GetSceneQrCodeParam();
        param.setAppCode(appcode);
        param.setDeviceMac(deviceMac);
        param.setDeviceName(deviceName);
        param.setIp(ip);
        SceneStrQrcodeInfo sceneStrQrcodeInfo = loginService.getSceneStrQrcode(param);
        WechatLoginQrcodeResp loginQrInfo = new WechatLoginQrcodeResp();
        BeanUtil.copyProperties(sceneStrQrcodeInfo, loginQrInfo);
        return CommonResponse.ofSuccess(loginQrInfo);
    }

    @Override
    public CommonResponse<LoginResultResp> checkLoginResult(String loginId) {
        AuthResult authResult = loginService.checkLoginResult(loginId);
        if (Objects.isNull(authResult) || Objects.isNull(authResult.getAuthToken()) || Objects.isNull(authResult.getRefreshToken())) {
            return CommonResponse.ofSuccess();
        }
        LoginResultResp loginResultResp = new LoginResultResp();
        loginResultResp.setAuthToken(authResult.getAuthToken());
        loginResultResp.setRefreshToken(authResult.getRefreshToken());

        // 设置登录成功记录
        loginService.updateLoginRecord(loginId, null, null, LoginStatusEnum.ISSUED);
        return CommonResponse.ofSuccess(loginResultResp);
    }

    @Override
    public CommonResponse<LoginResultResp> refreshLoginResult(String refreshToken) {
        AuthResult authResult = loginService.refreshToken(refreshToken);
        LoginResultResp loginResultResp = new LoginResultResp();
        loginResultResp.setAuthToken(authResult.getAuthToken());
        loginResultResp.setRefreshToken(authResult.getRefreshToken());
        return CommonResponse.ofSuccess(loginResultResp);
    }
}
