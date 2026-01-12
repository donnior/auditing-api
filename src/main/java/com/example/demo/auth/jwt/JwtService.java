package com.example.demo.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import com.example.demo.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        if (props.getSecret() == null || props.getSecret().isBlank()) {
            throw new IllegalStateException("Missing config: security.jwt.secret");
        }
        byte[] bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("security.jwt.secret too short (need >= 32 bytes)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public long ttlSeconds() {
        return props.getTtlSeconds();
    }

    public String issueToken(String username) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getTtlSeconds());
        return Jwts.builder()
            .subject(username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key)
            .compact();
    }

    public Optional<String> parseSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            String sub = claims.getSubject();
            if (sub == null || sub.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(sub);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
