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
}
