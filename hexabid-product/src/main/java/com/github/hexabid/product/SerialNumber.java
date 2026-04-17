package com.github.hexabid.product;

/**
 * Serial number for individually tracked products.
 * Can be VIN, IMEI, or generic text.
 */
public sealed interface SerialNumber permits VinSerialNumber, ImeiSerialNumber, TextualSerialNumber {
    String value();
}
