package com.atom.authservice.api.wechat;

import org.apache.dubbo.remoting.http12.HttpMethods;
import org.apache.dubbo.remoting.http12.HttpRequest;
import org.apache.dubbo.remoting.http12.rest.Mapping;
import org.apache.dubbo.remoting.http12.rest.Param;

/**
 * 微信公众号消息接口
 *
 * @data: 2025/7/27
 * @author: yang lianhuan
 */
@Mapping(path = "v1/wechat/account")
public interface PublicAccountMsgAPI {

    @Mapping(path = "/msg", method = HttpMethods.GET)
    String wechatMsg(@Param(value = "signature", required = false) String signature,
                       @Param(value = "timestamp", required = false) String timestamp,
                       @Param(value = "nonce", required = false) String nonce,
                       @Param(value = "echostr", required = false) String echostr);

    @Mapping(path = "/msg", method = HttpMethods.POST)
    String wechatMsg(HttpRequest request,
                     @Param(value = "signature", required = false) String signature,
                     @Param(value = "timestamp", required = false) String timestamp,
                     @Param(value = "nonce", required = false) String nonce,
                     @Param(value = "msg_signature") String msgSignature);
}
