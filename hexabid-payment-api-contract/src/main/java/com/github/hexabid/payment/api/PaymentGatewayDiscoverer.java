package com.github.hexabid.payment.api;

import java.util.List;

/**
 * Interface for discovering available payment gateways.
 */
public interface PaymentGatewayDiscoverer {
    List<PaymentGatewayDescriptor> getGateways();

    record PaymentGatewayDescriptor(
            String id,
            String name,
            boolean localOnly
    ) {}
}
