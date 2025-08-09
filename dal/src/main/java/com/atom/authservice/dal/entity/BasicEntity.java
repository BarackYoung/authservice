package com.atom.authservice.dal.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * 必选字段
 *
 * @data: 2025/1/23
 * @author: yang lianhuan
 */
@Data
@MappedSuperclass
public abstract class BasicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private Date createdAt;

    /**
     * 修改时间
     */
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date(); // 创建时设置 createdAt
        this.updatedAt = new Date(); // 创建时也设置 modifiedAt
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date(); // 更新时设置 modifiedAt
    }
}
