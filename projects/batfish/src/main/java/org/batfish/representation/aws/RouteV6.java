package org.batfish.representation.aws;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix6;

/** Representation of a IPv6 route in AWS */
@ParametersAreNonnullByDefault
final class RouteV6 extends Route {

  @Nonnull private final Prefix6 _destinationCidrBlock;

  RouteV6(
      Prefix6 destinationCidrBlock, State state, @Nullable String target, TargetType targetType) {
    super(state, target, targetType);
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
        && Objects.equals(_target, route._target)
        && _targetType == route._targetType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_destinationCidrBlock, _state, _target, _targetType);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_destinationCidrBlock", _destinationCidrBlock)
        .add("baseRoute", super.toString())
        .toString();
  }
}
