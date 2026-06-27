package com.github.hexabid.adapter.in.auth.oauth;

import com.github.hexabid.adapter.in.auth.oauth.dev.DevOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Produkcyjna wtyczka uwierzytelniania OAuth2/OpenID Connect.
 *
 * Dostarcza:
 * - SecurityFilterChain z oauth2Login (Google, GitHub)
 * - Konfigurację CSRF z wyłączeniem dla WebSocket
 * - Konfigurację CORS odczytaną z właściwości aplikacji (tylko profil dev)
 *
 * W profilu deweloperskim:
 * - CSRF jest wyłączony
 * - Strona logowania kieruje na /login/dev z lokalnym dostawcą
 * - CORS jest aktywny jeśli zdefiniowano {@code spring.cors.allowed-origins}
 *
 * Na produkcji:
 * - CSRF z CookieCsrfTokenRepository
 * - Strona logowania /login z wyborem dostawcy OAuth2
 * - CORS nieaktywny (frontend i backend z tej samej domeny)
 */
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass("com.github.hexabid.adapter.in.auth.local.LocalSecurityConfiguration")
public class OAuth2SecurityConfiguration {

    @Autowired(required = false)
    private DevOAuth2UserService devOauth2UserService;

    @Autowired
    private Environment environment;

    @Value("${spring.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Value("${spring.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private List<String> allowedMethods;

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean developmentProfile = Arrays.asList(environment.getActiveProfiles()).contains("dev");

        if (developmentProfile) {
            http.csrf(csrf -> csrf.disable());
        } else {
            http.csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers("/ws-auctions/**"));
        }

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource(developmentProfile)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/", "/error", "/login/**", "/dev-auth/**").permitAll()
                        .requestMatchers("/ws-auctions/**").permitAll()
                        .requestMatchers("/h2-console/**").access((authentication, context) ->
                                new org.springframework.security.authorization.AuthorizationDecision(developmentProfile))
                        .requestMatchers(HttpMethod.GET, "/api/auctions", "/api/auctions/*").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> {
                    if (developmentProfile) {
                        oauth2.loginPage("/login/dev");
                        oauth2.userInfoEndpoint(userInfo -> userInfo.userService(devOauth2UserService));
                    } else {
                        oauth2.loginPage("/login");
                    }
                })
                .oauth2Client(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        return http.build();
    }

    private org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource(boolean developmentProfile) {
        if (!developmentProfile || allowedOrigins == null || allowedOrigins.isEmpty()) {
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
}