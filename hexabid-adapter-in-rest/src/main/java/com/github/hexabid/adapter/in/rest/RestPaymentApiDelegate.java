package com.github.hexabid.adapter.in.rest;

import com.github.hexabid.contract.payment.api.PaymentApiDelegate;
import com.github.hexabid.contract.payment.model.PaymentGatewayResponse;
import com.github.hexabid.payment.api.PaymentGatewayDiscoverer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestPaymentApiDelegate implements PaymentApiDelegate {

    private final List<PaymentGatewayDiscoverer> discoverers;

    public RestPaymentApiDelegate(List<PaymentGatewayDiscoverer> discoverers) {
        this.discoverers = discoverers;
    }

    @Override
    public ResponseEntity<List<PaymentGatewayResponse>> getPaymentGateways(String xApiVersion) {
        List<PaymentGatewayResponse> responses = discoverers.stream()
                .flatMap(d -> d.getGateways().stream())
                .map(d -> new PaymentGatewayResponse()
                        .id(d.id())
                        .name(d.name())
                        .gatewayUrl("/api/payments/initiate?gatewayId=" + d.id()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
