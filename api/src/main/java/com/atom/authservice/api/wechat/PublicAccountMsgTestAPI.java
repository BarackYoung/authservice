package com.atom.authservice.api.wechat;

import com.atom.authservice.api.wechat.bean.EventMsgReq;
import org.apache.dubbo.remoting.http12.HttpMethods;
import org.apache.dubbo.remoting.http12.rest.Mapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 微信公众号消息接收测试
 *
 * @data: 2025/8/9
 * @author: yang lianhuan
 */
@Mapping(path = "v1/test/wechat/account")
public interface PublicAccountMsgTestAPI {

    @Mapping(path = "/event/msg", method = HttpMethods.POST)
    String wechatEventMsg(@RequestBody EventMsgReq eventMsgReq);
}
