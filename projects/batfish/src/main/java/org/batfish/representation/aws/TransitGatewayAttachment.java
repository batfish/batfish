package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents an AWS Transit Gateway Attachment
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-transit-gateway-attachments.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class TransitGatewayAttachment implements AwsVpcEntity, Serializable {

  enum ResourceType {
    VPC,
    VPN
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class Association implements Serializable {

    @Nonnull private final String _routeTableId;

    @Nonnull private final String _state;

    @JsonCreator
    private static Association create(
        @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ROUTE_TABLE_ID) String routeTableId,
        @Nullable @JsonProperty(JSON_KEY_STATE) String state) {
      checkArgument(
          routeTableId != null, "Route table id cannot be null for transit gateway attachment");
      checkArgument(state != null, "State cannot be null for transit gateway attachment");

      return new Association(routeTableId, state);
    }

    Association(String routeTableId, String state) {
      _routeTableId = routeTableId;
      _state = state;
    }

    @Nonnull
    public String getRouteTableId() {
      return _routeTableId;
    }

    @Nonnull
    public String getState() {
      return _state;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Association)) {
        return false;
      }
      Association that = (Association) o;
      return Objects.equal(_routeTableId, that._routeTableId) && Objects.equal(_state, that._state);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_routeTableId, _state);
    }
  }

  @Nonnull private final String _attachmentId;

  @Nonnull private final String _gatewayId;

  @Nonnull private final ResourceType _resourceType;

  @Nonnull private final String _resourceId;

  @Nonnull private final Association _association;

  @JsonCreator
  private static TransitGatewayAttachment create(
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ATTACHMENT_ID) String attachmentId,
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ID) String gatewayId,
      @Nullable @JsonProperty(JSON_KEY_RESOURCE_TYPE) String resourceType,
      @Nullable @JsonProperty(JSON_KEY_RESOURCE_ID) String resourceId,
      @Nullable @JsonProperty(JSON_KEY_ASSOCIATION) Association association) {
    checkArgument(
        attachmentId != null, "Attachment id cannot be null for transit gateway attachment");
    checkArgument(gatewayId != null, "Gateway id cannot be nul for transit gateway attachment");
    checkArgument(
        resourceType != null, "Resource type cannot be nul for transit gateway attachment");
    checkArgument(resourceId != null, "Resource id cannot be nul for transit gateway attachment");
    checkArgument(association != null, "Association cannot be nul for transit gateway attachment");

    return new TransitGatewayAttachment(
        attachmentId,
        gatewayId,
        Enum.valueOf(ResourceType.class, resourceType.toUpperCase()),
        resourceId,
        association);
  }

  TransitGatewayAttachment(
      String attachmentId,
      String gatewayId,
      ResourceType resourceType,
      String resourceId,
      Association association) {
    _attachmentId = attachmentId;
    _gatewayId = gatewayId;
    _resourceType = resourceType;
    _resourceId = resourceId;
    _association = association;
  }

  @Nonnull
  public Association getAssociation() {
    return _association;
  }

  @Nonnull
  @Override
  public String getId() {
    return _attachmentId;
  }

  @Nonnull
  public String getGatewayId() {
    return _gatewayId;
  }

  @Nonnull
  public ResourceType getResourceType() {
    return _resourceType;
  }

  @Nonnull
  public String getResourceId() {
    return _resourceId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TransitGatewayAttachment)) {
      return false;
    }
    TransitGatewayAttachment that = (TransitGatewayAttachment) o;
    return Objects.equal(_attachmentId, that._attachmentId)
        && Objects.equal(_gatewayId, that._gatewayId)
        && _resourceType == that._resourceType
        && Objects.equal(_resourceId, that._resourceId)
        && Objects.equal(_association, that._association);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_attachmentId, _gatewayId, _resourceType, _resourceId, _association);
  }
}
