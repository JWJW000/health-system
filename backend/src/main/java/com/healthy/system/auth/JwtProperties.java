package com.healthy.system.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    /**
     * JWT 签名密钥
     */
    private String secret = "change_this_secret";

    /**
     * 过期时间（分钟）
     */
    private long expireMinutes = 120;
}

