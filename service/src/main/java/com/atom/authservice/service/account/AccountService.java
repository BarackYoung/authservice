package com.atom.authservice.service.account;

/**
 * 账户服务
 *
 * @data: 2025/8/3
 * @author: yang lianhuan
 */
public interface AccountService {
    /**
     * 生成一个uid
     *
     * @return uid
     */
    String generateUid();

    /**
     * 生成一个账户id
     *
     * @return 账户id
     */
    String generateAccountId(String accountType);
}
