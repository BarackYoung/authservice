package com.atom.authservice.dal.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用实体信息表
 *
 * @data: 2025/8/5
 * @author: yang lianhuan
 */
@Entity
@Table(name = "app_info")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppInfoEntity extends BasicEntity {
    private String appCode;
    private String appName;
    private String appShortCode;
    private String weOpenAppId;
    private String weMiniprogramAppId;
    private String wePublicAccountAppId;
}
