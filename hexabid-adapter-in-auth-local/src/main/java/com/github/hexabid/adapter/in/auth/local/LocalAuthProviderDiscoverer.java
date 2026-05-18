package com.github.hexabid.adapter.in.auth.local;

import com.github.hexabid.auth.core.identityaccess.port.out.AuthProviderDescriptor;
import com.github.hexabid.auth.core.identityaccess.port.out.AuthProviderDiscoverer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalAuthProviderDiscoverer implements AuthProviderDiscoverer {

    @Override
    public List<AuthProviderDescriptor> getProviders() {
        return List.of();
    }
}
