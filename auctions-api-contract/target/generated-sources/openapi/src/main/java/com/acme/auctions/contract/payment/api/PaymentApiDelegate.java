package com.acme.auctions.contract.payment.api;

import com.acme.auctions.contract.payment.model.PaymentGatewayResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

/**
 * A delegate to be called by the {@link PaymentApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-29T10:49:38.392313855+02:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public interface PaymentApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /api/payments/gateways : Get available payment gateways
     *
     * @return List of available gateways (status code 200)
     * @see PaymentApi#getPaymentGateways
     */
    default ResponseEntity<List<PaymentGatewayResponse>> getPaymentGateways() {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "[ { \"gatewayUrl\" : \"gatewayUrl\", \"name\" : \"name\", \"id\" : \"id\" }, { \"gatewayUrl\" : \"gatewayUrl\", \"name\" : \"name\", \"id\" : \"id\" } ]";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
