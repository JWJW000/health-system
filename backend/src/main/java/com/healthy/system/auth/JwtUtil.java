package com.healthy.system.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
        // 允许用户配置 Base64 字符串，也兼容普通字符串
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(properties.getSecret());
        } catch (Exception e) {
            keyBytes = properties.getSecret().getBytes();
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("uname", username);
        claims.put("role", role);

        Instant now = Instant.now();
        Instant exp = now.plus(properties.getExpireMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }
}

