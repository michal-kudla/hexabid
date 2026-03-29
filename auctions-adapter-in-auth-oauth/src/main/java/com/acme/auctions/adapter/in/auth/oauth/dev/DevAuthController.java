package com.acme.auctions.adapter.in.auth.oauth.dev;

import com.acme.auctions.auth.core.identityaccess.model.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Profile("dev")
public class DevAuthController {

    private final DevUserCatalog devUserCatalog;

    public DevAuthController(DevUserCatalog devUserCatalog) {
        this.devUserCatalog = devUserCatalog;
    }

    /**
     * Główny punkt wejścia dla logowania deweloperskiego.
     */
    @GetMapping({"/dev-auth", "/login/dev"})
    public String devLoginPage(@RequestParam(value = "redirect", defaultValue = "http://localhost:14002/") String redirect) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: sans-serif; padding: 20px;'>");
        html.append("<h1>Hexabid Dev Login</h1>");
        html.append("<p>Wybierz użytkownika do zalogowania (MOCK):</p>");
        html.append("<ul style='list-style: none; padding: 0;'>");

        for (DevUserCatalog.DevUserEntry user : devUserCatalog.users()) {
            html.append("<li style='margin-bottom: 10px; border: 1px solid #ccc; padding: 10px; border-radius: 4px;'>");
            html.append("<strong>").append(user.displayName()).append("</strong> (").append(user.username()).append(")<br/>");
            html.append("<small>").append(user.description()).append("</small><br/>");
            html.append("<form method='POST' action='/dev-auth/login' style='margin-top: 5px;'>");
            html.append("<input type='hidden' name='username' value='").append(user.username()).append("'/>");
            html.append("<input type='hidden' name='redirect' value='").append(redirect).append("'/>");
            html.append("<button type='submit' style='background: #007bff; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer;'>Login jako ").append(user.username()).append("</button>");
            html.append("</form>");
            html.append("</li>");
        }

        html.append("</ul>");
        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Akcja logowania - ręcznie ustawia kontekst bezpieczeństwa i przekierowuje z powrotem.
     */
    @PostMapping("/dev-auth/login")
    public String handleLogin(
            @RequestParam("username") String username,
            @RequestParam("redirect") String redirect,
            HttpServletRequest request) {

        Optional<DevUserCatalog.DevUserEntry> userEntry = devUserCatalog.findByUsername(username);
        if (userEntry.isPresent()) {
            AuthenticatedUser authenticatedUser = userEntry.get().toAuthenticatedUser();
            Authentication auth = new UsernamePasswordAuthenticationToken(authenticatedUser, null, List.of());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            return "<html><body><script>window.location.href='" + redirect + "';</script></body></html>";
        }

        return "User not found";
    }
}
