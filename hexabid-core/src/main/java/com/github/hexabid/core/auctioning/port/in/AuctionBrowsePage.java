package com.github.hexabid.core.auctioning.port.in;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record AuctionBrowsePage(List<AuctionSummaryView> items, @Nullable String nextCursor) {
}
