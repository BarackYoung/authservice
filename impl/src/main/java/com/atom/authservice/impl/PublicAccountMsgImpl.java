package com.atom.authservice.impl;

import com.alibaba.fastjson2.JSON;
import com.atom.authservice.api.wechat.PublicAccountMsgAPI;
import com.atom.commonsdk.exception.BusinessException;
import com.atom.commonsdk.model.ResultCode;
import com.atom.commonsdk.wechat.WechatMsgService;
import com.atom.commonsdk.wechat.bean.VerifyInfo;
import com.atom.commonsdk.wechat.enums.BodyFormat;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.remoting.http12.HttpRequest;
import org.apache.dubbo.remoting.http12.HttpResponse;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
        }
        log.info("wechatMsg.verify.res:{}", false);
        return StringUtils.EMPTY;
    }

    @Override
    public String wechatMsg(HttpRequest request, String msgSignature, String timestamp, String nonce) {
        VerifyInfo verifyInfo = new VerifyInfo();
        verifyInfo.setTimestamp(timestamp);
        verifyInfo.setNonce(nonce);
        verifyInfo.setMsg_signature(msgSignature);
        verifyInfo.setToken(publicAccountToken);
        Map<String, String> msgBody = JSON.parseObject(request.inputStream(), Map.class);
        String encrypt = msgBody.get(ENCRYPT_KW);
        verifyInfo.setEncrypt(encrypt);
        log.info("PublicAccountMsgImpl.wechatMsg,verifyInfo:{}", verifyInfo);
        InputStream inputStream = new ByteArrayInputStream(JSON.toJSONString(msgBody).getBytes(StandardCharsets.UTF_8));
        if (wechatMsgService.verifySafeMode(verifyInfo)) {
            String result = wechatMsgService.process(BodyFormat.JSON, inputStream);
            HttpResponse response = RpcContext.getServiceContext().getResponse(HttpResponse.class);
            try  (OutputStream outputStream = response.outputStream()){
                outputStream.write(result.getBytes(StandardCharsets.UTF_8));
                response.commit();
            } catch (Exception e) {
                log.error("PublicAccountMsgImpl.wechatMsg.verify.error", e);
            }
        } else {
            log.error("PublicAccountMsgController.wechatMsg,invalid msg,ip:{}", request.remoteAddr());
        }
        return StringUtils.EMPTY;
    }
}
