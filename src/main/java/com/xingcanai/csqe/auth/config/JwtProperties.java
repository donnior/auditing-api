package com.xingcanai.csqe.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    /**
     * HMAC 密钥（至少 32 字节）。
     */
    private String secret;

    /**
     * token 过期时间（秒）。
     */
    private long ttlSeconds = 604800;

    /**
     * 本地开发环境绕过认证（务必只在 local/dev profile 开启）。
     */
    private boolean devBypass = false;

    /**
     * devBypass 开启时注入的默认用户名。
     */
    private String devBypassUser = "local-dev";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public boolean isDevBypass() {
        return devBypass;
    }

    public void setDevBypass(boolean devBypass) {
        this.devBypass = devBypass;
    }

    public String getDevBypassUser() {
        return devBypassUser;
    }

    public void setDevBypassUser(String devBypassUser) {
        this.devBypassUser = devBypassUser;
    }
}
