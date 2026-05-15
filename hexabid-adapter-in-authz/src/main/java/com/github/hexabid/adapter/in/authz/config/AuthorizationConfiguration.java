package com.github.hexabid.adapter.in.authz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Włącza method security (@PreAuthorize) dla autoryzacji RBAC.
 * <p>
 * SecurityFilterChain jest definiowany w LocalSecurityConfiguration (formLogin + JWT).
 * Ta klasa tylko włącza @PreAuthorize.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AuthorizationConfiguration {
}
