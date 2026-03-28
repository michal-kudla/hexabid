package com.acme.auctions.payment.adapter.payu;

import com.acme.auctions.payment.api.PaymentGateway;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public class PayUAdapter implements PaymentGateway {

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {
        // Mocking PayU REST API call
        System.out.println("Initiating PayU payment for " + request.amount());
        
        return new PaymentResponse(
                PaymentStatus.PENDING,
                Optional.of(URI.create("https://merch-prod.snd.payu.com/pay/?orderId=" + UUID.randomUUID())),
                UUID.randomUUID().toString(),
                null
        );
    }

    @Override
    public String gatewayId() {
        return "PAYU";
    }
}
