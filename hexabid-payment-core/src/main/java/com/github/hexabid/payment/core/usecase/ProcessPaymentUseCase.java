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
import java.util.UUID;

public class ProcessPaymentUseCase {

    private final PaymentGateway paymentGateway;
    private final CurrencyConverter currencyConverter;

    public ProcessPaymentUseCase(PaymentGateway paymentGateway, CurrencyConverter currencyConverter) {
        this.paymentGateway = paymentGateway;
        this.currencyConverter = currencyConverter;
    }

    public PaymentResponse execute(AuctionId auctionId, Price amount, String targetCurrency, AccountId buyerAccountId, AccountId sellerAccountId) {
        Price finalAmount = currencyConverter.convert(amount, targetCurrency);

        String gatewayTxId = UUID.randomUUID().toString();
        PaymentRequest request = new PaymentRequest(
                gatewayTxId,
                finalAmount,
                "Payment for auction " + auctionId.value(),
                URI.create("https://hexabid.com/payment/callback")
        );

        PaymentResponse response = paymentGateway.initiatePayment(request);

        TransactionId txId = TransactionId.next();
        var now = Instant.now();
        AccountingEntry debit = new AccountingEntry(
                EntryId.next(),
                buyerAccountId,
                finalAmount,
                AccountingEntry.EntryType.DEBIT,
                now
        );
        AccountingEntry credit = new AccountingEntry(
                EntryId.next(),
                sellerAccountId,
                finalAmount,
                AccountingEntry.EntryType.CREDIT,
                now
        );
        AccountingTransaction tx = AccountingTransaction.createBalanced(txId, auctionId, debit, credit);

        return response;
    }
}
