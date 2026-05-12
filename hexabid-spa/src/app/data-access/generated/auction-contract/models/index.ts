/* tslint:disable */
/* eslint-disable */
/**
 * 
 * @export
 * @interface AppliedRates
 */
export interface AppliedRates {
    /**
     * Applied VAT rate (e.g. '23%')
     * @type {string}
     * @memberof AppliedRates
     */
    vatRate: string;
    /**
     * Applied excise rate (e.g. '3.1%' or '1.17 PLN/l')
     * @type {string}
     * @memberof AppliedRates
     */
    exciseRate?: string | null;
    /**
     * Applied customs duty rate (e.g. '5%')
     * @type {string}
     * @memberof AppliedRates
     */
    customsDutyRate?: string | null;
    /**
     * 
     * @type {AppliedRatesWadiumTypeEnum}
     * @memberof AppliedRates
     */
    wadiumType?: AppliedRatesWadiumTypeEnum | null;
}

/**
* @export
* @enum {string}
*/
export enum AppliedRatesWadiumTypeEnum {
    FIXED = 'FIXED',
    PERCENTAGE = 'PERCENTAGE'
}

/**
 * 
 * @export
 * @interface AuctionListItemResponse
 */
export interface AuctionListItemResponse {
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    auctionId: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    sellerId: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    title: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    currentPrice: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    currency: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    endsAt: string;
    /**
     * 
     * @type {AuctionStatus}
     * @memberof AuctionListItemResponse
     */
    status: AuctionStatus;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    leadingBidderId?: string;
}


/**
 * 
 * @export
 * @interface AuctionListResponse
 */
export interface AuctionListResponse {
    /**
     * 
     * @type {Array<AuctionListItemResponse>}
     * @memberof AuctionListResponse
     */
    items: Array<AuctionListItemResponse>;
    /**
     * 
     * @type {string}
     * @memberof AuctionListResponse
     */
    nextCursor?: string;
}
/**
 * 
 * @export
 * @interface AuctionPriceBreakdownResponse
 */
export interface AuctionPriceBreakdownResponse {
    /**
     * 
     * @type {Money}
     * @memberof AuctionPriceBreakdownResponse
     */
    hammerPrice: Money;
    /**
     * 
     * @type {Money}
     * @memberof AuctionPriceBreakdownResponse
     */
    wadiumOffset: Money;
    /**
     * 
     * @type {Money}
     * @memberof AuctionPriceBreakdownResponse
     */
    netto: Money;
    /**
     * 
     * @type {Money}
     * @memberof AuctionPriceBreakdownResponse
     */
    excise: Money;
    /**
     * 
     * @type {Money}
     * @memberof AuctionPriceBreakdownResponse
     */
    customsDuty: Money;
    /**
     * 
     * @type {Money}
     * @memberof AuctionPriceBreakdownResponse
     */
    vat: Money;
    /**
     * 
     * @type {Money}
     * @memberof AuctionPriceBreakdownResponse
     */
    totalDue: Money;
    /**
     * 
     * @type {AppliedRates}
     * @memberof AuctionPriceBreakdownResponse
     */
    appliedRates: AppliedRates;
}
/**
 * 
 * @export
 * @interface AuctionQualificationSummary
 */
export interface AuctionQualificationSummary {
    /**
     * Name of the participation policy template assigned to this auction
     * @type {string}
     * @memberof AuctionQualificationSummary
     */
    participationPolicyTemplate?: string;
    /**
     * Human-readable label of the assigned template
     * @type {string}
     * @memberof AuctionQualificationSummary
     */
    templateLabel?: string;
    /**
     * Number of qualification tasks bidders must complete
     * @type {number}
     * @memberof AuctionQualificationSummary
     */
    taskCount?: number;
}
/**
 * 
 * @export
 * @interface AuctionResponse
 */
