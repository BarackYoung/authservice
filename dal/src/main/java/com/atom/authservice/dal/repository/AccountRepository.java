package com.atom.authservice.dal.repository;

import com.atom.authservice.dal.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AccountRepository
 *
 * @data: 2025/8/2
 * @author: yang lianhuan
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    /**
     * 通过账户ID查找
     *
     * @param accountId 账户ID
     * @return 账户
     */
    AccountEntity findByAccountId(String accountId);

}
