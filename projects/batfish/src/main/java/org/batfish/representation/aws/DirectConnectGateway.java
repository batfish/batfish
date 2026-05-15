package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an AWS Direct Connect Gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class DirectConnectGateway implements AwsVpcEntity, Serializable {

  static final String JSON_KEY_DIRECT_CONNECT_GATEWAYS = "DirectConnectGateways";
  static final String JSON_KEY_DIRECT_CONNECT_GATEWAY_ID = "DirectConnectGatewayId";
  static final String JSON_KEY_DIRECT_CONNECT_GATEWAY_NAME = "DirectConnectGatewayName";

  private final @Nonnull String _directConnectGatewayId;

  private final @Nonnull String _directConnectGatewayName;

  private final long _amazonSideAsn;

  private final @Nonnull Map<String, String> _tags;

  @JsonCreator
  private static DirectConnectGateway create(
      @JsonProperty(JSON_KEY_DIRECT_CONNECT_GATEWAY_ID) @Nullable String directConnectGatewayId,
      @JsonProperty(JSON_KEY_DIRECT_CONNECT_GATEWAY_NAME) @Nullable String directConnectGatewayName,
      @JsonProperty(JSON_KEY_AMAZON_SIDE_ASN) @Nullable Long amazonSideAsn,
      @JsonProperty(JSON_KEY_TAGS) @Nullable List<Tag> tags) {
    checkArgument(directConnectGatewayId != null, "Direct Connect Gateway id cannot be null");
    checkArgument(directConnectGatewayName != null, "Direct Connect Gateway name cannot be null");
    checkArgument(
        amazonSideAsn != null, "Amazon side ASN cannot be null for Direct Connect Gateway");

    return new DirectConnectGateway(
        directConnectGatewayId,
        directConnectGatewayName,
        amazonSideAsn,
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)));
  }

  DirectConnectGateway(
      String directConnectGatewayId,
      String directConnectGatewayName,
      long amazonSideAsn,
      Map<String, String> tags) {
    _directConnectGatewayId = directConnectGatewayId;
    _directConnectGatewayName = directConnectGatewayName;
    _amazonSideAsn = amazonSideAsn;
    _tags = tags;
  }

  @Override
  public @Nonnull String getId() {
    return _directConnectGatewayId;
  }

  public @Nonnull String getDirectConnectGatewayName() {
    return _directConnectGatewayName;
  }

  public long getAmazonSideAsn() {
    return _amazonSideAsn;
  }

  public @Nonnull Map<String, String> getTags() {
    return _tags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DirectConnectGateway)) {
      return false;
    }
    DirectConnectGateway that = (DirectConnectGateway) o;
    return _amazonSideAsn == that._amazonSideAsn
        && Objects.equals(_directConnectGatewayId, that._directConnectGatewayId)
        && Objects.equals(_directConnectGatewayName, that._directConnectGatewayName)
        && Objects.equals(_tags, that._tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_directConnectGatewayId, _directConnectGatewayName, _amazonSideAsn, _tags);
  }
}
