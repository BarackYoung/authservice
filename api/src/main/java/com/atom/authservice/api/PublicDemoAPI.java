package com.atom.authservice.api;

import org.apache.dubbo.remoting.http12.rest.Mapping;
import org.apache.dubbo.remoting.http12.rest.Param;

/**
 * 免鉴权演示
 *
 * @data: 2025/8/14
 * @author: yang lianhuan
 */
@Mapping(path = "/public/v1")
public interface PublicDemoAPI {

    @Mapping("/demo")
    String demo(@Param("param") String param);
}
