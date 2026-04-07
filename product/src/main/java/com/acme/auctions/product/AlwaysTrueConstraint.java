package com.acme.auctions.product;

public record AlwaysTrueConstraint() implements ApplicabilityConstraint {

    public static final AlwaysTrueConstraint INSTANCE = new AlwaysTrueConstraint();

    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return true;
    }
}
