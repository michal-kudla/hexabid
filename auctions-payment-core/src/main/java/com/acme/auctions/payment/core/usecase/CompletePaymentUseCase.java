package com.acme.auctions.payment.core.usecase;

import com.acme.auctions.payment.api.PaymentGateway.PaymentStatus;
/**
 * Use case to complete or fail a payment based on the callback from
 * the payment gateway.
 */
public class CompletePaymentUseCase {

    public void execute(String gatewayTransactionId, PaymentStatus finalStatus) {
        System.out.println("Payment Callback Received! TxID: " + gatewayTransactionId + ", Status: " + finalStatus);
        
        // W prawdziwym środowisku aktualizujemy tu AccountingTransaction 
        // lub zamykamy odpowiedni Entry.
        
        if (finalStatus == PaymentStatus.COMPLETED) {
            System.out.println("Payment COMPLETED. The auction is now officially finalized.");
        } else {
            System.out.println("Payment FAILED (or Insufficient Funds).");
        }
    }
}
