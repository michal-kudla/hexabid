package com.github.hexabid.inventory;

import com.github.hexabid.quantity.Quantity;
import java.util.Objects;

public record Availability(
        Quantity total,
        Quantity reserved,
        Quantity available
) {
    public Availability {
        Objects.requireNonNull(total, "total must not be null");
        Objects.requireNonNull(reserved, "reserved must not be null");
        Objects.requireNonNull(available, "available must not be null");
    }

    public boolean isAvailable() {
        return available.isGreaterThan(Quantity.of(0, available.unit()));
    }

    public static Availability of(Quantity total, Quantity reserved) {
        var unit = total.unit();
        var available = total.subtract(reserved);
        return new Availability(total, reserved, available);
    }
}
