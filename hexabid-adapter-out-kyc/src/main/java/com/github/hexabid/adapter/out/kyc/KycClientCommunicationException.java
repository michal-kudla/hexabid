package com.github.hexabid.adapter.out.kyc;

public class KycClientCommunicationException extends RuntimeException {

    private final KycErrorClassification classification;

    public KycClientCommunicationException(KycErrorClassification classification, String message) {
        super(message);
        this.classification = classification;
    }

    public KycClientCommunicationException(KycErrorClassification classification, String message, Throwable cause) {
        super(message, cause);
        this.classification = classification;
    }

    public KycErrorClassification classification() {
        return classification;
    }
}
