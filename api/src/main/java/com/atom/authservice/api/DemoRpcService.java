package com.atom.authservice.api;

import org.apache.dubbo.remoting.http12.HttpMethods;
import org.apache.dubbo.remoting.http12.rest.Mapping;
import org.apache.dubbo.remoting.http12.rest.Param;

/**
 * 远程调用示例
 *
 * @data: 2025/7/9
 * @author: yang lianhuan
 */
public interface DemoRpcService {

    @Mapping(path = "/tryRpc", method = HttpMethods.GET)
    String tryRpc(@Param("param") String param);
}
