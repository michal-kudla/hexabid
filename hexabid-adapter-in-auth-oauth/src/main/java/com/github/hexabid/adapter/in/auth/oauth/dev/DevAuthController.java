package com.github.hexabid.adapter.in.auth.oauth.dev;

import com.github.hexabid.adapter.in.auth.oauth.OAuth2AuthenticatedUser;
import com.github.hexabid.auth.core.identityaccess.model.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
class DevAuthController {

    private final DevUserCatalog devUserCatalog;
    private final String defaultRedirectUri;

    DevAuthController(
            DevUserCatalog devUserCatalog,
            @Value("${hexabid.dev-auth.default-redirect-uri:http://localhost:14200/}") String defaultRedirectUri
    ) {
        this.devUserCatalog = devUserCatalog;
        this.defaultRedirectUri = defaultRedirectUri;
    }

    @GetMapping("/login/dev")
    ModelAndView devLoginPage(@RequestParam(required = false) String redirect) {
        return new ModelAndView("dev-login", Map.of(
                "users", devUserCatalog.users(),
                "redirect", safeRedirectTarget(redirect)
        ));
    }

    @GetMapping("/login/dev/select")
    String selectDevUser(
            @RequestParam String username,
            @RequestParam(required = false) String redirect,
            HttpServletRequest request
    ) {
        AuthenticatedUser user = devUserCatalog.findByUsername(username)
                .map(DevUserCatalog.DevUserEntry::toAuthenticatedUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown dev user"));

        OAuth2AuthenticatedUser principal = new OAuth2AuthenticatedUser(user);
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
        );

        return "redirect:" + safeRedirectTarget(redirect);
    }

    private String safeRedirectTarget(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return defaultRedirectUri;
        }
        if (redirect.startsWith("/") && !redirect.startsWith("//")) {
            return redirect;
        }
        if (redirect.startsWith("http://localhost:14200/") || redirect.equals("http://localhost:14200")) {
            return redirect;
        }
        if (redirect.startsWith("http://127.0.0.1:14200/") || redirect.equals("http://127.0.0.1:14200")) {
            return redirect;
        }
        return defaultRedirectUri;
    }
}
