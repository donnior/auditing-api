package com.example.demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 一次性工具：通过环境变量生成 BCrypt hash，避免把明文密码写进代码或 migration。
 *
 * 用法：
 *   PLAIN_PASSWORD='xxx' java ... com.example.demo.util.BCryptHashPrinter
 */
public class BCryptHashPrinter {

    public static void main(String[] args) {
        String plain = System.getenv("PLAIN_PASSWORD");
        if (plain == null || plain.isBlank()) {
            System.err.println("Missing env var: PLAIN_PASSWORD");
            System.exit(2);
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode(plain));
    }
}
