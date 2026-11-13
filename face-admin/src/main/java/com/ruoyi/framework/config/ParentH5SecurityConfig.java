package com.ruoyi.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

/**
 * 家长 H5（微信服务号）公开接口，不走 JWT 认证链。
 */
@Configuration
public class ParentH5SecurityConfig {

    static final String[] PUBLIC_URLS = {
        "/face/user/wx-openid",
        "/face/user/wx-config",
        "/face/user/bind",
        "/face/user/bind-preview",
        "/face/user/by-openid",
        "/face/user/bind-qr",
        "/face/user/register",
        "/face/user/bind-student",
        "/face/parent/**"
    };

    @Bean
    public WebSecurityCustomizer parentH5WebSecurityCustomizer() {
        return web -> web.ignoring().antMatchers(PUBLIC_URLS);
    }
}
