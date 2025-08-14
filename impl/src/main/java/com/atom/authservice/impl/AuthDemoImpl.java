package com.atom.authservice.impl;

import com.atom.authservice.api.AuthDemoAPI;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * AuthDemoImpl
 *
 * @data: 2025/8/14
 * @author: yang lianhuan
 */
@DubboService
public class AuthDemoImpl implements AuthDemoAPI {

    @Override
    public String demo(String param, String ip, String uid, String accountId, String requestId) {
        return "hello " + param + " ip:" + ip + " uid:" + uid + " accountId:" + accountId + " requestId:" + requestId;
    }
}
