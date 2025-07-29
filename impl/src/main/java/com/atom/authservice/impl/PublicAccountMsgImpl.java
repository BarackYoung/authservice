package com.atom.authservice.impl;

import com.atom.authservice.api.wechat.PublicAccountMsgAPI;
import com.atom.commonsdk.exception.BusinessException;
import com.atom.commonsdk.model.ResultCode;
import com.atom.commonsdk.wechat.WechatMsgService;
import com.atom.commonsdk.wechat.bean.VerifyInfo;
import com.atom.commonsdk.wechat.crypt.AesException;
import com.atom.commonsdk.wechat.crypt.WXBizMsgCrypt;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.remoting.http12.HttpRequest;
import org.apache.dubbo.remoting.http12.HttpResponse;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * PublicAccountMsgImpl
 *
 * @data: 2025/7/27
 * @author: yang lianhuan
 */
@DubboService
@Slf4j
public class PublicAccountMsgImpl implements PublicAccountMsgAPI {
    private static final String SUCCESS = "success";
    private static final String ENCRYPT_KW = "Encrypt";

    @Resource
    private WechatMsgService wechatMsgService;

    @Value("${wechat.public.account.token}")
    private String publicAccountToken;

    @Value("${wechat.public.account.encrypt}")
    private String publicAccountEncrypt;

    @Value("${wechat.public.account.appid}")
    private String publicAccountAppid;

    private WXBizMsgCrypt wxBizMsgCrypt;

    @PostConstruct
    public void init() throws AesException {
        this.wxBizMsgCrypt = new WXBizMsgCrypt(this.publicAccountToken, this.publicAccountEncrypt, this.publicAccountAppid);
    }

    @Override
    public String wechatMsg(HttpRequest request, String signature, String timestamp, String nonce, String echostr, String msgSignature) {
        log.info("wechatMsg.signature:{}, timestamp:{}, nonce:{}, echostr:{}", signature, timestamp, nonce, echostr);
        if (StringUtils.isAnyEmpty(signature, timestamp, nonce, echostr)) {
            throw new BusinessException(ResultCode.INVALID_PARAMS);
        }
        VerifyInfo verifyInfo = new VerifyInfo();
        verifyInfo.setToken(publicAccountToken);
        verifyInfo.setNonce(nonce);
        verifyInfo.setTimestamp(timestamp);
        verifyInfo.setSignature(signature);

        // 配置请求
        if (StringUtils.isEmpty(msgSignature)) {
            HttpResponse response = RpcContext.getServiceContext().getResponse(HttpResponse.class);
            if (wechatMsgService.verify(verifyInfo)) {
                try (OutputStream outputStream = response.outputStream()) {
                    outputStream.write(echostr.getBytes(StandardCharsets.UTF_8));
                    response.commit();
                } catch (Exception e) {
                    log.error("PublicAccountMsgImpl.wechatMsg.verify.error", e);
                }
            } else {
                log.info("wechatMsg.verify.res:{}", false);
                return null;
            }
        }

        // 消息推送
        try {
            byte[] bytes = request.inputStream().readAllBytes();
            String encryptMsgBody = new String(bytes, StandardCharsets.UTF_8);
            log.info("PublicAccountMsgImpl.encryptMsgBody:{}", encryptMsgBody);
            String decryptMsgBody = wxBizMsgCrypt.decryptMsg(msgSignature, timestamp, nonce, encryptMsgBody);
            log.info("PublicAccountMsgImpl.decryptMsgBody:{}", decryptMsgBody);
        } catch (IOException | AesException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        }
        return StringUtils.EMPTY;
    }
}
