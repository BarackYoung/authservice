package com.atom.authservice.dal.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录记录表
 *
 * @data: 2025/8/2
 * @author: yang lianhuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "login_record")
public class LoginRecordEntity extends BasicEntity {
    private String account_id;

    private String loginId;

    private String identifier;

    private String authType;

    private String ip;

    private String location;

    private String deviceMac;

    private String deviceName;

    private String requestId;

    /**
     * @see com.atom.authservice.service.login.enums.LoginStatusEnum
     */
    private int status;
}
