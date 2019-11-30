package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents AWS Transit Gateway Static Routes
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

    private final boolean _enabled;

    @JsonCreator
    private static Propagation create(
        @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ATTACHMENT_ID) String attachmentId,
        @Nullable @JsonProperty(JSON_KEY_STATE) String state) {
      checkArgument(
          attachmentId != null, "Attachment id cannot be null for transit gateway attachment");
      checkArgument(state != null, "State cannot be nul for transit gateway attachment");

      return new Propagation(attachmentId, state.equalsIgnoreCase("enabled"));
    }

    Propagation(String attachmentId, boolean enabled) {
      _attachmentId = attachmentId;
      _enabled = enabled;
    }

    @Nonnull
    public String getAttachmentId() {
      return _attachmentId;
    }

    public boolean isEnabled() {
      return _enabled;
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
      return _enabled == that._enabled && Objects.equal(_attachmentId, that._attachmentId);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_attachmentId, _enabled);
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
    checkArgument(propagations != null, "Propagations cannot be nul for transit gateway");

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
    return Objects.equal(_routeTableId, that._routeTableId)
        && Objects.equal(_propagations, that._propagations);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_routeTableId, _propagations);
  }
}
