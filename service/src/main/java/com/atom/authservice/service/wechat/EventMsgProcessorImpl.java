package com.atom.authservice.service.wechat;

import com.alibaba.fastjson2.JSON;
import com.atom.commonsdk.wechat.anotation.WechatMsgProcessor;
import com.atom.commonsdk.wechat.message.EventMessage;
import com.atom.commonsdk.wechat.msgprocessor.EventMsgProcessor;
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

    @Override
    public String process(EventMessage eventMessage) {
        log.info("EventMsgProcessorImpl.process, eventMessage:{}", JSON.toJSONString(eventMessage));
        return "success";
    }
}
