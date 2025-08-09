package com.atom.authservice.service.login.bean;

import lombok.Data;

/**
 * 获取场景码请求
 *
 * @data: 2025/8/7
 * @author: yang lianhuan
 */
@Data
public class GetSceneQrCodeParam {
    private String appCode;
    private String deviceMac;
    private String deviceName;
    private String ip;
}
