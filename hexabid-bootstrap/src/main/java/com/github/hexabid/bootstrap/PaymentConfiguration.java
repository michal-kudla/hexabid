package com.github.hexabid.bootstrap;

import com.github.hexabid.payment.api.PaymentGateway;
import com.github.hexabid.payment.core.domain.CurrencyConverter;
import com.github.hexabid.payment.core.domain.SimpleCurrencyConverter;
import com.github.hexabid.payment.core.usecase.ProcessPaymentUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URI;
import java.util.Optional;

@Configuration
@Profile("!payment-local")
public class PaymentConfiguration {

    @Bean
    public CurrencyConverter currencyConverter() {
        return new SimpleCurrencyConverter();
    }

    @Bean
    public PaymentGateway paymentGateway() {
        return new PaymentGateway() {
            @Override
            public PaymentResponse initiatePayment(PaymentRequest request) {
                return new PaymentGateway.PaymentResponse(
                    PaymentGateway.PaymentStatus.PENDING,
                    Optional.of(URI.create("https://mock-payment-gateway.com/pay/" + request.transactionId())),
                    request.transactionId(),
                    null
                );
            }

            @Override
            public String gatewayId() {
                return "mock-gateway";
            }
        };
    }

    @Bean
    public ProcessPaymentUseCase processPaymentUseCase(PaymentGateway paymentGateway, CurrencyConverter currencyConverter) {
        return new ProcessPaymentUseCase(paymentGateway, currencyConverter);
    }
}