export interface AuctionResponse {
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    auctionId: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    sellerId: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    title: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    currentPrice: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    currency: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    endsAt: string;
    /**
     * 
     * @type {AuctionStatus}
     * @memberof AuctionResponse
     */
    status: AuctionStatus;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    leadingBidderId?: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    lotId?: string;
    /**
     * 
     * @type {AuctionResponseBuyNowPrice}
     * @memberof AuctionResponse
     */
    buyNowPrice?: AuctionResponseBuyNowPrice;
    /**
     * 
     * @type {Array<BidResponse>}
     * @memberof AuctionResponse
     */
    bids: Array<BidResponse>;
    /**
     * 
     * @type {PricingConfig}
     * @memberof AuctionResponse
     */
    pricingConfig?: PricingConfig;
    /**
     * 
     * @type {AuctionQualificationSummary}
     * @memberof AuctionResponse
     */
    qualificationSummary?: AuctionQualificationSummary;
}


/**
 * 
 * @export
 * @interface AuctionResponseBuyNowPrice
 */
export interface AuctionResponseBuyNowPrice {
    /**
     * 
     * @type {string}
     * @memberof AuctionResponseBuyNowPrice
     */
    amount?: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponseBuyNowPrice
     */
    currency?: string;
}
/**
 * 
 * @export
 * @enum {string}
 */
export enum AuctionSort {
    ENDING_SOON = 'ENDING_SOON',
    ENDING_LATEST = 'ENDING_LATEST'
}

/**
 * 
 * @export
 * @enum {string}
 */
export enum AuctionStatus {
    DRAFT = 'DRAFT',
    PUBLISHED = 'PUBLISHED',
    IN_PROGRESS = 'IN_PROGRESS',
    PENDING_SETTLEMENT = 'PENDING_SETTLEMENT',
    SETTLED = 'SETTLED',
    FAILED_SETTLEMENT = 'FAILED_SETTLEMENT',
    REOFFERED = 'REOFFERED',
    CLOSED = 'CLOSED'
}

/**
 * 
 * @export
 * @interface BatchListResponse
 */
export interface BatchListResponse {
    /**
     * List of batches
     * @type {Array<BatchResponse>}
     * @memberof BatchListResponse
     */
    items: Array<BatchResponse>;
    /**
     * 
     * @type {string}
     * @memberof BatchListResponse
     */
    nextCursor?: string | null;
}
/**
 * 
 * @export
 * @interface BatchResponse
 */
export interface BatchResponse {
    /**
     * 
     * @type {string}
     * @memberof BatchResponse
     */
    batchId: string;
    /**
     * 
     * @type {string}
     * @memberof BatchResponse
     */
    productId: string;
    /**
     * 
     * @type {string}
     * @memberof BatchResponse
     */
    name: string;
    /**
     * 
     * @type {BatchResponseQuantity}
     * @memberof BatchResponse
     */
    quantity: BatchResponseQuantity;
    /**
     * 
     * @type {string}
     * @memberof BatchResponse
     */
    dateProduced?: string;
    /**
     * 
     * @type {string}
     * @memberof BatchResponse
     */
    bestBefore?: string;
}
/**
 * 
 * @export
 * @interface BatchResponseQuantity
 */
export interface BatchResponseQuantity {
    /**
     * 
     * @type {string}
     * @memberof BatchResponseQuantity
     */
    amount?: string;
    /**
     * 
     * @type {string}
     * @memberof BatchResponseQuantity
     */
    unit?: string;
}
/**
 * 
 * @export
 * @interface BidResponse
 */
export interface BidResponse {
    /**
     * 
     * @type {string}
     * @memberof BidResponse
     */
    bidderId: string;
    /**
     * 
     * @type {string}
     * @memberof BidResponse
     */
    amount: string;
    /**
     * 
     * @type {string}
     * @memberof BidResponse
     */
    currency: string;
    /**
     * 
     * @type {string}
     * @memberof BidResponse
     */
    placedAt: string;
}
/**
 * 
 * @export
 * @interface CreateAuctionRequest
 */
export interface CreateAuctionRequest {
    /**
     * 
     * @type {string}
     * @memberof CreateAuctionRequest
     */
    title: string;
    /**
     * 
     * @type {Money}
     * @memberof CreateAuctionRequest
     */
    startingPrice: Money;
    /**
     * 
     * @type {string}
     * @memberof CreateAuctionRequest
     */
    endsAt: string;
    /**
     * 
     * @type {string}
     * @memberof CreateAuctionRequest
     */
    lotId?: string;
    /**
     * 
     * @type {Money}
     * @memberof CreateAuctionRequest
     */
    buyNowPrice?: Money;
    /**
     * 
     * @type {PricingConfig}
     * @memberof CreateAuctionRequest
     */
    pricingConfig?: PricingConfig;
    /**
     * Name of the participation policy template to assign to this auction. Determines which qualification steps bidders must complete.
     * @type {string}
     * @memberof CreateAuctionRequest
     */
    participationPolicyTemplate?: string;
}
/**
 * 
 * @export
 * @interface CreateBatchRequest
 */
