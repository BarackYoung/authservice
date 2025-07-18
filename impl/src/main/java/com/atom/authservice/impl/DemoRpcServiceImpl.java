package com.atom.authservice.impl;

import com.atom.authservice.api.DemoRpcService;
import com.atom.authservice.service.RpcCallDemo;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * DemoRpcServiceImpl
 *
 * @data: 2025/7/9
 * @author: yang lianhuan
 */
@DubboService
public class DemoRpcServiceImpl implements DemoRpcService {

    @Resource
    private RpcCallDemo rpcCallDemo;

    @Override
    public String tryRpc(String param) {
        rpcCallDemo.callRpc();
        return "success";
    }
}
