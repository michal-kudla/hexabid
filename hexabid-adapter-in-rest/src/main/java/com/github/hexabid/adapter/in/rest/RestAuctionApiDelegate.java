package com.github.hexabid.adapter.in.rest;

import com.github.hexabid.auth.core.identityaccess.port.in.FindCurrentUserProfileUseCase;
import com.github.hexabid.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.github.hexabid.contract.model.*;
import com.github.hexabid.contract.api.AuctionsApiDelegate;
import com.github.hexabid.core.auctioning.model.AuctionId;
import com.github.hexabid.core.auctioning.model.Price;
import com.github.hexabid.core.auctioning.port.in.AuctionDetailsResult;
import com.github.hexabid.core.auctioning.port.in.BrowseAuctionsQuery;
import com.github.hexabid.core.auctioning.port.in.BrowseAuctionsUseCase;
import com.github.hexabid.core.auctioning.port.in.CreateAuctionResult;
import com.github.hexabid.core.auctioning.port.in.CreateAuctionCommand;
import com.github.hexabid.core.auctioning.port.in.CreateAuctionUseCase;
import com.github.hexabid.core.auctioning.port.in.FindAuctionDetailsUseCase;
import com.github.hexabid.pricing.auction.AuctionPriceBreakdown;
import com.github.hexabid.pricing.auction.AuctionPricingFacade;
import com.github.hexabid.pricing.model.CustomsDutyRate;
import com.github.hexabid.pricing.model.ExciseRate;
import com.github.hexabid.pricing.model.Money;
import com.github.hexabid.pricing.model.PricingContext;
import com.github.hexabid.pricing.model.VatRate;
import com.github.hexabid.pricing.model.Wadium;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RestAuctionApiDelegate implements AuctionsApiDelegate {

    private final CreateAuctionUseCase createAuctionUseCase;
    private final FindAuctionDetailsUseCase findAuctionDetailsUseCase;
    private final BrowseAuctionsUseCase browseAuctionsUseCase;
    private final FindCurrentUserProfileUseCase findCurrentUserProfileUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final RestAuctionContractMapper mapper;
    private final AuctionPricingFacade auctionPricingFacade;
    private final Counter createAuctionAcceptedCounter;
    private final Counter createAuctionRejectedCounter;
    private final Counter browseAuctionsCounter;
    private final Counter browseMyAuctionsCounter;
    private final Counter browseMyBidsCounter;
    private final Counter getAuctionByIdCounter;

    private final Map<UUID, PricingConfig> auctionPricingConfigs = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, WadiumDeposit>> auctionWadiumDeposits = new ConcurrentHashMap<>();

    public RestAuctionApiDelegate(
            CreateAuctionUseCase createAuctionUseCase,
            FindAuctionDetailsUseCase findAuctionDetailsUseCase,
            BrowseAuctionsUseCase browseAuctionsUseCase,
            FindCurrentUserProfileUseCase findCurrentUserProfileUseCase,
            CurrentUserProvider currentUserProvider,
            RestAuctionContractMapper mapper,
            AuctionPricingFacade auctionPricingFacade,
            MeterRegistry meterRegistry
    ) {
        this.createAuctionUseCase = createAuctionUseCase;
        this.findAuctionDetailsUseCase = findAuctionDetailsUseCase;
        this.browseAuctionsUseCase = browseAuctionsUseCase;
        this.findCurrentUserProfileUseCase = findCurrentUserProfileUseCase;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
        this.auctionPricingFacade = auctionPricingFacade;
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
            if (request.getPricingConfig() != null) {
                auctionPricingConfigs.put(created.auction().auctionId(), request.getPricingConfig());
            }
            createAuctionAcceptedCounter.increment();
            AuctionResponse auctionResponse = mapper.toResponse(created.auction());
            auctionResponse.pricingConfig(request.getPricingConfig());
            return ResponseEntity.status(HttpStatus.CREATED).body(auctionResponse);
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

    @Override
    public ResponseEntity<AuctionPriceBreakdownResponse> getAuctionPrice(UUID auctionId, String xApiVersion) {
        PricingConfig config = auctionPricingConfigs.getOrDefault(auctionId, new PricingConfig());
        AuctionDetailsResult result = findAuctionDetailsUseCase.findAuctionDetails(new AuctionId(auctionId));
        if (!(result instanceof AuctionDetailsResult.AuctionFound found)) {
            return ResponseEntity.notFound().build();
        }

        BigDecimal hammerAmount = new BigDecimal(found.auction().currentPrice());
        String currency = found.auction().currency();

        PricingContext context = buildPricingContext(hammerAmount, currency, config);
        AuctionPriceBreakdown breakdown = auctionPricingFacade.calculate(context);

        AuctionPriceBreakdownResponse response = toBreakdownResponse(breakdown, config);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WadiumResponse> depositWadium(UUID auctionId, DepositWadiumRequest depositWadiumRequest, String xApiVersion) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID wadiumId = UUID.randomUUID();
        String partyId = authenticatedUser.partyId().value();
        WadiumDeposit deposit = new WadiumDeposit(
                wadiumId,
                auctionId,
                partyId,
                depositWadiumRequest.getAmount().getAmount(),
                depositWadiumRequest.getAmount().getCurrency()
        );
        auctionWadiumDeposits.computeIfAbsent(auctionId, k -> new ConcurrentHashMap<>())
                .put(partyId, deposit);

        WadiumResponse response = new WadiumResponse();
        response.setWadiumId(wadiumId);
        response.setAuctionId(auctionId);
        response.setStatus(WadiumResponse.StatusEnum.PAID);
        response.setAmount(depositWadiumRequest.getAmount());
        response.setRefundableOnLoss(true);
        response.setDeductibleOnWin(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<WadiumRefundResponse> refundWadium(UUID auctionId, RefundWadiumRequest refundWadiumRequest, String xApiVersion) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String partyId = authenticatedUser.partyId().value();
        Map<String, WadiumDeposit> deposits = auctionWadiumDeposits.get(auctionId);
        if (deposits == null) {
            return ResponseEntity.notFound().build();
        }
        WadiumDeposit deposit = deposits.get(partyId);
        if (deposit == null) {
            return ResponseEntity.notFound().build();
        }

        deposits.remove(partyId);

        WadiumRefundResponse response = new WadiumRefundResponse();
        response.setWadiumId(deposit.wadiumId);
        response.setAuctionId(auctionId);
        response.setStatus(WadiumRefundResponse.StatusEnum.REFUNDED);
        com.github.hexabid.contract.model.Money refundAmount = new com.github.hexabid.contract.model.Money();
        refundAmount.setAmount(deposit.amount);
        refundAmount.setCurrency(deposit.currency);
        response.setRefundAmount(refundAmount);
        return ResponseEntity.ok(response);
    }

    private PricingContext buildPricingContext(BigDecimal hammerAmount, String currency, PricingConfig config) {
        Money hammerPrice = new Money(hammerAmount, currency);

        PricingContext.Builder builder = PricingContext.builder()
                .hammerPrice(hammerPrice)
                .productType("UNIQUE");

        if (config.getVatRate() != null) {
            builder.vatRate(new VatRate(new BigDecimal(config.getVatRate())));
        }
        if (Boolean.TRUE.equals(config.getIsExcisable()) && config.getExciseRate() != null) {
            builder.excisable(true);
            if (config.getExciseType() == PricingConfig.ExciseTypeEnum.PER_UNIT) {
                builder.exciseRate(ExciseRate.perUnit(new BigDecimal(config.getExciseRate())));
            } else {
                builder.exciseRate(ExciseRate.percentage(new BigDecimal(config.getExciseRate())));
            }
        }
        if (Boolean.TRUE.equals(config.getIsImported()) && config.getCustomsDutyRate() != null) {
            builder.imported(true);
            builder.customsDutyRate(CustomsDutyRate.of(new BigDecimal(config.getCustomsDutyRate())));
        }
        if (config.getWadiumStrategy() != null) {
            if (config.getWadiumStrategy() == WadiumStrategy.PERCENTAGE && config.getWadiumRate() != null) {
                builder.wadium(Wadium.percentage(new BigDecimal(config.getWadiumRate()), hammerPrice));
            } else if (config.getWadiumStrategy() == WadiumStrategy.FIXED && config.getWadiumFixedAmount() != null) {
                builder.wadium(Wadium.fixed(new Money(new BigDecimal(config.getWadiumFixedAmount().getAmount()), currency)));
            }
        }

        return builder.build();
    }

    private AuctionPriceBreakdownResponse toBreakdownResponse(AuctionPriceBreakdown breakdown, PricingConfig config) {
        AuctionPriceBreakdownResponse response = new AuctionPriceBreakdownResponse();
        response.setHammerPrice(toContractMoney(breakdown.hammerPrice()));
        response.setWadiumOffset(toContractMoney(breakdown.wadiumOffset()));
        response.setNetto(toContractMoney(breakdown.netto()));
        response.setExcise(toContractMoney(breakdown.excise()));
        response.setCustomsDuty(toContractMoney(breakdown.customsDuty()));
        response.setVat(toContractMoney(breakdown.vat()));
        response.setTotalDue(toContractMoney(breakdown.totalDue()));

        AppliedRates rates = new AppliedRates();
        if (config.getVatRate() != null) {
            rates.setVatRate(formatPercent(config.getVatRate()));
        } else {
            rates.setVatRate("0%");
        }
        if (Boolean.TRUE.equals(config.getIsExcisable()) && config.getExciseRate() != null) {
            if (config.getExciseType() == PricingConfig.ExciseTypeEnum.PER_UNIT) {
                rates.setExciseRate(config.getExciseRate() + " PLN/u");
            } else {
                rates.setExciseRate(formatPercent(config.getExciseRate()));
            }
        }
        if (Boolean.TRUE.equals(config.getIsImported()) && config.getCustomsDutyRate() != null) {
            rates.setCustomsDutyRate(formatPercent(config.getCustomsDutyRate()));
        }
        if (config.getWadiumStrategy() != null) {
            rates.setWadiumType(AppliedRates.WadiumTypeEnum.valueOf(config.getWadiumStrategy().getValue()));
        }
        response.setAppliedRates(rates);

        return response;
    }

    private com.github.hexabid.contract.model.Money toContractMoney(Money domainMoney) {
        com.github.hexabid.contract.model.Money m = new com.github.hexabid.contract.model.Money();
        m.setAmount(domainMoney.amount().toPlainString());
        m.setCurrency(domainMoney.currency());
        return m;
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

    private String formatPercent(String rateStr) {
        BigDecimal pct = new BigDecimal(rateStr).multiply(BigDecimal.valueOf(100));
        pct = pct.setScale(2, java.math.RoundingMode.HALF_UP);
        pct = pct.stripTrailingZeros();
        return pct.toPlainString() + "%";
    }

    record WadiumDeposit(UUID wadiumId, UUID auctionId, String partyId, String amount, String currency) {}}
