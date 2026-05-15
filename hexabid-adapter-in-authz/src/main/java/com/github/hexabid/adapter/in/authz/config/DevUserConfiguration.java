package com.github.hexabid.adapter.in.authz.config;

import com.github.hexabid.adapter.in.authz.dev.DevUserCatalog;
import com.github.hexabid.adapter.in.authz.dev.DevUserEntry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Ładuje dev użytkowników z application-local.yaml.
 */
@Configuration
@Profile("local")
public class DevUserConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "hexabid.authorization")
    public DevUserCatalog devUserCatalog(List<DevUserEntry> devUsers) {
        return new DevUserCatalog(devUsers);
    }

    @Bean
    @ConfigurationProperties(prefix = "hexabid.authorization.jwt")
    public JwtProperties jwtProperties() {
        return new JwtProperties(null, 0);
    }

    /**
     * Właściwości JWT z konfiguracji YAML.
     */
    public static class JwtProperties {
        private String secret;
        private long expirationHours;

        public JwtProperties() {
        }

        public JwtProperties(String secret, long expirationHours) {
            this.secret = secret;
            this.expirationHours = expirationHours;
        }

        public String getSecret() {
            return secret != null ? secret : "hexabid-local-development-secret-key-at-least-256-bits-long-for-hmac-sha";
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpirationHours() {
            return expirationHours > 0 ? expirationHours : 8;
        }

        public void setExpirationHours(long expirationHours) {
            this.expirationHours = expirationHours;
        }
    }
}
