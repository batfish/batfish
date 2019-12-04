package org.batfish.representation.aws;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.Route.State;

@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class TransitGatewayRouteV4 extends TransitGatewayRoute {

  @Nonnull private final Prefix _destinationCidrBlock;

  TransitGatewayRouteV4(
      Prefix destinationCidrBlock, State state, Type type, List<String> attachmentIds) {
    super(state, type, attachmentIds);
    _destinationCidrBlock = destinationCidrBlock;
  }

  @Nonnull
  public Prefix getDestinationCidrBlock() {
    return _destinationCidrBlock;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TransitGatewayRouteV4)) {
      return false;
    }
    TransitGatewayRouteV4 that = (TransitGatewayRouteV4) o;
    return Objects.equals(_destinationCidrBlock, that._destinationCidrBlock)
        && _state == that._state
        && _type == that._type
        && Objects.equals(_attachmentIds, that._attachmentIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_destinationCidrBlock, _state, _type, _attachmentIds);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_destinationCidrBlock", _destinationCidrBlock)
        .add("_parent", super.toString())
        .toString();
  }
}
