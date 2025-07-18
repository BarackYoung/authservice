package com.atom.authservice.common.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求上下文工具
 *
 * @data: 2025/3/3
 * @author: yang lianhuan
 */
public class HttpContextUtils {
    /**
     * 获取当前请求的 HttpServletResponse
     * 注意：只能在请求线程中使用（如 Controller、Service 等）
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("当前线程非 Web 请求上下文");
        }
        return attributes.getResponse();
    }
}
