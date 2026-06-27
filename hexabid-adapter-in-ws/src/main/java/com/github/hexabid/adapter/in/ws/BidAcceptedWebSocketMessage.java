package com.github.hexabid.adapter.in.ws;

public record BidAcceptedWebSocketMessage(String bidderId, String amount, String currency, String placedAt) {
}
