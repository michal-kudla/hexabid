package com.acme.auctions.payment.adapter.local;

import com.acme.auctions.payment.api.PaymentGateway.PaymentStatus;
import com.acme.auctions.payment.core.usecase.CompletePaymentUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dev-payment-mock")
public class DevLocalPaymentMockController {

    private final CompletePaymentUseCase completePaymentUseCase;

    public DevLocalPaymentMockController(CompletePaymentUseCase completePaymentUseCase) {
        this.completePaymentUseCase = completePaymentUseCase;
    }

    /**
     * Endpoint symulujący widok po przekierowaniu użytkownika na stronę operatora.
     * Ułatwia on deweloperom wywołanie statusu.
     */
    @GetMapping
    public String mockPaymentPage(@RequestParam("txId") String txId) {
        return "<html><body>" +
               "<h1>Lokalna Bramka Płatności (MOCK)</h1>" +
               "<p>Transakcja: " + txId + "</p>" +
               "<hr/>" +
               "<form method='POST' action='/dev-payment-mock/callback'>" +
               "<input type='hidden' name='txId' value='" + txId + "'/>" +
               "<button type='submit' name='status' value='COMPLETED' style='background:green;color:white;padding:10px;'>Płacę z Sukcesem (COMPLETED)</button> " +
               "<button type='submit' name='status' value='FAILED' style='background:red;color:white;padding:10px;'>Brak Środków (FAILED)</button>" +
               "</form>" +
               "</body></html>";
    }

    /**
     * Endpoint odbierający wynik symulacji i wywołujący przypadek użycia systemu płatności.
     */
    @PostMapping("/callback")
    public ResponseEntity<String> handleMockCallback(
            @RequestParam("txId") String txId,
            @RequestParam("status") String statusStr) {

        PaymentStatus status = PaymentStatus.valueOf(statusStr);
        completePaymentUseCase.execute(txId, status);

        return ResponseEntity.ok("Zasymulowano powrót z bramki do systemu Hexabid. Status: " + status);
    }
}
