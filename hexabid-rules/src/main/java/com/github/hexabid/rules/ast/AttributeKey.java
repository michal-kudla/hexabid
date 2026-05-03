package com.github.hexabid.rules.ast;

public record AttributeKey(String name) {

    public static final AttributeKey PRODUCT_TYPE = new AttributeKey("productType");
    public static final AttributeKey ESTIMATED_VALUE = new AttributeKey("estimatedValue");
    public static final AttributeKey BIDDER_AGE = new AttributeKey("bidderAge");
    public static final AttributeKey WADIUM_PAID = new AttributeKey("wadiumPaid");
    public static final AttributeKey EXCISABLE = new AttributeKey("excisable");
    public static final AttributeKey IMPORTED = new AttributeKey("imported");
    public static final AttributeKey CUSTOMS_EXEMPT = new AttributeKey("customsExempt");
    public static final AttributeKey DOCUMENT_STATUS = new AttributeKey("documentStatus");
    public static final AttributeKey KYC_VERIFIED = new AttributeKey("kycVerified");
    public static final AttributeKey VIEWING_DATE_SCHEDULED = new AttributeKey("viewingDateScheduled");
    public static final AttributeKey EXCISE_DOCUMENT_STATUS = new AttributeKey("exciseDocumentStatus");
    public static final AttributeKey CUSTOMS_EXEMPTION_DOC_STATUS = new AttributeKey("customsExemptionDocStatus");
    public static final AttributeKey SETTLEMENT_DOCUMENT_PROVIDED = new AttributeKey("settlementDocumentProvided");
    public static final AttributeKey DAYS_SINCE_CLOSE = new AttributeKey("daysSinceClose");
    public static final AttributeKey FULL_PAYMENT_RECEIVED = new AttributeKey("fullPaymentReceived");
    public static final AttributeKey RECEIPT_CONFIRMED = new AttributeKey("receiptConfirmed");
}
