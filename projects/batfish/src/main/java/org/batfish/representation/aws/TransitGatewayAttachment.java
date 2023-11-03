package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
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

  /** Different types of TGW attachments */
  enum ResourceType {
    DIRECT_CONNECT_GATEWAY,
    PEERING,
    VPC,
    VPN
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class Association implements Serializable {

    private final @Nonnull String _routeTableId;

    private final @Nonnull String _state;

    @JsonCreator
    private static Association create(
        @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ROUTE_TABLE_ID) @Nullable String routeTableId,
        @JsonProperty(JSON_KEY_STATE) @Nullable String state) {
      checkArgument(
          routeTableId != null, "Route table id cannot be null for transit gateway attachment");
      checkArgument(state != null, "State cannot be null for transit gateway attachment");

      return new Association(routeTableId, state);
    }

    Association(String routeTableId, String state) {
      _routeTableId = routeTableId;
      _state = state;
    }

    public @Nonnull String getRouteTableId() {
      return _routeTableId;
    }

    public @Nonnull String getState() {
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
      return Objects.equals(_routeTableId, that._routeTableId)
          && Objects.equals(_state, that._state);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_routeTableId, _state);
    }
  }

  private final @Nonnull String _attachmentId;

  private final @Nonnull String _gatewayId;

  private final @Nonnull String _gatewayOwnerId;

  private final @Nonnull ResourceType _resourceType;

  private final @Nonnull String _resourceId;

  private final @Nonnull String _resourceOwnerId;

  private final @Nullable Association _association;

  @JsonCreator
  private static TransitGatewayAttachment create(
      @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ATTACHMENT_ID) @Nullable String attachmentId,
      @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ID) @Nullable String gatewayId,
      @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_OWNER_ID) @Nullable String gatewayOwnerId,
      @JsonProperty(JSON_KEY_RESOURCE_TYPE) @Nullable String resourceType,
      @JsonProperty(JSON_KEY_RESOURCE_ID) @Nullable String resourceId,
      @JsonProperty(JSON_KEY_RESOURCE_OWNER_ID) @Nullable String resourceOwnerId,
      @JsonProperty(JSON_KEY_ASSOCIATION) @Nullable Association association) {
    checkArgument(
        attachmentId != null, "Attachment id cannot be null for transit gateway attachment");
    checkArgument(gatewayId != null, "Gateway id cannot be null for transit gateway attachment");
    checkNonNull(gatewayOwnerId, JSON_KEY_TRANSIT_GATEWAY_OWNER_ID, "transit gateway attachment");
    checkArgument(
        resourceType != null, "Resource type cannot be null for transit gateway attachment");
    checkArgument(resourceId != null, "Resource id cannot be null for transit gateway attachment");
    checkNonNull(resourceOwnerId, JSON_KEY_RESOURCE_OWNER_ID, "transit gateway attachment");
    // association can be null

    return new TransitGatewayAttachment(
        attachmentId,
        gatewayId,
        gatewayOwnerId,
        Enum.valueOf(ResourceType.class, resourceType.toUpperCase().replace('-', '_')),
        resourceId,
        resourceOwnerId,
        association);
  }

  TransitGatewayAttachment(
      String attachmentId,
      String gatewayId,
      String gatewayOwnerId,
      ResourceType resourceType,
      String resourceId,
      String resourceOwnerId,
      @Nullable Association association) {
    _attachmentId = attachmentId;
    _gatewayId = gatewayId;
    _gatewayOwnerId = gatewayOwnerId;
    _resourceType = resourceType;
    _resourceId = resourceId;
    _resourceOwnerId = resourceOwnerId;
    _association = association;
  }

  public @Nullable Association getAssociation() {
    return _association;
  }

  @Override
  public @Nonnull String getId() {
    return _attachmentId;
  }

  public @Nonnull String getGatewayId() {
    return _gatewayId;
  }

  public @Nonnull String getGatewayOwnerId() {
    return _gatewayOwnerId;
  }

  public @Nonnull ResourceType getResourceType() {
    return _resourceType;
  }

  public @Nonnull String getResourceId() {
    return _resourceId;
  }

  public @Nonnull String getResourceOwnerId() {
    return _resourceOwnerId;
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
    return Objects.equals(_attachmentId, that._attachmentId)
        && Objects.equals(_gatewayId, that._gatewayId)
        && _gatewayOwnerId.equals(that._gatewayOwnerId)
        && _resourceType == that._resourceType
        && Objects.equals(_resourceId, that._resourceId)
        && _resourceOwnerId.equals(that._resourceOwnerId)
        && Objects.equals(_association, that._association);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _attachmentId,
        _gatewayId,
        _gatewayOwnerId,
        _resourceType,
        _resourceId,
        _resourceOwnerId,
        _association);
  }
}
