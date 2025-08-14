package com.atom.authservice.api;

import org.apache.dubbo.remoting.http12.rest.Mapping;
import org.apache.dubbo.remoting.http12.rest.Param;

/**
 * 鉴权演示
 *
 * @data: 2025/8/14
 * @author: yang lianhuan
 */
@Mapping(path = "/v1")
public interface AuthDemoAPI {
    @Mapping("/demo")
    String demo(@Param("param") String param);
}