export interface CreateBatchRequest {
    /**
     * 
     * @type {string}
     * @memberof CreateBatchRequest
     */
    productId: string;
    /**
     * 
     * @type {string}
     * @memberof CreateBatchRequest
     */
    name: string;
    /**
     * 
     * @type {CreateBatchRequestQuantity}
     * @memberof CreateBatchRequest
     */
    quantity: CreateBatchRequestQuantity;
    /**
     * 
     * @type {string}
     * @memberof CreateBatchRequest
     */
    dateProduced?: string;
    /**
     * 
     * @type {string}
     * @memberof CreateBatchRequest
     */
    bestBefore?: string;
}
/**
 * 
 * @export
 * @interface CreateBatchRequestQuantity
 */
export interface CreateBatchRequestQuantity {
    /**
     * 
     * @type {string}
     * @memberof CreateBatchRequestQuantity
     */
    amount: string;
    /**
     * 
     * @type {string}
     * @memberof CreateBatchRequestQuantity
     */
    unit: string;
}
/**
 * 
 * @export
 * @interface CreateInventoryInstanceRequest
 */
export interface CreateInventoryInstanceRequest {
    /**
     * 
     * @type {string}
     * @memberof CreateInventoryInstanceRequest
     */
    productId: string;
    /**
     * 
     * @type {string}
     * @memberof CreateInventoryInstanceRequest
     */
    batchId?: string;
    /**
     * 
     * @type {string}
     * @memberof CreateInventoryInstanceRequest
     */
    serialNumber?: string;
    /**
     * 
     * @type {CreateInventoryInstanceRequestQuantity}
     * @memberof CreateInventoryInstanceRequest
     */
    quantity: CreateInventoryInstanceRequestQuantity;
}
/**
 * 
 * @export
 * @interface CreateInventoryInstanceRequestQuantity
 */
export interface CreateInventoryInstanceRequestQuantity {
    /**
     * 
     * @type {string}
     * @memberof CreateInventoryInstanceRequestQuantity
     */
    amount: string;
    /**
     * 
     * @type {string}
     * @memberof CreateInventoryInstanceRequestQuantity
     */
    unit: string;
}
/**
 * 
 * @export
 * @interface CreateLotRequest
 */
export interface CreateLotRequest {
    /**
     * 
     * @type {string}
     * @memberof CreateLotRequest
     */
    title: string;
    /**
     * 
     * @type {string}
     * @memberof CreateLotRequest
     */
    description: string;
    /**
     * 
     * @type {string}
     * @memberof CreateLotRequest
     */
    inventoryEntryId: string;
    /**
     * 
     * @type {SellingMode}
     * @memberof CreateLotRequest
     */
    sellingMode: SellingMode;
    /**
     * 
     * @type {Money}
     * @memberof CreateLotRequest
     */
    reservePrice?: Money;
}


/**
 * 
 * @export
 * @interface CreateProductTypeRequest
 */
export interface CreateProductTypeRequest {
    /**
     * 
     * @type {string}
     * @memberof CreateProductTypeRequest
     */
    name: string;
    /**
     * 
     * @type {string}
     * @memberof CreateProductTypeRequest
     */
    description: string;
    /**
     * 
     * @type {ProductTrackingStrategy}
     * @memberof CreateProductTypeRequest
     */
    trackingStrategy: ProductTrackingStrategy;
    /**
     * 
     * @type {string}
     * @memberof CreateProductTypeRequest
     */
    preferredUnit: string;
}


/**
 * 
 * @export
 * @interface CurrentUserProfileResponse
 */
