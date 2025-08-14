package com.atom.authservice.impl;

import com.atom.authservice.api.PublicDemoAPI;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * PublicDemoImpl
 *
 * @data: 2025/8/14
 * @author: yang lianhuan
 */
@DubboService
public class PublicDemoImpl implements PublicDemoAPI {
    @Override
    public String demo(String param) {
        return "hello " + param;
    }
}
