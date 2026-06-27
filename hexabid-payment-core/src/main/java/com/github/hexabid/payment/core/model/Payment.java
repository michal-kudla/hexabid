package com.github.hexabid.payment.core.model;

import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.Price;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Payment {

    private final PaymentId id;
    private final AuctionId auctionId;
    private final Price amount;
    private final String targetCurrency;
    private final AccountId buyerAccountId;
    private final AccountId sellerAccountId;
    private final String gatewayTransactionId;
    private Status status;
    private String failureReason;
    private final Instant createdAt;
    private Instant completedAt;

    public Payment(AuctionId auctionId, Price amount, String targetCurrency,
                   AccountId buyerAccountId, AccountId sellerAccountId, String gatewayTransactionId) {
        this.id = PaymentId.next();
        this.auctionId = Objects.requireNonNull(auctionId, "auctionId must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.targetCurrency = Objects.requireNonNull(targetCurrency, "targetCurrency must not be null");
        this.buyerAccountId = Objects.requireNonNull(buyerAccountId, "buyerAccountId must not be null");
        this.sellerAccountId = Objects.requireNonNull(sellerAccountId, "sellerAccountId must not be null");
        this.gatewayTransactionId = Objects.requireNonNull(gatewayTransactionId, "gatewayTransactionId must not be null");
        this.status = Status.PENDING;
        this.createdAt = Instant.now();
    }

    public PaymentId id() { return id; }
    public AuctionId auctionId() { return auctionId; }
    public Price amount() { return amount; }
    public String targetCurrency() { return targetCurrency; }
    public AccountId buyerAccountId() { return buyerAccountId; }
    public AccountId sellerAccountId() { return sellerAccountId; }
    public String gatewayTransactionId() { return gatewayTransactionId; }
    public Status status() { return status; }
    public String failureReason() { return failureReason; }
    public Instant createdAt() { return createdAt; }
    public Instant completedAt() { return completedAt; }

    public void complete() {
        if (status != Status.PENDING) {
            throw new IllegalStateException("Cannot complete payment in status: " + status);
        }
        this.status = Status.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void fail(String reason) {
        if (status != Status.PENDING) {
            throw new IllegalStateException("Cannot fail payment in status: " + status);
        }
        this.status = Status.FAILED;
        this.failureReason = Objects.requireNonNull(reason, "reason must not be null");
        this.completedAt = Instant.now();
    }

    public void refund() {
        if (status != Status.COMPLETED) {
            throw new IllegalStateException("Cannot refund payment in status: " + status);
        }
        this.status = Status.REFUNDED;
    }

    public enum Status {
        PENDING, COMPLETED, FAILED, REFUNDED
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Payment that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Payment{id=" + id + ", auction=" + auctionId + ", status=" + status + "}";
    }
}
