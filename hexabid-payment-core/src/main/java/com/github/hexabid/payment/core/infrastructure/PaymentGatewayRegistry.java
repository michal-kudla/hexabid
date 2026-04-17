package com.github.hexabid.payment.core.infrastructure;

import com.github.hexabid.payment.api.PaymentGateway;
import com.github.hexabid.payment.api.PaymentGatewayDiscoverer;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Registry that manages all available payment gateways discovered by plugins.
 */
@Component
public class PaymentGatewayRegistry {

    private final List<PaymentGateway> gateways;
    private final List<PaymentGatewayDiscoverer> discoverers;

    public PaymentGatewayRegistry(List<PaymentGateway> gateways, List<PaymentGatewayDiscoverer> discoverers) {
        this.gateways = gateways;
        this.discoverers = discoverers;
    }

    public List<PaymentGatewayDiscoverer.PaymentGatewayDescriptor> getAllDescriptors() {
        List<PaymentGatewayDiscoverer.PaymentGatewayDescriptor> descriptors = new ArrayList<>();
        for (PaymentGatewayDiscoverer discoverer : discoverers) {
            descriptors.addAll(discoverer.getGateways());
        }
        return descriptors;
    }

    public Optional<PaymentGateway> getGateway(String id) {
        return gateways.stream()
                .filter(g -> g.gatewayId().equals(id))
                .findFirst();
    }
}
