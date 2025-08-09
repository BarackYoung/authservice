package com.atom.authservice.service.wechat.msg;

import com.alibaba.fastjson2.JSON;
import com.atom.authservice.service.wechat.business.WechatLoginProcessor;
import com.atom.commonsdk.wechat.anotation.WechatMsgProcessor;
import com.atom.commonsdk.wechat.message.EventMessage;
import com.atom.commonsdk.wechat.msgprocessor.EventMsgProcessor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 事件消息处理实现
 *
 * @data: 2025/7/15
 * @author: yang lianhuan
 */
@WechatMsgProcessor
@Component
@Slf4j
public class EventMsgProcessorImpl implements EventMsgProcessor {
    private static final String SCAN_EVENT = "SCAN";
    private static final String SUBSCRIBE_EVENT = "subscribe";

    @Resource
    private WechatLoginProcessor wechatLoginProcessor;

    @Override
    public String process(EventMessage eventMessage) {
        log.info("EventMsgProcessorImpl.process, eventMessage:{}", JSON.toJSONString(eventMessage));

        // 处理登录事件消息
        wechatLoginProcessor.processLogin(eventMessage);
        return "success";
    }
}
