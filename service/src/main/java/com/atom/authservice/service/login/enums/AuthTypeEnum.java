package com.atom.authservice.service.login.enums;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证方式枚举
 *
 * @data: 2025/8/6
 * @author: yang lianhuan
 */
@Slf4j
@AllArgsConstructor
public enum AuthTypeEnum {
    PASSWORD("密码登录"),
    MESSAGE("短信验证码"),
    WECHAT_SCAN_QRCODE("微信扫码"),
    WECHAT_PUBLIC_ACCOUNT("微信公众号");

    private final String desc;
}
