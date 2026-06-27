/* tslint:disable */
/* eslint-disable */
/**
 * 
 * @export
 * @interface PaymentGatewayResponse
 */
export interface PaymentGatewayResponse {
    /**
     * Unique identifier of the gateway (e.g., 'payu', 'local')
     * @type {string}
     * @memberof PaymentGatewayResponse
     */
    id: string;
    /**
     * Human-readable name of the gateway
     * @type {string}
     * @memberof PaymentGatewayResponse
     */
    name: string;
    /**
     * Relative or absolute URL to initiate the payment
     * @type {string}
     * @memberof PaymentGatewayResponse
     */
    gatewayUrl: string;
}
