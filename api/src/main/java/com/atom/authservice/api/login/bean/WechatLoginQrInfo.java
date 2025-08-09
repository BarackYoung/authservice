package com.atom.authservice.api.login.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信二维码登录信息
 *
 * @data: 2025/7/31
 * @author: yang lianhuan
 */
@Data
public class WechatLoginQrInfo implements Serializable {
    private String url;

    private String expireTime;

    private String loginId;

    private String ticket;
}
