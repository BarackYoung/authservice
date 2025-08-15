package com.atom.authservice.service.login.bean;

import com.atom.authservice.service.token.model.TokenInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 鉴权结果
 *
 * @data: 2025/8/15
 * @author: yang lianhuan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private TokenInfo authToken;
    private TokenInfo refreshToken;
}
