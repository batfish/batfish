package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.representation.aws.TransitGatewayAttachment.ResourceType;

/**
 * Represents AWS Transit Gateway Propagations
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/get-transit-gateway-route-table-propagations.html
 *
 * <p>The JSON input is a custom format that wraps around multiple calls to the API above, once per
 * route table. See the test file for the format.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class TransitGatewayPropagations implements AwsVpcEntity, Serializable {

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class Propagation implements Serializable {

    @Nonnull private final String _attachmentId;

    @Nonnull private final ResourceType _resourceType;

    @Nonnull private final String _resourceId;

    private final boolean _enabled;

    @JsonCreator
    private static Propagation create(
        @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ATTACHMENT_ID) String attachmentId,
        @Nullable @JsonProperty(JSON_KEY_RESOURCE_ID) String resourceId,
        @Nullable @JsonProperty(JSON_KEY_RESOURCE_TYPE) String resourceType,
        @Nullable @JsonProperty(JSON_KEY_STATE) String state) {
      checkArgument(
          attachmentId != null, "Attachment id cannot be null for transit gateway attachment");
      checkArgument(
          resourceType != null, "Resource type cannot be null for transit gateway propagation");
      checkArgument(
          resourceId != null, "Resource id cannot be null for transit gateway propagation");
      checkArgument(state != null, "State cannot be null for transit gateway attachment");

      return new Propagation(
          attachmentId,
          ResourceType.valueOf(resourceType.toUpperCase().replace('-', '_')),
          resourceId,
          state.equalsIgnoreCase("enabled"));
    }

    Propagation(
        String attachmentId, ResourceType resourceType, String resourceId, boolean enabled) {
      _attachmentId = attachmentId;
      _resourceId = resourceId;
      _resourceType = resourceType;
      _enabled = enabled;
    }

    @Nonnull
    public String getAttachmentId() {
      return _attachmentId;
    }

    public boolean isEnabled() {
      return _enabled;
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
      if (!(o instanceof Propagation)) {
        return false;
      }
      Propagation that = (Propagation) o;
      return _enabled == that._enabled
          && Objects.equals(_resourceId, that._resourceId)
          && Objects.equals(_resourceType, that._resourceType)
          && Objects.equals(_attachmentId, that._attachmentId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_attachmentId, _resourceId, _resourceType, _enabled);
    }
  }

  @Nonnull private final String _routeTableId;

  @Nonnull private final List<Propagation> _propagations;

  @JsonCreator
  private static TransitGatewayPropagations create(
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ROUTE_TABLE_ID) String routeTableId,
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ROUTE_TABLE_PROPAGATIONS)
          List<Propagation> propagations) {
    checkArgument(routeTableId != null, "Route table id cannot be null for transit gateway");
    checkArgument(propagations != null, "Propagations cannot be null for transit gateway");

    return new TransitGatewayPropagations(routeTableId, propagations);
  }

  TransitGatewayPropagations(String routeTableId, List<Propagation> propagations) {
    _routeTableId = routeTableId;
    _propagations = propagations;
  }

  @Nonnull
  public List<Propagation> getPropagations() {
    return _propagations;
  }

  @Nonnull
  @Override
  public String getId() {
    return _routeTableId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TransitGatewayPropagations)) {
      return false;
    }
    TransitGatewayPropagations that = (TransitGatewayPropagations) o;
    return Objects.equals(_routeTableId, that._routeTableId)
        && Objects.equals(_propagations, that._propagations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_routeTableId, _propagations);
  }
}
