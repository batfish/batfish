package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Represents the configuration of a customer gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class CustomerGateway implements AwsVpcEntity, Serializable {

  private final @Nonnull String _bgpAsn;

  private final @Nonnull String _customerGatewayId;

  private final @Nonnull Ip _ipAddress;

  private final @Nonnull String _type;

  @JsonCreator
  private static CustomerGateway create(
      @JsonProperty(JSON_KEY_CUSTOMER_GATEWAY_ID) @Nullable String customerGatewayId,
      @JsonProperty(JSON_KEY_IP_ADDRESS) @Nullable String ipAddress,
      @JsonProperty(JSON_KEY_TYPE) @Nullable String type,
      @JsonProperty(JSON_KEY_BGP_ASN) @Nullable String bgpAsn) {
    checkArgument(customerGatewayId != null, "Customer gateway id cannot be null");
    checkArgument(ipAddress != null, "Ip address cannot be null for customer gateway");
    checkArgument(type != null, "Type cannot be null for customer gateway");
    checkArgument(bgpAsn != null, "BGP ASN cannot be null for customer gateway");

    return new CustomerGateway(customerGatewayId, Ip.parse(ipAddress), type, bgpAsn);
  }

  public CustomerGateway(String customerGatewayId, Ip ipAddress, String type, String bgpAsn) {
    _customerGatewayId = customerGatewayId;
    _ipAddress = ipAddress;
    _type = type;
    _bgpAsn = bgpAsn;
  }

  public @Nonnull String getBgpAsn() {
    return _bgpAsn;
  }

  public @Nonnull String getCustomerGatewayId() {
    return _customerGatewayId;
  }

  @Override
  public String getId() {
    return _customerGatewayId;
  }

  public @Nonnull Ip getIpAddress() {
    return _ipAddress;
  }

  public @Nonnull String getType() {
    return _type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CustomerGateway)) {
      return false;
    }
    CustomerGateway that = (CustomerGateway) o;
    return Objects.equals(_bgpAsn, that._bgpAsn)
        && Objects.equals(_customerGatewayId, that._customerGatewayId)
        && Objects.equals(_ipAddress, that._ipAddress)
        && Objects.equals(_type, that._type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_bgpAsn, _customerGatewayId, _ipAddress, _type);
  }
}
