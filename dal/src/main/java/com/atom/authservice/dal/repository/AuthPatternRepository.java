package com.atom.authservice.dal.repository;

import com.atom.authservice.dal.entity.AuthPatternEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 登录方式
 *
 * @data: 2025/8/4
 * @author: yang lianhuan
 */
@Repository
public interface AuthPatternRepository extends JpaRepository<AuthPatternEntity, Long> {
    /**
     * 查找一个鉴权方式
     *
     * @param appCode appCode
     * @param identifier identifier
     * @param authType authType
     * @return 鉴权方式
     */
    AuthPatternEntity findByAppCodeAndIdentifierAndAuthType(String appCode, String identifier, String authType);

    /**
     * 通过账户的鉴权方式查找
     *
     * @param appCode appCode
     * @param accountId 账号ID
     * @param authType 鉴权类型
     * @return 鉴权模式
     */
    AuthPatternEntity findByAppCodeAndAccountIdAndAuthType(String appCode, String accountId, String authType);
}
