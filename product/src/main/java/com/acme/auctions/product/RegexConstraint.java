package com.acme.auctions.product;

import java.util.regex.Pattern;

public record RegexConstraint(Pattern pattern) implements FeatureValueConstraint {

    public RegexConstraint {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null");
        }
    }

    public static RegexConstraint of(String regex) {
        return new RegexConstraint(Pattern.compile(regex));
    }

    @Override
    public void validate(Object value) {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Value must be a String");
        }
        if (!pattern.matcher((String) value).matches()) {
            throw new IllegalArgumentException(
                "Value '%s' does not match pattern '%s'".formatted(value, pattern.pattern()));
        }
    }
}
