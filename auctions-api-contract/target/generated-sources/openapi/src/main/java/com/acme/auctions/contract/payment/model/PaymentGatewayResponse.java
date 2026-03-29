package com.acme.auctions.contract.payment.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * PaymentGatewayResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-29T10:49:38.392313855+02:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public class PaymentGatewayResponse {

  private String id;

  private String name;

  private String gatewayUrl;

  public PaymentGatewayResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PaymentGatewayResponse(String id, String name, String gatewayUrl) {
    this.id = id;
    this.name = name;
    this.gatewayUrl = gatewayUrl;
  }

  public PaymentGatewayResponse id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Unique identifier of the gateway (e.g., 'payu', 'local')
   * @return id
   */
  @NotNull 
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PaymentGatewayResponse name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Human-readable name of the gateway
   * @return name
   */
  @NotNull 
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PaymentGatewayResponse gatewayUrl(String gatewayUrl) {
    this.gatewayUrl = gatewayUrl;
    return this;
  }

  /**
   * Relative or absolute URL to initiate the payment
   * @return gatewayUrl
   */
  @NotNull 
  @JsonProperty("gatewayUrl")
  public String getGatewayUrl() {
    return gatewayUrl;
  }

  public void setGatewayUrl(String gatewayUrl) {
    this.gatewayUrl = gatewayUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PaymentGatewayResponse paymentGatewayResponse = (PaymentGatewayResponse) o;
    return Objects.equals(this.id, paymentGatewayResponse.id) &&
        Objects.equals(this.name, paymentGatewayResponse.name) &&
        Objects.equals(this.gatewayUrl, paymentGatewayResponse.gatewayUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, gatewayUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentGatewayResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    gatewayUrl: ").append(toIndentedString(gatewayUrl)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