export interface CurrentUserProfileResponse {
    /**
     * 
     * @type {string}
     * @memberof CurrentUserProfileResponse
     */
    partyId: string;
    /**
     * 
     * @type {string}
     * @memberof CurrentUserProfileResponse
     */
    provider: string;
    /**
     * 
     * @type {string}
     * @memberof CurrentUserProfileResponse
     */
    displayName: string;
    /**
     * 
     * @type {string}
     * @memberof CurrentUserProfileResponse
     */
    email?: string;
    /**
     * 
     * @type {boolean}
     * @memberof CurrentUserProfileResponse
     */
    verified: boolean;
}
/**
 * 
 * @export
 * @interface DepositWadiumRequest
 */
export interface DepositWadiumRequest {
    /**
     * 
     * @type {Money}
     * @memberof DepositWadiumRequest
     */
    amount: Money;
}
/**
 * 
 * @export
 * @enum {string}
 */
export enum DocumentStatus {
    MISSING = 'MISSING',
    COPY = 'COPY',
    ORIGINAL = 'ORIGINAL'
}

/**
 * 
 * @export
 * @enum {string}
 */
export enum DocumentType {
    EXCISE_CERTIFICATE = 'EXCISE_CERTIFICATE',
    CUSTOMS_EXEMPTION = 'CUSTOMS_EXEMPTION',
    REAL_ESTATE_SETTLEMENT = 'REAL_ESTATE_SETTLEMENT',
    IDENTITY_VERIFICATION = 'IDENTITY_VERIFICATION',
    VEHICLE_REGISTRATION = 'VEHICLE_REGISTRATION'
}

/**
 * 
 * @export
 * @interface InventoryInstanceListResponse
 */
export interface InventoryInstanceListResponse {
    /**
     * List of inventory instances
     * @type {Array<InventoryInstanceResponse>}
     * @memberof InventoryInstanceListResponse
     */
    items: Array<InventoryInstanceResponse>;
    /**
     * 
     * @type {string}
     * @memberof InventoryInstanceListResponse
     */
    nextCursor?: string | null;
}
/**
 * 
 * @export
 * @interface InventoryInstanceResponse
 */
export interface InventoryInstanceResponse {
    /**
     * 
     * @type {string}
     * @memberof InventoryInstanceResponse
     */
    instanceId: string;
    /**
     * 
     * @type {string}
     * @memberof InventoryInstanceResponse
     */
    productId: string;
    /**
     * 
     * @type {string}
     * @memberof InventoryInstanceResponse
     */
    batchId?: string;
    /**
     * 
     * @type {string}
     * @memberof InventoryInstanceResponse
     */
    serialNumber?: string;
    /**
     * 
     * @type {BatchResponseQuantity}
     * @memberof InventoryInstanceResponse
     */
    quantity: BatchResponseQuantity;
}
/**
 * 
 * @export
 * @interface LotListResponse
 */
export interface LotListResponse {
    /**
     * List of lots
     * @type {Array<LotResponse>}
     * @memberof LotListResponse
     */
    items: Array<LotResponse>;
    /**
     * 
     * @type {string}
     * @memberof LotListResponse
     */
    nextCursor?: string | null;
}
/**
 * 
 * @export
 * @interface LotResponse
 */
export interface LotResponse {
    /**
     * 
     * @type {string}
     * @memberof LotResponse
     */
    lotId: string;
    /**
     * 
     * @type {string}
     * @memberof LotResponse
     */
    title: string;
    /**
     * 
     * @type {string}
     * @memberof LotResponse
     */
    description?: string;
    /**
     * 
     * @type {string}
     * @memberof LotResponse
     */
    inventoryEntryId?: string;
    /**
     * 
     * @type {SellingMode}
     * @memberof LotResponse
     */
    sellingMode: SellingMode;
    /**
     * 
     * @type {AuctionResponseBuyNowPrice}
     * @memberof LotResponse
     */
    reservePrice?: AuctionResponseBuyNowPrice;
}


/**
 * 
 * @export
 * @interface Money
 */
export interface Money {
    /**
     * 
     * @type {string}
     * @memberof Money
     */
    amount: string;
    /**
     * 
     * @type {string}
     * @memberof Money
     */
    currency: string;
}
/**
 * 
 * @export
 * @interface ParticipationDecisionView
 */
