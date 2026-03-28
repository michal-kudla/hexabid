package com.acme.auctions.contract.payment.api;

import com.acme.auctions.contract.payment.model.PaymentGatewayResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-29T00:10:37.962855585+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
@Controller
@RequestMapping("${openapi.hexabidPayment.base-path:}")
public class PaymentApiController implements PaymentApi {

    private final PaymentApiDelegate delegate;

    public PaymentApiController(@Autowired(required = false) PaymentApiDelegate delegate) {
        this.delegate = Optional.ofNullable(delegate).orElse(new PaymentApiDelegate() {});
    }

    @Override
    public PaymentApiDelegate getDelegate() {
        return delegate;
    }

}
