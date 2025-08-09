package com.atom.authservice.service.account.impl;

import cn.hutool.core.util.StrUtil;
import com.atom.authservice.service.account.AccountService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;
import java.util.zip.CRC32;

/**
 * AccountServiceImpl
 *
 * @data: 2025/8/3
 * @author: yang lianhuan
 */
@Component
public class AccountServiceImpl implements AccountService {
    private static final String SEQUENCE_KEY_PREFIX = "authService:AccountServiceImpl:";

    @Resource
    private RedisTemplate<String, Integer> redisTemplate;

    @Override
    public String generateUid() {
        // 1. 生成14位时间戳 (格式: yyyyMMddHHmmss)
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 2. 生成5位随机数 (00000-99999)
        String randomPart = String.format("%05d", new Random().nextInt(100000));

        // 3. 拼接前19位
        String first19 = timestamp + randomPart;

        // 4. 计算校验位 (前19位数字之和 mod 10)
        int checkSum = 0;
        for (char c : first19.toCharArray()) {
            checkSum += Character.getNumericValue(c);
        }
        char lastChar = (char) ('0' + (checkSum % 10));

        return first19 + lastChar;
    }

    @Override
    public String generateAccountId(String accountType) {
        // 验证账户类型
        if (!accountType.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("账户类型必须是2位大写字母");
        }

        // 获取当前日期
        LocalDate today = LocalDate.now();
        String dateStr = today.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

        // 获取并递增序列号
        String sequenceKey = SEQUENCE_KEY_PREFIX + accountType + StrUtil.COLON + dateStr;
        redisTemplate.opsForValue().setIfAbsent(sequenceKey, 0);
        int seq = Objects.requireNonNull(redisTemplate.opsForValue().increment(sequenceKey, 1)).intValue();;
        if (seq > 99999) {
            throw new IllegalStateException("当日账户数量超过99999上限");
        }
        String sequenceStr = String.format("%05d", seq);

        // 构建前15位
        String prefix = accountType + dateStr + sequenceStr;

        // 计算CRC32校验码
        CRC32 crc = new CRC32();
        crc.update(prefix.getBytes());
        String crcHex = String.format("%08X", crc.getValue());
        String checkDigits = crcHex.substring(6); // 取最后2位十六进制字符

        return prefix + checkDigits;
    }
}
