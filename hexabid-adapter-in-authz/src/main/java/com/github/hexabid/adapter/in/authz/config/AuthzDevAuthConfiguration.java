package com.github.hexabid.adapter.in.authz.config;

import com.github.hexabid.adapter.in.authz.dev.DevUserCatalog;
import com.github.hexabid.adapter.in.authz.dev.DevUserEntry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;

/**
 * Konfiguracja dev użytkowników i JWT dla profilu local.
 */
@Configuration
@Profile("local")
public class AuthzDevAuthConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(DevUserCatalog devUserCatalog) {
        List<UserDetails> users = devUserCatalog.users().stream()
                .map(entry -> User.withUsername(entry.getUsername())
                        .password(entry.getPassword())
                        .roles("USER")
                        .build())
                .toList();

        return new InMemoryUserDetailsManager(users);
    }
}
