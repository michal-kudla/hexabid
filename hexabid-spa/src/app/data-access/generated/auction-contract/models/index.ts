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
    OPEN = 'OPEN',
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

