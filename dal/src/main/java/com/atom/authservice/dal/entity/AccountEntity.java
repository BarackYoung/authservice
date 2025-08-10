package com.atom.authservice.dal.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账户实体
 *
 * @data: 2025/8/2
 * @author: yang lianhuan
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "account")
public class AccountEntity extends BasicEntity {

    private String accountId;

    private String appCode;

    private String uid;

    private String weUnionid;

    private String username;

    private String phone;

    private String email;

    private String avatarUrl;

    private String sex;

    private String city;

    private String province;

    private String country;

    private int status;

    private int isDeleted;
}
