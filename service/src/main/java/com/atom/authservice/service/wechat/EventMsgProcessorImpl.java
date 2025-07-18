package com.atom.authservice.service.wechat;

import com.atom.commonsdk.wechat.anotation.WechatMsgProcessor;
import com.atom.commonsdk.wechat.message.EventMessage;
import com.atom.commonsdk.wechat.msgprocessor.EventMsgProcessor;
import org.springframework.stereotype.Component;

/**
 * 事件消息处理实现
 *
 * @data: 2025/7/15
 * @author: yang lianhuan
 */
@WechatMsgProcessor
@Component
public class EventMsgProcessorImpl implements EventMsgProcessor {

    @Override
    public void process(EventMessage eventMessage) {

    }
}
