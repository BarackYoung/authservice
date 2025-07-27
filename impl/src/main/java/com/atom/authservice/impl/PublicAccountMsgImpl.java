package com.atom.authservice.impl;

import com.atom.authservice.api.wechat.PublicAccountMsgAPI;
import com.atom.commonsdk.exception.BusinessException;
import com.atom.commonsdk.model.ResultCode;
import com.atom.commonsdk.wechat.WechatMsgService;
import com.atom.commonsdk.wechat.bean.VerifyInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.remoting.http12.HttpRequest;
import org.springframework.beans.factory.annotation.Value;

/**
 * PublicAccountMsgImpl
 *
 * @data: 2025/7/27
 * @author: yang lianhuan
 */
@DubboService
@Slf4j
public class PublicAccountMsgImpl implements PublicAccountMsgAPI {

    @Resource
    private WechatMsgService wechatMsgService;

    @Value("${wechat.public.account.token}")
    private String publicAccountToken;

    @Override
    public String wechatMsg(String signature, String timestamp, String nonce, String echostr) {
        log.info("wechatMsg.signature:{}, timestamp:{}, nonce:{}", signature, timestamp, nonce);
        if (StringUtils.isAnyEmpty(signature, timestamp, nonce, echostr)) {
            throw new BusinessException(ResultCode.INVALID_PARAMS);
        }
        VerifyInfo verifyInfo = new VerifyInfo();
        verifyInfo.setToken(publicAccountToken);
        verifyInfo.setEchostr(echostr);
        verifyInfo.setNonce(nonce);
        verifyInfo.setTimestamp(timestamp);
        verifyInfo.setSignature(signature);
        if (wechatMsgService.verify(verifyInfo)) {
            return echostr;
        }
        return StringUtils.EMPTY; // 验证失败
    }

    @Override
    public String wechatMsg(HttpRequest request, String signature, String timestamp, String nonce) {
        VerifyInfo verifyInfo = new VerifyInfo();
        verifyInfo.setTimestamp(timestamp);
        verifyInfo.setNonce(nonce);
        verifyInfo.setSignature(signature);
        verifyInfo.setToken(publicAccountToken);
        if (!wechatMsgService.verify(verifyInfo)) {
            log.error("PublicAccountMsgController.wechatMsg,invalid msg,ip:{}", request.remoteAddr());
            return StringUtils.EMPTY;
        }
        return wechatMsgService.process(request.inputStream());
    }
}
