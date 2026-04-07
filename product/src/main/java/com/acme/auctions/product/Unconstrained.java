package com.acme.auctions.product;

public record Unconstrained() implements FeatureValueConstraint {

    public static final Unconstrained INSTANCE = new Unconstrained();

    @Override
    public void validate(Object value) {
        // no validation
    }
}