export interface ParticipationDecisionView {
    /**
     * 
     * @type {ParticipationDecisionViewStatusEnum}
     * @memberof ParticipationDecisionView
     */
    status: ParticipationDecisionViewStatusEnum;
    /**
     * 
     * @type {string}
     * @memberof ParticipationDecisionView
     */
    rootCause?: string;
    /**
     * 
     * @type {string}
     * @memberof ParticipationDecisionView
     */
    humanReason?: string;
    /**
     * 
     * @type {Array<string>}
     * @memberof ParticipationDecisionView
     */
    missingStatements?: Array<string>;
    /**
     * 
     * @type {Array<string>}
     * @memberof ParticipationDecisionView
     */
    cascadedStatements?: Array<string>;
    /**
     * 
     * @type {Array<string>}
     * @memberof ParticipationDecisionView
     */
    conditions?: Array<string>;
}

/**
* @export
* @enum {string}
*/
export enum ParticipationDecisionViewStatusEnum {
    PENDING = 'PENDING',
    ADMITTED = 'ADMITTED',
    ADMITTED_WITH_CONDITIONS = 'ADMITTED_WITH_CONDITIONS',
    REJECTED = 'REJECTED'
}

/**
 * 
 * @export
 * @interface PricingConfig
 */
export interface PricingConfig {
    /**
     * 
     * @type {WadiumStrategy}
     * @memberof PricingConfig
     */
    wadiumStrategy?: WadiumStrategy;
    /**
     * Rate for percentage wadium (e.g. '0.05' for 5%) or fixed amount
     * @type {string}
     * @memberof PricingConfig
     */
    wadiumRate?: string;
    /**
     * 
     * @type {Money}
     * @memberof PricingConfig
     */
    wadiumFixedAmount?: Money;
    /**
     * VAT rate as fraction (e.g. '0.23' for 23%)
     * @type {string}
     * @memberof PricingConfig
     */
    vatRate?: string;
    /**
     * 
     * @type {boolean}
     * @memberof PricingConfig
     */
    isExcisable?: boolean;
    /**
     * Excise rate as fraction or per-unit amount
     * @type {string}
     * @memberof PricingConfig
     */
    exciseRate?: string;
    /**
     * 
     * @type {PricingConfigExciseTypeEnum}
     * @memberof PricingConfig
     */
    exciseType?: PricingConfigExciseTypeEnum;
    /**
     * 
     * @type {boolean}
     * @memberof PricingConfig
     */
    isImported?: boolean;
    /**
     * Customs duty rate as fraction (e.g. '0.05' for 5%)
     * @type {string}
     * @memberof PricingConfig
     */
    customsDutyRate?: string;
}

/**
* @export
* @enum {string}
*/
export enum PricingConfigExciseTypeEnum {
    PERCENTAGE = 'PERCENTAGE',
    PER_UNIT = 'PER_UNIT'
}

/**
 * 
 * @export
 * @enum {string}
 */
export enum ProductTrackingStrategy {
    UNIQUE = 'UNIQUE',
    INDIVIDUALLY_TRACKED = 'INDIVIDUALLY_TRACKED',
    BATCH_TRACKED = 'BATCH_TRACKED',
    INDIVIDUALLY_AND_BATCH_TRACKED = 'INDIVIDUALLY_AND_BATCH_TRACKED',
    IDENTICAL = 'IDENTICAL'
}

/**
 * 
 * @export
 * @interface ProductTypeListResponse
 */
export interface ProductTypeListResponse {
    /**
     * List of product types
     * @type {Array<ProductTypeResponse>}
     * @memberof ProductTypeListResponse
     */
    items: Array<ProductTypeResponse>;
    /**
     * Cursor for pagination - pass this in next request to get next page
     * @type {string}
     * @memberof ProductTypeListResponse
     */
    nextCursor?: string | null;
}
/**
 * 
 * @export
 * @interface ProductTypeResponse
 */
export interface ProductTypeResponse {
    /**
     * 
     * @type {string}
     * @memberof ProductTypeResponse
     */
    productId: string;
    /**
     * 
     * @type {string}
     * @memberof ProductTypeResponse
     */
    name: string;
    /**
     * 
     * @type {string}
     * @memberof ProductTypeResponse
     */
    description?: string;
    /**
     * 
     * @type {ProductTrackingStrategy}
     * @memberof ProductTypeResponse
     */
    trackingStrategy: ProductTrackingStrategy;
    /**
     * 
     * @type {string}
     * @memberof ProductTypeResponse
     */
    preferredUnit: string;
}


