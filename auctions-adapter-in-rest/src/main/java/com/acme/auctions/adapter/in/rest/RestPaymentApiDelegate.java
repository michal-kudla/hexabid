package com.acme.auctions.adapter.in.rest;

import com.acme.auctions.contract.payment.api.PaymentApiDelegate;
import com.acme.auctions.contract.payment.model.PaymentGatewayResponse;
import com.acme.auctions.payment.core.infrastructure.PaymentGatewayRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestPaymentApiDelegate implements PaymentApiDelegate {

    private final PaymentGatewayRegistry registry;

    public RestPaymentApiDelegate(PaymentGatewayRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ResponseEntity<List<PaymentGatewayResponse>> getPaymentGateways(String xAPIVersion) {
        List<PaymentGatewayResponse> responses = registry.getAllDescriptors().stream()
                .map(d -> new PaymentGatewayResponse()
                        .id(d.id())
                        .name(d.name())
                        .gatewayUrl("/api/payments/initiate?gatewayId=" + d.id()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
