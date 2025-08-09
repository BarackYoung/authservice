package com.atom.authservice.api.login;

import com.atom.authservice.api.login.bean.LoginResult;
import com.atom.authservice.api.login.bean.WechatLoginQrInfo;
import com.atom.commonsdk.model.CommonResponse;
import org.apache.dubbo.remoting.http12.HttpMethods;
import org.apache.dubbo.remoting.http12.rest.Mapping;
import org.apache.dubbo.remoting.http12.rest.Param;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 登录相关API接口，接口是通用设计，即公司所有app登录都使用同一套账户系统
 *
 * @data: 2025/7/31
 * @author: yang lianhuan
 */
@Mapping(path = "v1/login")
public interface LoginAPI {


    /**
     * 获取公众号登录二维码
     *
     * @param ip ip地址
     * @param deviceMac 登录设备MAC
     * @param deviceName 登录设备名
     * @param appcode app名
     * @return 登录信息
     */
    @Mapping(path = "/wechat/public-account/qrcode", method = HttpMethods.GET)
    CommonResponse<WechatLoginQrInfo> getPALoginQRCode(@RequestHeader("X-Request-IP") String ip,
                                                       @RequestHeader("X-Device-Mac") String deviceMac,
                                                       @RequestHeader("X-Device-name") String deviceName,
                                                       @Param(value = "appcode") String appcode);

    /**
     * 检查登录结果，通过loginId检查当前用户的登录结果，轮训接口
     *
     * @param loginId 登录Id
     * @return 登录结果
     */
    @Mapping(path = "/v1/login/result/check", method = HttpMethods.GET)
    CommonResponse<LoginResult> checkLoginResult(@Param("loginId") String loginId);
}