/**
 * 
 * @export
 * @interface QualificationProfileListResponse
 */
export interface QualificationProfileListResponse {
    /**
     * 
     * @type {Array<QualificationProfileSummary>}
     * @memberof QualificationProfileListResponse
     */
    items: Array<QualificationProfileSummary>;
}
/**
 * 
 * @export
 * @interface QualificationProfileSummary
 */
export interface QualificationProfileSummary {
    /**
     * 
     * @type {string}
     * @memberof QualificationProfileSummary
     */
    templateName: string;
    /**
     * Human-readable profile name
     * @type {string}
     * @memberof QualificationProfileSummary
     */
    label: string;
    /**
     * Business-oriented description for the seller
     * @type {string}
     * @memberof QualificationProfileSummary
     */
    description?: string;
    /**
     * Number of qualification tasks bidders must complete
     * @type {number}
     * @memberof QualificationProfileSummary
     */
    taskCount: number;
    /**
     * Estimated time for bidders to complete all tasks
     * @type {string}
     * @memberof QualificationProfileSummary
     */
    estimatedMinutes?: string;
    /**
     * 
     * @type {QualificationProfileSummaryAbandonmentRiskEnum}
     * @memberof QualificationProfileSummary
     */
    abandonmentRisk?: QualificationProfileSummaryAbandonmentRiskEnum;
    /**
     * Whether this profile is recommended for general use
     * @type {boolean}
     * @memberof QualificationProfileSummary
     */
    recommended?: boolean;
}

/**
* @export
* @enum {string}
*/
export enum QualificationProfileSummaryAbandonmentRiskEnum {
    low = 'low',
    medium = 'medium',
    high = 'high'
}

/**
 * 
 * @export
 * @interface RefundWadiumRequest
 */
export interface RefundWadiumRequest {
    /**
     * ID of the bidder requesting wadium refund
     * @type {string}
     * @memberof RefundWadiumRequest
     */
    partyId: string;
}
/**
 * 
 * @export
 * @interface RuleEvaluationResponse
 */
export interface RuleEvaluationResponse {
    /**
     * 
     * @type {string}
     * @memberof RuleEvaluationResponse
     */
    auctionId: string;
    /**
     * 
     * @type {Array<RulePhaseEvaluation>}
     * @memberof RuleEvaluationResponse
     */
    evaluations: Array<RulePhaseEvaluation>;
}
/**
 * Phase of the auction lifecycle where rules apply
 * @export
 * @enum {string}
 */
export enum RulePhase {
    PARTICIPATION = 'PARTICIPATION',
    BIDDING = 'BIDDING',
    SETTLEMENT = 'SETTLEMENT'
}

/**
 * 
 * @export
 * @interface RulePhaseEvaluation
 */
export interface RulePhaseEvaluation {
    /**
     * 
     * @type {RulePhase}
     * @memberof RulePhaseEvaluation
     */
    phase: RulePhase;
    /**
     * 
     * @type {Array<RuleViolationItem>}
     * @memberof RulePhaseEvaluation
     */
    rules: Array<RuleViolationItem>;
    /**
     * 
     * @type {boolean}
     * @memberof RulePhaseEvaluation
     */
    hasBlockingViolations: boolean;
}


/**
 * Severity level of a rule violation
 * @export
 * @enum {string}
 */
export enum RuleSeverity {
    BLOCKING = 'BLOCKING',
    WARNING = 'WARNING',
    INFORMATIVE = 'INFORMATIVE'
}

/**
 * Evaluation status of a single rule
 * @export
 * @enum {string}
 */
export enum RuleStatus {
    SATISFIED = 'SATISFIED',
    PENDING = 'PENDING',
    VIOLATED = 'VIOLATED'
}

/**
 * 
 * @export
 * @interface RuleViolationItem
 */
