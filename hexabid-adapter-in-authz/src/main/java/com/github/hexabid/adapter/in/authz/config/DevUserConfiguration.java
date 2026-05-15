package com.github.hexabid.adapter.in.authz.config;

import com.github.hexabid.adapter.in.authz.dev.DevUserCatalog;
import com.github.hexabid.adapter.in.authz.dev.DevUserEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Ładuje dev użytkowników z application-local.yaml.
 */
@Configuration
@Profile("local")
public class DevUserConfiguration {

    @Value("${hexabid.authorization.dev-users:}")
    private String devUsersYaml;

    @Bean
    public DevUserCatalog authzDevUserCatalog() {
        // Parse YAML manually or use a simple approach
        // For now, create users programmatically
        List<DevUserEntry> users = new ArrayList<>();
        users.add(createUser("anna", "password", List.of("AUCTION_AUTHOR"), "A12/B04/C77", "Anna Developer"));
        users.add(createUser("marek", "password", List.of("AUCTION_AUTHOR"), "A12/B04/C77", "Marek Demo"));
        users.add(createUser("piotr", "password", List.of("AUCTION_MANAGER"), "A12/B04", "Piotr Manager"));
        users.add(createUser("barbara", "password", List.of("REPORT_VIEWER"), "A12", "Barbara Viewer"));
        users.add(createUser("admin", "password", List.of("AUCTION_ADMIN"), "A12", "Admin User"));
        return new DevUserCatalog(users);
    }

    private DevUserEntry createUser(String username, String password, List<String> roles, String orgCode, String displayName) {
        return new DevUserEntry(username, password, roles, orgCode, displayName);
    }

    @Bean
    public JwtProperties authzJwtProperties() {
        return new JwtProperties(
                "hexabid-local-development-secret-key-at-least-256-bits-long-for-hmac-sha",
                8
        );
    }

    public static class JwtProperties {
        private final String secret;
        private final long expirationHours;

        public JwtProperties(String secret, long expirationHours) {
            this.secret = secret;
            this.expirationHours = expirationHours;
        }

        public String getSecret() {
            return secret;
        }

        public long getExpirationHours() {
            return expirationHours;
        }
    }
}
