package com.atom.authservice.service.login.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录状态
 *
 * @data: 2025/8/8
 * @author: yang lianhuan
 */
@Getter
@AllArgsConstructor
public enum LoginStatusEnum {
    INIT(0, "初始化"),
    ISSUED(1, "已签发TOKEN"),
    SUCCESS(2, "登录成功"),
    FAIL(3, "登录失败");

    private final int status;
    private final String msg;
}
