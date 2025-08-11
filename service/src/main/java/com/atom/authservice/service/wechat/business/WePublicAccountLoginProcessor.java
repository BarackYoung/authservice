package com.atom.authservice.service.wechat.business;

import com.atom.commonsdk.wechat.message.EventMessage;

/**
 * 收到带场景值二维码关注事件时的登录处理
 *
 * @data: 2025/8/4
 * @author: yang lianhuan
 */
public interface WePublicAccountLoginProcessor {

    /**
     * 处理登录行为
     *
     * @param eventMessage 事件消息
     */
    String processLogin(EventMessage eventMessage);
}
