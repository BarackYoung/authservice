package com.atom.authservice.impl.wechat;

import com.atom.authservice.api.wechat.PublicAccountMsgTestAPI;
import com.atom.authservice.api.wechat.bean.EventMsgReq;
import com.atom.commonsdk.wechat.message.EventMessage;
import com.atom.commonsdk.wechat.msgprocessor.EventMsgProcessor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;

/**
 * PublicAccountMsgTestImpl
 *
 * @data: 2025/8/9
 * @author: yang lianhuan
 */
@DubboService
@Slf4j
public class PublicAccountMsgTestImpl implements PublicAccountMsgTestAPI {

    @Resource
    private EventMsgProcessor eventMsgProcessor;

    @Override
    public String wechatEventMsg(EventMsgReq eventMsgReq) {
        EventMessage eventMessage = new EventMessage();
        BeanUtils.copyProperties(eventMsgReq, eventMessage);
        return eventMsgProcessor.process(eventMessage);
    }
}
