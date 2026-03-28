package com.acme.auctions.payment.adapter.local;

import com.acme.auctions.payment.api.PaymentGatewayDiscoverer;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LocalPaymentGatewayDiscoverer implements PaymentGatewayDiscoverer {

    @Override
    public List<PaymentGatewayDescriptor> getGateways() {
        return List.of(new PaymentGatewayDescriptor("LOCAL", "Lokalna Bramka (Mock)", true));
    }
}
