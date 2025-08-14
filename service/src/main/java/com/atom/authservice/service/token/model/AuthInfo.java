package com.atom.authservice.service.token.model;

import com.atom.authservice.service.utils.AuthInfoConstant;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 鉴权信息
 *
 * @data: 2025/1/20
 * @author: yang lianhuan
 */
@Data
public class AuthInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主体appCode
     */
    private String subject;

    /**
     * 签发者appName
     */
    private String issuer;

    /**
     * 签发给谁(accountId)
     */
    private String issuerFor;

    /**
     * uid
     */
    private String uid;

    /**
     * 允许的操作，用户ID:资源ID/all:all/read/write
     */
    private List<String> allow;

    /**
     * 签发时间
     */
    private Date issueAt;

    /**
     * 到期时间
     */
    private Date expireAt;

    /**
     * 获取签名头部
     *
     * @return 头部
     */
    public Map<String, Object> generateHeader() {
        Map<String,Object> header = new HashMap<>();
        header.put(AuthInfoConstant.ISSUE_AT, this.issueAt);
        header.put(AuthInfoConstant.EXPIRE_AT, this.expireAt);
        return header;
    }

    /**
     * 获取body
     *
     * @return body
     */
    public Map<String, Object> generateClaims() {
        Map<String,Object> claims = new HashMap<>();
        claims.put(AuthInfoConstant.SUBJECT, this.subject);
        claims.put(AuthInfoConstant.ISSUE_FOR,this.issuerFor);
        claims.put(AuthInfoConstant.ISSUER, this.issuer);
        claims.put(AuthInfoConstant.ALLOW, allow);
        claims.put(AuthInfoConstant.UID, this.uid);
        return claims;
    }
}
