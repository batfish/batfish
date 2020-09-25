package org.batfish.representation.aws;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix6;

/** Representation of a IPv6 route in AWS */
@ParametersAreNonnullByDefault
public final class RouteV6 extends Route {

  @Nonnull private final Prefix6 _destinationCidrBlock;

  /** Deprecated constructor */
  @VisibleForTesting
  RouteV6(
      Prefix6 destinationCidrBlock, State state, @Nullable String targetId, TargetType targetType) {
    super(state, new RouteTarget(targetId, targetType));
    _destinationCidrBlock = destinationCidrBlock;
  }

  public RouteV6(Prefix6 destinationCidrBlock, State state, RouteTarget routeTarget) {
    super(state, routeTarget);
    _destinationCidrBlock = destinationCidrBlock;
  }

  @Nonnull
  public Prefix6 getDestinationCidrBlock() {
    return _destinationCidrBlock;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RouteV6)) {
      return false;
    }
    RouteV6 route = (RouteV6) o;
    return Objects.equals(_destinationCidrBlock, route._destinationCidrBlock)
        && _state == route._state
        && Objects.equals(_routeTarget, route._routeTarget);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_destinationCidrBlock, _state, _routeTarget);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_destinationCidrBlock", _destinationCidrBlock)
        .add("baseRoute", super.toString())
        .toString();
  }
}
