package com.github.hexabid.adapter.in.auth.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Deweloperska wtyczka uwierzytelniania lokalnego.
 *
 * Dostarcza:
 * - InMemoryUserDetailsManager z testowymi użytkownikami
 * - SecurityFilterChain z formLogin (tylko jeśli żaden inny łańcuch nie jest zdefiniowany,
 *   np. gdy adapter auth-oauth NIE jest na classpath)
 * - Konfigurację CORS odczytaną z właściwości aplikacji (profil dev)
 *
 * Wzorzec LISTY: ta wtyczka może współistnieć z innymi dostawcami uwierzytelniania
 * (np. OAuth2). Gdy OAuth2 jest obecny, jego SecurityFilterChain ma pierwszeństwo,
 * a ta klasa dostarcza jedynie użytkowników lokalnych.
 *
 * CORS jest aktywny wyłącznie gdy właściwość {@code spring.cors.allowed-origins}
 * jest zdefiniowana (profil dev). Na produkcji brak CORS — frontend i backend
 * powinny być serwowane z tej samej domeny.
 */
@Configuration
public class LocalSecurityConfiguration {

    @Value("${spring.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Value("${spring.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private List<String> allowedMethods;

    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Autowired(required = false) com.github.hexabid.adapter.in.auth.oauth.dev.DevOAuth2UserService devOauth2UserService
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/", "/error", "/login", "/login/**", "/logout", "/dev-auth/**").permitAll()
                        .requestMatchers("/h2-console/**", "/ws-auctions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auctions", "/api/auctions/*", "/api/auth/providers").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.realmName("hexabid"))
                .formLogin(form -> form.defaultSuccessUrl("/", true))
                .oauth2Login(oauth2 -> {
                    oauth2.defaultSuccessUrl("/", true);
                    if (devOauth2UserService != null) {
                        oauth2.userInfoEndpoint(userInfo -> userInfo.userService(devOauth2UserService));
                    }
                })
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            return request -> null;
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public InMemoryUserDetailsManager localUserDetailsService() {
        UserDetails user1 = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user1, admin);
    }
}