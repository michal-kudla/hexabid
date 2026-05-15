package com.github.hexabid.bootstrap;

import com.github.hexabid.core.auctioning.model.Auction;
import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.Price;
import com.github.hexabid.core.auctioning.port.in.AuctionSort;
import com.github.hexabid.core.auctioning.port.in.BrowseAuctionsQuery;
import com.github.hexabid.core.auctioning.port.out.AuctionReadModel;
import com.github.hexabid.core.auctioning.port.out.AuctionRepository;
import com.github.hexabid.core.lot.model.Lot;
import com.github.hexabid.core.party.model.PartyId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Configuration
@Profile("local")
class LocalDevelopmentDataConfiguration {

    @Bean
    ApplicationRunner developmentDataInitializer(
            AuctionReadModel auctionReadModel,
            AuctionRepository auctionRepository,
            Clock clock,
            @Value("${auctions.seed.enabled:true}") boolean seedEnabled
    ) {
        return args -> {
            if (!seedEnabled || hasAnyAuction(auctionReadModel)) {
                return;
            }

            Instant now = clock.instant();
            Instant startedAt = now.minus(Duration.ofDays(10));
            List<Auction> sampleAuctions = List.of(
                    activeAuction(
                            "Apartament z tarasem w Gdyni",
                            "dev:seller-anna",
                            money("485000.00", "PLN"),
                            now.plus(Duration.ofDays(6)),
                            List.of(new BidSeed("dev:bidder-ola", "501000.00", now.minus(Duration.ofHours(6)))),
                            startedAt
                    ),
                    activeAuction(
                            "Ford Mustang Fastback 1968",
                            "dev:seller-marek",
                            money("185000.00", "PLN"),
                            now.plus(Duration.ofDays(4)),
                            List.of(
                                    new BidSeed("dev:bidder-jan", "192500.00", now.minus(Duration.ofHours(8))),
                                    new BidSeed("dev:bidder-ola", "198000.00", now.minus(Duration.ofHours(2)))
                            ),
                            startedAt
                    ),
                    activeAuction(
                            "Kolekcja komiksow z lat 90.",
                            "dev:seller-anna",
                            money("1200.00", "PLN"),
                            now.plus(Duration.ofDays(2)),
                            List.of(new BidSeed("dev:bidder-jan", "1480.00", now.minus(Duration.ofMinutes(40)))),
                            startedAt
                    ),
                    activeAuction(
                            "Maszyna CNC po przegladzie serwisowym",
                            "dev:seller-marek",
                            money("27500.00", "PLN"),
                            now.plus(Duration.ofDays(9)),
                            List.of(),
                            startedAt
                    ),
                    activeAuction(
                            "Fotografia kolekcjonerska - edycja limitowana",
                            "dev:seller-anna",
                            money("950.00", "PLN"),
                            now.plus(Duration.ofHours(19)),
                            List.of(
                                    new BidSeed("dev:bidder-lena", "1100.00", now.minus(Duration.ofHours(5))),
                                    new BidSeed("dev:bidder-jan", "1250.00", now.minus(Duration.ofMinutes(55)))
                            ),
                            startedAt
                    ),
                    activeAuction(
                            "Zestaw mebli loftowych do biura",
                            "dev:seller-marek",
                            money("8900.00", "PLN"),
                            now.plus(Duration.ofDays(3)),
                            List.of(new BidSeed("dev:bidder-ola", "9300.00", now.minus(Duration.ofHours(4)))),
                            startedAt
                    ),
                    activeAuction(
                            "Skuter Vespa Primavera",
                            "dev:seller-anna",
                            money("3200.00", "EUR"),
                            now.plus(Duration.ofDays(5)),
                            List.of(
                                    new BidSeed("dev:bidder-lena", "3400.00", now.minus(Duration.ofHours(10))),
                                    new BidSeed("dev:bidder-jan", "3600.00", now.minus(Duration.ofHours(3)))
                            ),
                            startedAt
                    ),
                    activeAuction(
                            "Gitara Fender Stratocaster USA",
                            "dev:seller-marek",
                            money("7800.00", "PLN"),
                            now.plus(Duration.ofDays(1)),
                            List.of(new BidSeed("dev:bidder-ola", "8200.00", now.minus(Duration.ofMinutes(25)))),
                            startedAt
                    ),
                    closedAuction(
                            "Dzialka rekreacyjna nad jeziorem",
                            "dev:seller-anna",
                            money("99000.00", "PLN"),
                            now.minus(Duration.ofDays(2)),
                            List.of(
                                    new BidSeed("dev:bidder-ola", "103000.00", now.minus(Duration.ofDays(3))),
                                    new BidSeed("dev:bidder-jan", "108500.00", now.minus(Duration.ofDays(2)).minus(Duration.ofHours(2)))
                            ),
                            now,
                            startedAt
                    ),
                    closedAuction(
                            "Konsola retro z kolekcja gier",
                            "dev:seller-marek",
                            money("650.00", "PLN"),
                            now.minus(Duration.ofDays(1)),
                            List.of(),
                            now,
                            startedAt
                    )
            );

            sampleAuctions.forEach(auctionRepository::save);
        };
    }

    private static boolean hasAnyAuction(AuctionReadModel auctionReadModel) {
        return !auctionReadModel
                .browseAuctions(new BrowseAuctionsQuery(null, null, AuctionSort.ENDING_SOON, 1, null))
                .items()
                .isEmpty();
    }

    private static Auction activeAuction(
            String title,
            String sellerId,
            Price startingPrice,
            Instant endsAt,
            List<BidSeed> bids,
            Instant startedAt
    ) {
        Auction auction = Auction.create(
                AuctionId.newId(),
                new PartyId(sellerId),
                "dev:" + sellerId,
                "A12/B04/C77",
                Lot.singleProductDraft(title),
                startingPrice,
                endsAt
        );
        auction.publish(startedAt);
        auction.start(startedAt);
        bids.forEach(bid -> auction.placeBid(new PartyId(bid.bidderId()), new Price(new BigDecimal(bid.amount()), startingPrice.currency()), bid.placedAt()));
        return auction;
    }

    private static Auction closedAuction(
            String title,
            String sellerId,
            Price startingPrice,
            Instant endsAt,
            List<BidSeed> bids,
            Instant now,
            Instant startedAt
    ) {
        Auction auction = activeAuction(title, sellerId, startingPrice, endsAt, bids, startedAt);
        auction.maybeCloseIfExpired(now);
        return auction;
    }

    private static Price money(String amount, String currency) {
        return new Price(new BigDecimal(amount), currency);
    }

    private record BidSeed(String bidderId, String amount, Instant placedAt) {
    }
}
