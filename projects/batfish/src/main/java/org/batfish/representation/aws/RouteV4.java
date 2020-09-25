package org.batfish.representation.aws;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/** Representation of an IPv4 route in AWS */
@ParametersAreNonnullByDefault
public final class RouteV4 extends Route {

  @Nonnull private final Prefix _destinationCidrBlock;

  /** Deprecated constructor */
  @VisibleForTesting
  RouteV4(
      Prefix destinationCidrBlock, State state, @Nullable String target, TargetType targetType) {
    super(state, new RouteTarget(target, targetType));
    _destinationCidrBlock = destinationCidrBlock;
  }

  public RouteV4(Prefix destinationCidrBlock, State state, RouteTarget routeTarget) {
    super(state, routeTarget);
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
    if (!(o instanceof RouteV4)) {
      return false;
    }
    RouteV4 route = (RouteV4) o;
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
