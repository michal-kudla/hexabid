package com.acme.auctions.adapter.in.auth.oauth;

import com.acme.auctions.adapter.in.auth.oauth.dev.DevOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass("com.acme.auctions.adapter.in.auth.local.LocalSecurityConfiguration")
public class OAuth2SecurityConfiguration {

    @Autowired(required = false)
    private DevOAuth2UserService devOauth2UserService;

    @Autowired
    private Environment environment;

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
}
