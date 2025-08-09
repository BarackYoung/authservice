package com.atom.authservice.dal.repository;

import com.atom.authservice.dal.entity.AppInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AppInfoRepository
 *
 * @data: 2025/8/5
 * @author: yang lianhuan
 */
@Repository
public interface AppInfoRepository extends JpaRepository<AppInfoEntity, Long> {

    /**
     * 通过appCode查询
     *
     * @param appCode appCode
     * @return AppInfo
     */
    AppInfoEntity findByAppCode(String appCode);

    /**
     * 通过id查询
     *
     * @param id id
     * @return AppInfo
     */
    AppInfoEntity findById(long id);

    /**
     * 通过APP代号查找
     *
     * @param appShortCode app代号
     * @return app信息
     */
    AppInfoEntity findByAppShortCode(String appShortCode);
}
