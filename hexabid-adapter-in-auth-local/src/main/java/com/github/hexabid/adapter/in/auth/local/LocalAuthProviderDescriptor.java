package com.github.hexabid.adapter.in.auth.local;

import com.github.hexabid.auth.core.identityaccess.port.out.AuthProviderDescriptor;
import org.springframework.stereotype.Component;

@Component
public class LocalAuthProviderDescriptor implements AuthProviderDescriptor {
    @Override
    public String getRegistrationId() {
        return "local";
    }

    @Override
    public String getName() {
        return "Local Development Account";
    }

    @Override
    public String getLoginUrl() {
        return "/login"; // Ścieżka do lokalnego formularza, który zachowuje się jak provider
    }
}
