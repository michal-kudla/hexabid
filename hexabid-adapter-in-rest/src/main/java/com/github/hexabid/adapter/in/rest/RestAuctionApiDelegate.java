package com.github.hexabid.adapter.in.rest;

import com.github.hexabid.auth.core.identityaccess.port.in.FindCurrentUserProfileUseCase;
import com.github.hexabid.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.github.hexabid.contract.model.AuctionListResponse;
import com.github.hexabid.contract.api.AuctionsApiDelegate;
import com.github.hexabid.contract.model.AuctionResponse;
import com.github.hexabid.contract.model.AuctionSort;
import com.github.hexabid.contract.model.AuctionStatus;
import com.github.hexabid.contract.model.CreateAuctionRequest;
import com.github.hexabid.contract.model.CurrentUserProfileResponse;
import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.Price;
import com.github.hexabid.core.auctioning.port.in.AuctionDetailsResult;
import com.github.hexabid.core.auctioning.port.in.BrowseAuctionsQuery;
import com.github.hexabid.core.auctioning.port.in.BrowseAuctionsUseCase;
import com.github.hexabid.core.auctioning.port.in.CreateAuctionResult;
import com.github.hexabid.core.auctioning.port.in.CreateAuctionCommand;
import com.github.hexabid.core.auctioning.port.in.CreateAuctionUseCase;
import com.github.hexabid.core.auctioning.port.in.FindAuctionDetailsUseCase;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class RestAuctionApiDelegate implements AuctionsApiDelegate {

    private final CreateAuctionUseCase createAuctionUseCase;
    private final FindAuctionDetailsUseCase findAuctionDetailsUseCase;
    private final BrowseAuctionsUseCase browseAuctionsUseCase;
    private final FindCurrentUserProfileUseCase findCurrentUserProfileUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final RestAuctionContractMapper mapper;
    private final Counter createAuctionAcceptedCounter;
    private final Counter createAuctionRejectedCounter;
    private final Counter browseAuctionsCounter;
    private final Counter browseMyAuctionsCounter;
    private final Counter browseMyBidsCounter;
    private final Counter getAuctionByIdCounter;

    public RestAuctionApiDelegate(
            CreateAuctionUseCase createAuctionUseCase,
            FindAuctionDetailsUseCase findAuctionDetailsUseCase,
            BrowseAuctionsUseCase browseAuctionsUseCase,
            FindCurrentUserProfileUseCase findCurrentUserProfileUseCase,
            CurrentUserProvider currentUserProvider,
            RestAuctionContractMapper mapper,
            MeterRegistry meterRegistry
    ) {
        this.createAuctionUseCase = createAuctionUseCase;
        this.findAuctionDetailsUseCase = findAuctionDetailsUseCase;
        this.browseAuctionsUseCase = browseAuctionsUseCase;
        this.findCurrentUserProfileUseCase = findCurrentUserProfileUseCase;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
        this.createAuctionAcceptedCounter = meterRegistry.counter("auctions.create.accepted");
        this.createAuctionRejectedCounter = meterRegistry.counter("auctions.create.rejected");
        this.browseAuctionsCounter = meterRegistry.counter("auctions.browse.requests", "scope", "market");
        this.browseMyAuctionsCounter = meterRegistry.counter("auctions.browse.requests", "scope", "seller");
        this.browseMyBidsCounter = meterRegistry.counter("auctions.browse.requests", "scope", "bidder");
        this.getAuctionByIdCounter = meterRegistry.counter("auctions.details.requests");
    }

    @Override
    public ResponseEntity<AuctionListResponse> browseAuctions(
            String xApiVersion,
            String query,
            AuctionStatus status,
            AuctionSort sort,
            Integer limit,
            String after
    ) {
        browseAuctionsCounter.increment();
        return ResponseEntity.ok(mapper.toResponse(browseAuctionsUseCase.browseAuctions(toQuery(query, status, sort, limit, after))));
    }

    @Override
    public ResponseEntity<AuctionResponse> createAuction(CreateAuctionRequest request, String xApiVersion) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            createAuctionRejectedCounter.increment();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CreateAuctionResult result = createAuctionUseCase.createAuction(new CreateAuctionCommand(
                authenticatedUser.partyId(),
                request.getTitle(),
                toPrice(request.getStartingPrice().getAmount(), request.getStartingPrice().getCurrency()),
                request.getEndsAt().toInstant()
        ));
        if (result instanceof CreateAuctionResult.AuctionCreated created) {
            createAuctionAcceptedCounter.increment();
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created.auction()));
        }
        CreateAuctionResult.AuctionCreationRejected rejected = (CreateAuctionResult.AuctionCreationRejected) result;
        createAuctionRejectedCounter.increment();
        throw new RestRequestRejectedException(HttpStatus.BAD_REQUEST, rejected.message());
    }

    @Override
    public ResponseEntity<AuctionResponse> getAuctionById(UUID auctionId, String xApiVersion) {
        getAuctionByIdCounter.increment();
        AuctionDetailsResult result = findAuctionDetailsUseCase.findAuctionDetails(new AuctionId(auctionId));
        if (result instanceof AuctionDetailsResult.AuctionFound found) {
            return ResponseEntity.ok(mapper.toResponse(found.auction()));
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<CurrentUserProfileResponse> getCurrentUserProfile(String xApiVersion) {
        return findCurrentUserProfileUseCase.findCurrentUserProfile()
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Override
    public ResponseEntity<AuctionListResponse> browseMyAuctions(
            String xApiVersion,
            AuctionStatus status,
            AuctionSort sort,
            Integer limit,
            String after
    ) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        browseMyAuctionsCounter.increment();
        return ResponseEntity.ok(
                mapper.toResponse(
                        browseAuctionsUseCase.browseSellerAuctions(
                                authenticatedUser.partyId(),
                                toQuery(null, status, sort, limit, after)
                        )
                )
        );
    }

    @Override
    public ResponseEntity<AuctionListResponse> browseMyBids(
            String xApiVersion,
            AuctionStatus status,
            AuctionSort sort,
            Integer limit,
            String after
    ) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        browseMyBidsCounter.increment();
        return ResponseEntity.ok(
                mapper.toResponse(
                        browseAuctionsUseCase.browseBidderAuctions(
                                authenticatedUser.partyId(),
                                toQuery(null, status, sort, limit, after)
                        )
                )
        );
    }

    private Price toPrice(String amount, String currency) {
        return new Price(new BigDecimal(amount), currency);
    }

    private BrowseAuctionsQuery toQuery(String query, AuctionStatus status, AuctionSort sort, Integer limit, String after) {
        com.github.hexabid.core.auctioning.model.AuctionStatus parsedStatus =
                status == null ? null : com.github.hexabid.core.auctioning.model.AuctionStatus.valueOf(status.getValue());
        com.github.hexabid.core.auctioning.port.in.AuctionSort parsedSort =
                sort == null
                        ? com.github.hexabid.core.auctioning.port.in.AuctionSort.ENDING_SOON
                        : com.github.hexabid.core.auctioning.port.in.AuctionSort.valueOf(sort.getValue());
        int parsedLimit = limit == null ? 20 : limit;
        return new BrowseAuctionsQuery(query, parsedStatus, parsedSort, parsedLimit, after);
    }
}
