package com.atom.authservice.dal.repository;

import com.atom.authservice.dal.entity.LoginRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 登录记录
 *
 * @data: 2025/8/4
 * @author: yang lianhuan
 */
@Repository
public interface LoginRecordRepository extends JpaRepository<LoginRecordEntity, Long> {
    /**
     * 通过登录ID查找
     *
     * @param loginId 登录ID
     * @return 登录记录
     */
    LoginRecordEntity findByLoginId(String loginId);
}
