package com.github.hexabid.statement.model;

import java.util.Objects;

/**
 * Type-safe identifier for a {@link StatementDefinition}.
 *
 * <p>Each statement code uniquely identifies a declaration that a candidate
 * must answer during participation qualification. Common codes are defined
 * as static constants (e.g. {@link #LEGAL_CAPACITY}, {@link #SANCTIONS_CLEARANCE}).
 *
 * <p>Codes are compared by value; two instances with the same value are equal
 * regardless of whether one is a static constant and the other is created via
 * the constructor.
 */
public record StatementCode(String value) {

    public StatementCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    public static final StatementCode LEGAL_CAPACITY = new StatementCode("LEGAL_CAPACITY");
    public static final StatementCode BENEFICIAL_OWNER_DISCLOSURE = new StatementCode("BENEFICIAL_OWNER_DISCLOSURE");
    public static final StatementCode SANCTIONS_CLEARANCE = new StatementCode("SANCTIONS_CLEARANCE");
    public static final StatementCode PEP_DISCLOSURE = new StatementCode("PEP_DISCLOSURE");
    public static final StatementCode NO_CONFLICT_OF_INTEREST = new StatementCode("NO_CONFLICT_OF_INTEREST");
    public static final StatementCode NO_COLLUSION = new StatementCode("NO_COLLUSION");
    public static final StatementCode SOURCE_OF_FUNDS = new StatementCode("SOURCE_OF_FUNDS");
    public static final StatementCode PAYMENT_READINESS = new StatementCode("PAYMENT_READINESS");
    public static final StatementCode BID_BOND_ACCEPTANCE = new StatementCode("BID_BOND_ACCEPTANCE");
    public static final StatementCode TAX_RESIDENCY = new StatementCode("TAX_RESIDENCY");
    public static final StatementCode EXPORT_CONTROL_ELIGIBILITY = new StatementCode("EXPORT_CONTROL_ELIGIBILITY");
    public static final StatementCode SECTOR_LICENSE = new StatementCode("SECTOR_LICENSE");
    public static final StatementCode SITE_VISIT_ACKNOWLEDGEMENT = new StatementCode("SITE_VISIT_ACKNOWLEDGEMENT");
    public static final StatementCode DATA_ROOM_CONFIDENTIALITY = new StatementCode("DATA_ROOM_CONFIDENTIALITY");
    public static final StatementCode INSIDER_INFORMATION_ABSENCE = new StatementCode("INSIDER_INFORMATION_ABSENCE");
    public static final StatementCode SUBCONTRACTOR_DISCLOSURE = new StatementCode("SUBCONTRACTOR_DISCLOSURE");
    public static final StatementCode ENVIRONMENTAL_HANDLING_CAPACITY = new StatementCode("ENVIRONMENTAL_HANDLING_CAPACITY");
    public static final StatementCode TERMS_ACCEPTANCE = new StatementCode("TERMS_ACCEPTANCE");

    @Override
    public String toString() {
        return value;
    }
}
