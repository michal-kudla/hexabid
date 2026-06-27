package com.github.hexabid.adapter.in.authz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Konfiguracja JWT i PasswordEncoder dla profilu local.
 * Nie tworzy własnego UserDetailsService — używa localUserDetailsService
 * z hexabid-adapter-in-auth-local.
 */
@Configuration
@Profile("local")
public class AuthzDevAuthConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
