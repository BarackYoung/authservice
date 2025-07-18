package com.atom.authservice.wechat;

import com.atom.commonsdk.exception.BusinessException;
import com.atom.commonsdk.model.ResultCode;
import com.atom.commonsdk.wechat.WechatMsgService;
import com.atom.commonsdk.wechat.bean.VerifyInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 微信公众号消息类接口
 *
 * @data: 2025/3/1
 * @author: yang lianhuan
 */
@RestController
@RequestMapping("/wechat/msg")
@Slf4j
public class PublicAccountMsgController {

    @Resource
    private WechatMsgService wechatMsgService;

    @Value("${wechat.public.account.token}")
    private String publicAccountToken;

    /**
     * 微信服务器验证接口
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echostr 随机字符串
     * @return 验证成功返回echostr，失败返回错误信息
     */
    @GetMapping()
    public String wechatMsg(@RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
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

    /**
     * 公众号消息入口，所有公众号事件/消息都会调用该接口
     *
     * @param request 请求
     */
    @PostMapping()
    public void wechatMsg(HttpServletRequest request,
                          @RequestParam("signature") String signature,
                          @RequestParam("timestamp") String timestamp,
                          @RequestParam("nonce") String nonce) {
        VerifyInfo verifyInfo = new VerifyInfo();
        verifyInfo.setTimestamp(timestamp);
        verifyInfo.setNonce(nonce);
        verifyInfo.setSignature(signature);
        verifyInfo.setToken(publicAccountToken);
        if (wechatMsgService.verify(verifyInfo)) {
            log.error("PublicAccountMsgController.wechatMsg,invalid msg,ip:{}", request.getRemoteAddr());
            return;
        }
        wechatMsgService.process(request);
    }
}
