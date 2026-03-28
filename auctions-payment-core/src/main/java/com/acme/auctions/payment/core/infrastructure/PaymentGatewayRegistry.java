package com.acme.auctions.payment.core.infrastructure;

import com.acme.auctions.payment.api.PaymentGateway;
import com.acme.auctions.payment.api.PaymentGatewayDiscoverer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Registry that manages all available payment gateways discovered by plugins.
 */
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
