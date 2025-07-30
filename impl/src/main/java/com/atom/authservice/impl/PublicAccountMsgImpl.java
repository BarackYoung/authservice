package com.atom.authservice.impl;

import com.atom.authservice.api.wechat.PublicAccountMsgAPI;
import com.atom.commonsdk.exception.BusinessException;
import com.atom.commonsdk.model.ResultCode;
import com.atom.commonsdk.wechat.WechatMsgService;
import com.atom.commonsdk.wechat.bean.VerifyInfo;
import com.atom.commonsdk.wechat.bean.WechatMsg;
import com.atom.commonsdk.wechat.enums.CryptType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.remoting.http12.HttpRequest;
import org.apache.dubbo.remoting.http12.HttpResponse;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
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

    @Override
    public String wechatMsg(String signature, String timestamp, String nonce, String echostr) {
        log.info("wechatMsg.signature:{}, timestamp:{}, nonce:{}, echostr:{}", signature, timestamp, nonce, echostr);
        if (StringUtils.isAnyEmpty(signature, timestamp, nonce, echostr)) {
            throw new BusinessException(ResultCode.INVALID_PARAMS);
        }
        VerifyInfo verifyInfo = new VerifyInfo();
        verifyInfo.setToken(publicAccountToken);
        verifyInfo.setNonce(nonce);
        verifyInfo.setTimestamp(timestamp);
        verifyInfo.setSignature(signature);

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
        return StringUtils.EMPTY;
    }

    @Override
    public String wechatMsg(String signature, String timestamp, String nonce, String msgSignature, HttpRequest request) {
        try (InputStream inputStream = request.inputStream()) {
            String postData = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.info("PublicAccountMsgImpl.postData:{}", postData);
            WechatMsg wechatMsg = new WechatMsg();
            wechatMsg.setMsg_signature(msgSignature);
            wechatMsg.setNonce(nonce);
            wechatMsg.setToken(publicAccountToken);
            wechatMsg.setTimestamp(timestamp);
            wechatMsg.setSignature(signature);
            wechatMsg.setCryptType(CryptType.ENCODED);
            wechatMsg.setXmlMsgContent(postData);
            return wechatMsgService.process(wechatMsg);
        } catch (Exception e) {
            log.info("PublicAccountMsgImpl.wechatMsg.process.error", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        }
    }
}
