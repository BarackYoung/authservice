package com.atom.authservice.service.token.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 鉴权信息
 *
 * @data: 2025/8/3
 * @author: yang lianhuan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String token;

    private AuthInfo authInfo;
}