export interface RuleViolationItem {
    /**
     * Unique identifier of the rule
     * @type {string}
     * @memberof RuleViolationItem
     */
    ruleName: string;
    /**
     * Human-readable rule evaluation message
     * @type {string}
     * @memberof RuleViolationItem
     */
    message: string;
    /**
     * Whether this violation blocks the associated action
     * @type {boolean}
     * @memberof RuleViolationItem
     */
    blocking: boolean;
    /**
     * Action required to satisfy this rule (non-empty for PENDING rules)
     * @type {string}
     * @memberof RuleViolationItem
     */
    requiredAction: string;
    /**
     * 
     * @type {RuleStatus}
     * @memberof RuleViolationItem
     */
    status: RuleStatus;
    /**
     * 
     * @type {RuleSeverity}
     * @memberof RuleViolationItem
     */
    severity: RuleSeverity;
}


/**
 * 
 * @export
 * @enum {string}
 */
export enum SellingMode {
    WHOLE = 'WHOLE',
    DIVISIBLE = 'DIVISIBLE',
    DIVISIBLE_ONLY = 'DIVISIBLE_ONLY'
}

/**
 * 
 * @export
 * @interface StartParticipationProgramRequest
 */
export interface StartParticipationProgramRequest {
    /**
     * Name of the participation policy template to use. If omitted, the auction's assigned template will be used.
     * @type {string}
     * @memberof StartParticipationProgramRequest
     */
    templateName?: string;
}
/**
 * 
 * @export
 * @interface StatementProgramView
 */
export interface StatementProgramView {
    /**
     * 
     * @type {string}
     * @memberof StatementProgramView
     */
    programInstanceId: string;
    /**
     * 
     * @type {string}
     * @memberof StatementProgramView
     */
    auctionId: string;
    /**
     * 
     * @type {string}
     * @memberof StatementProgramView
     */
    candidateId: string;
    /**
     * 
     * @type {string}
     * @memberof StatementProgramView
     */
    templateName: string;
    /**
     * 
     * @type {number}
     * @memberof StatementProgramView
     */
    templateVersion: number;
    /**
     * 
     * @type {StatementProgramViewStatusEnum}
     * @memberof StatementProgramView
     */
    status: StatementProgramViewStatusEnum;
    /**
     * 
     * @type {Array<StatementStepView>}
     * @memberof StatementProgramView
     */
    availableStatements: Array<StatementStepView>;
    /**
     * 
     * @type {Array<StatementStepView>}
     * @memberof StatementProgramView
     */
    completedStatements: Array<StatementStepView>;
    /**
     * 
     * @type {Array<StatementStepView>}
     * @memberof StatementProgramView
     */
    blockedStatements: Array<StatementStepView>;
    /**
     * 
     * @type {ParticipationDecisionView}
     * @memberof StatementProgramView
     */
    decision?: ParticipationDecisionView;
}

/**
* @export
* @enum {string}
*/
export enum StatementProgramViewStatusEnum {
    IN_PROGRESS = 'IN_PROGRESS',
    COMPLETED = 'COMPLETED',
    REJECTED = 'REJECTED',
    CANCELLED = 'CANCELLED'
}

/**
 * 
 * @export
 * @interface StatementStepView
 */
export interface StatementStepView {
    /**
     * 
     * @type {string}
     * @memberof StatementStepView
     */
    statementCode: string;
    /**
     * 
     * @type {string}
     * @memberof StatementStepView
     */
    title: string;
    /**
     * 
     * @type {string}
     * @memberof StatementStepView
     */
    question: string;
    /**
     * 
     * @type {StatementStepViewAnswerTypeEnum}
     * @memberof StatementStepView
     */
    answerType: StatementStepViewAnswerTypeEnum;
    /**
     * 
     * @type {number}
     * @memberof StatementStepView
     */
    order: number;
    /**
     * 
     * @type {string}
     * @memberof StatementStepView
     */
    stepLabel?: string;
    /**
     * 
     * @type {string}
     * @memberof StatementStepView
     */
    answerValue?: string;
}

/**
* @export
* @enum {string}
*/
export enum StatementStepViewAnswerTypeEnum {
    YES_NO = 'YES_NO',
    TEXT = 'TEXT',
    SINGLE_CHOICE = 'SINGLE_CHOICE',
    MULTI_CHOICE = 'MULTI_CHOICE',
    NUMERIC = 'NUMERIC',
    DOCUMENT_UPLOAD = 'DOCUMENT_UPLOAD'
}

