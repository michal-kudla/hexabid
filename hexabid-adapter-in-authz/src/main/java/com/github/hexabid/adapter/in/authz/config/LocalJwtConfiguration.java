package com.github.hexabid.adapter.in.authz.config;

import com.github.hexabid.adapter.in.authz.jwt.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Konfiguracja JWT dla profilu local/dev.
 */
@Configuration
@Profile("local")
public class LocalJwtConfiguration {

    @Bean
    public JwtTokenUtil jwtTokenUtil(DevUserConfiguration.JwtProperties jwtProperties) {
        return new JwtTokenUtil(jwtProperties.getSecret(), jwtProperties.getExpirationHours());
    }
}
