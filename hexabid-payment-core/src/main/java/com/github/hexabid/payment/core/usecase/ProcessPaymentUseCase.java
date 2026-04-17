package com.github.hexabid.payment.core.usecase;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.Price;
import com.github.hexabid.payment.api.PaymentGateway;
import com.github.hexabid.payment.api.PaymentGateway.PaymentRequest;
import com.github.hexabid.payment.api.PaymentGateway.PaymentResponse;
import com.github.hexabid.payment.core.domain.CurrencyConverter;
import com.github.hexabid.payment.core.model.*;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Use case for processing a payment for a won auction.
 */
public class ProcessPaymentUseCase {

    private final PaymentGateway paymentGateway;
    private final CurrencyConverter currencyConverter;

    public ProcessPaymentUseCase(PaymentGateway paymentGateway, CurrencyConverter currencyConverter) {
        this.paymentGateway = paymentGateway;
        this.currencyConverter = currencyConverter;
    }

    public PaymentResponse execute(AuctionId auctionId, Price amount, String targetCurrency) {
        // 1. Convert currency if needed
        Price finalAmount = currencyConverter.convert(amount, targetCurrency);

        // 2. Initiate payment via gateway
        String transactionId = UUID.randomUUID().toString();
        PaymentRequest request = new PaymentRequest(
                transactionId,
                finalAmount,
                "Payment for auction " + auctionId.value(),
                URI.create("https://hexabid.com/payment/callback")
        );

        PaymentResponse response = paymentGateway.initiatePayment(request);

        // 3. Create accounting entries (Accounting Archetype)
        // In a real system, we would persist these to a database
        TransactionId txId = TransactionId.next();
        AccountingEntry entry = new AccountingEntry(
                EntryId.next(),
                AccountId.next(), // Should be fetched from registry
                finalAmount,
                AccountingEntry.EntryType.DEBIT,
                Instant.now()
        );
        AccountingTransaction tx = new AccountingTransaction(txId, auctionId, List.of(entry));

        return response;
    }
}
