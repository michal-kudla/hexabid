package com.github.hexabid.adapter.in.auth.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Deweloperska wtyczka uwierzytelniania lokalnego.
 *
 * Dostarcza:
 * - InMemoryUserDetailsManager z testowymi użytkownikami
 * - SecurityFilterChain z formLogin + OAuth2 + JWT
 * - Konfigurację CORS odczytaną z właściwości aplikacji (profil local)
 *
 * KLUCZOWE ZASADY (NIE ZMIENIAJ BEZ ZGODY OWNERA):
 * 1. NIE UŻYWAJ httpBasic — Spring Security 7 wysyła WWW-Authenticate header
 *    nawet z HttpStatusEntryPoint, co blokuje OAuth2 redirecty w przeglądarce
 *    (ERR_INVALID_AUTH_CREDENTIALS) i wywołuje natywny dialog logowania.
 * 2. NIE UŻYWAJ SessionCreationPolicy.STATELESS — formLogin i /login/dev wymagają
 *    sesji HTTP. JWT jest dodatkowym mechanizmem, NIE zastępuje sesji.
 * 3. formLogin + oauth2Login są wymagane dla działania /login/dev.
 * 4. /login/**, /logout, /dev-auth/** muszą być permitAll.
 * 5. exceptionHandling musi używać HttpStatusEntryPoint(UNAUTHORIZED) — NIE
 *    przekierowania na /login dla API paths, bo SPA oczekuje 401.
 *
 * Patrz: ai/wiki/decisions/2026-05-05-dev-auth-e2e.md
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
            @Autowired(required = false) com.github.hexabid.adapter.in.auth.oauth.dev.DevOAuth2UserService devOauth2UserService,
            @Autowired(required = false) com.github.hexabid.adapter.in.authz.filter.JwtAuthorizationFilter jwtFilter
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/", "/error", "/login", "/login/**", "/logout", "/dev-auth/**").permitAll()
                        .requestMatchers("/h2-console/**", "/ws-auctions/**").permitAll()
                        .requestMatchers("/api/authz/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auctions", "/api/auctions/*", "/api/auth/providers").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.defaultSuccessUrl("/", true))
                .oauth2Login(oauth2 -> {
                    oauth2.defaultSuccessUrl("/", true);
                    if (devOauth2UserService != null) {
                        oauth2.userInfoEndpoint(userInfo -> userInfo.userService(devOauth2UserService));
                    }
                })
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                ))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        if (jwtFilter != null) {
            http.addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        }

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
    @org.springframework.context.annotation.Primary
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
        UserDetails anna = User.withDefaultPasswordEncoder()
                .username("anna")
                .password("password")
                .roles("AUCTION_AUTHOR")
                .build();
        UserDetails marek = User.withDefaultPasswordEncoder()
                .username("marek")
                .password("password")
                .roles("AUCTION_AUTHOR")
                .build();
        UserDetails piotr = User.withDefaultPasswordEncoder()
                .username("piotr")
                .password("password")
                .roles("AUCTION_MANAGER")
                .build();
        UserDetails barbara = User.withDefaultPasswordEncoder()
                .username("barbara")
                .password("password")
                .roles("REPORT_VIEWER")
                .build();
        return new InMemoryUserDetailsManager(user1, admin, anna, marek, piotr, barbara);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    @org.springframework.context.annotation.Primary
    public org.springframework.security.crypto.password.PasswordEncoder localPasswordEncoder() {
        return org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
