package com.atom.authservice.api.login.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录结果
 *
 * @data: 2025/7/31
 * @author: yang lianhuan
 */
@Data
public class LoginResultResp implements Serializable {
    private Object authToken;

    private Object refreshToken;
}