/**
 * 
 * @export
 * @interface SubmitDocumentRequest
 */
export interface SubmitDocumentRequest {
    /**
     * 
     * @type {DocumentType}
     * @memberof SubmitDocumentRequest
     */
    documentType: DocumentType;
    /**
     * 
     * @type {DocumentStatus}
     * @memberof SubmitDocumentRequest
     */
    status: DocumentStatus;
}


/**
 * 
 * @export
 * @interface SubmitDocumentResponse
 */
export interface SubmitDocumentResponse {
    /**
     * 
     * @type {DocumentType}
     * @memberof SubmitDocumentResponse
     */
    documentType: DocumentType;
    /**
     * 
     * @type {DocumentStatus}
     * @memberof SubmitDocumentResponse
     */
    status: DocumentStatus;
}


/**
 * 
 * @export
 * @interface SubmitStatementAnswerRequest
 */
export interface SubmitStatementAnswerRequest {
    /**
     * The candidate's answer value
     * @type {string}
     * @memberof SubmitStatementAnswerRequest
     */
    answerValue: string;
}
/**
 * 
 * @export
 * @interface SubmitStatementAnswerResponse
 */
export interface SubmitStatementAnswerResponse {
    /**
     * 
     * @type {SubmitStatementAnswerResponseResultTypeEnum}
     * @memberof SubmitStatementAnswerResponse
     */
    resultType: SubmitStatementAnswerResponseResultTypeEnum;
    /**
     * 
     * @type {string}
     * @memberof SubmitStatementAnswerResponse
     */
    reason?: string;
    /**
     * 
     * @type {Array<string>}
     * @memberof SubmitStatementAnswerResponse
     */
    missingPrerequisites?: Array<string>;
    /**
     * 
     * @type {StatementProgramView}
     * @memberof SubmitStatementAnswerResponse
     */
    program: StatementProgramView;
}

/**
* @export
* @enum {string}
*/
export enum SubmitStatementAnswerResponseResultTypeEnum {
    ACCEPTED = 'ACCEPTED',
    REJECTED = 'REJECTED',
    PREREQUISITE_NOT_MET = 'PREREQUISITE_NOT_MET'
}

/**
 * 
 * @export
 * @interface WadiumRefundResponse
 */
export interface WadiumRefundResponse {
    /**
     * 
     * @type {string}
     * @memberof WadiumRefundResponse
     */
    wadiumId: string;
    /**
     * 
     * @type {string}
     * @memberof WadiumRefundResponse
     */
    auctionId: string;
    /**
     * 
     * @type {WadiumRefundResponseStatusEnum}
     * @memberof WadiumRefundResponse
     */
    status: WadiumRefundResponseStatusEnum;
    /**
     * 
     * @type {Money}
     * @memberof WadiumRefundResponse
     */
    refundAmount: Money;
}

/**
* @export
* @enum {string}
*/
export enum WadiumRefundResponseStatusEnum {
    REFUNDED = 'REFUNDED'
}

/**
 * 
 * @export
 * @interface WadiumResponse
 */
export interface WadiumResponse {
    /**
     * 
     * @type {string}
     * @memberof WadiumResponse
     */
    wadiumId: string;
    /**
     * 
     * @type {string}
     * @memberof WadiumResponse
     */
    auctionId: string;
    /**
     * 
     * @type {WadiumResponseStatusEnum}
     * @memberof WadiumResponse
     */
    status: WadiumResponseStatusEnum;
    /**
     * 
     * @type {Money}
     * @memberof WadiumResponse
     */
    amount: Money;
    /**
     * 
     * @type {boolean}
     * @memberof WadiumResponse
     */
    refundableOnLoss: boolean;
    /**
     * 
     * @type {boolean}
     * @memberof WadiumResponse
     */
    deductibleOnWin: boolean;
}

/**
* @export
* @enum {string}
*/
export enum WadiumResponseStatusEnum {
    PAID = 'PAID',
    REFUNDED = 'REFUNDED',
    DEDUCTED = 'DEDUCTED'
}

/**
 * Strategy for calculating wadium amount
 * @export
 * @enum {string}
 */
export enum WadiumStrategy {
    FIXED = 'FIXED',
    PERCENTAGE = 'PERCENTAGE'
}

