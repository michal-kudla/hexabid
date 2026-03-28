package com.acme.auctions.bootstrap;

import com.acme.auctions.core.auctioning.event.AuctionWonEvent;
import com.acme.auctions.payment.api.PaymentGateway;
import com.acme.auctions.payment.api.PaymentGatewayDiscoverer;
import com.acme.auctions.payment.core.domain.CurrencyConverter;
import com.acme.auctions.payment.core.domain.SimpleCurrencyConverter;
import com.acme.auctions.payment.core.infrastructure.PaymentGatewayRegistry;
import com.acme.auctions.payment.core.usecase.CompletePaymentUseCase;
import com.acme.auctions.payment.core.usecase.ProcessPaymentUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

@Configuration
public class PaymentConfiguration {

    @Bean
    public CurrencyConverter currencyConverter() {
        return new SimpleCurrencyConverter();
    }

    @Bean
    public CompletePaymentUseCase completePaymentUseCase() {
        return new CompletePaymentUseCase();
    }

    @Bean
    public PaymentGatewayRegistry paymentGatewayRegistry(List<PaymentGateway> gateways, List<PaymentGatewayDiscoverer> discoverers) {
        return new PaymentGatewayRegistry(gateways, discoverers);
    }

    @Bean
    public ProcessPaymentUseCase processPaymentUseCase(List<PaymentGateway> gateways, CurrencyConverter currencyConverter) {
        PaymentGateway defaultGateway = gateways.isEmpty() ? null : gateways.get(0);
        return new ProcessPaymentUseCase(defaultGateway, currencyConverter);
    }

    @Bean
    public AuctionWonPaymentCoordinator auctionWonPaymentCoordinator(ProcessPaymentUseCase processPaymentUseCase) {
        return new AuctionWonPaymentCoordinator(processPaymentUseCase);
    }

    public static class AuctionWonPaymentCoordinator {
        private final ProcessPaymentUseCase processPaymentUseCase;

        public AuctionWonPaymentCoordinator(ProcessPaymentUseCase processPaymentUseCase) {
            this.processPaymentUseCase = processPaymentUseCase;
        }

        @EventListener
        public void onAuctionWon(AuctionWonEvent event) {
            System.out.println("Auction won: " + event.auctionId() + " by " + event.winnerId());
            String targetCurrency = "PLN";
            processPaymentUseCase.execute(event.auctionId(), event.winningPrice(), targetCurrency);
        }
    }
}
