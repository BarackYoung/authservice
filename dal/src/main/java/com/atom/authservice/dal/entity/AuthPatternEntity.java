package com.atom.authservice.dal.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 认证方式
 *
 * @data: 2025/8/2
 * @author: yang lianhuan
 */
@Entity
@Table(name = "auth_pattern")
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthPatternEntity extends BasicEntity {
    private String accountId;

    private String appCode;

    /**
     * 登录身份标识，手机、邮箱、账号ID
     */
    private String identifier;

    private String credential;

    /**
     * @see com.atom.authservice.service.login.enums.AuthTypeEnum
     */
    private String authType;

    private Date expireTime;

    private int isDeleted;
}
