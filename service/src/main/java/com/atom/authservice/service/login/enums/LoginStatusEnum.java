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
    INIT(0),
    SUCCESS(1),
    FAIL(2);

    private final int status;
}
