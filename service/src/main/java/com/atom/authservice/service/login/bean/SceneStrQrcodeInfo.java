package com.atom.authservice.service.login.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 带场景值的二维码信息
 *
 * @data: 2025/8/2
 * @author: yang lianhuan
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SceneStrQrcodeInfo {
    private String url;

    private String expireTime;

    private String loginId;

    private String ticket;
}
