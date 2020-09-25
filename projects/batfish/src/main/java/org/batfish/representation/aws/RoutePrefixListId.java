package org.batfish.representation.aws;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Representation of an IPv4 route in AWS */
@ParametersAreNonnullByDefault
final class RoutePrefixListId extends Route {

  @Nonnull private final String _prefixListId;

  /** Deprecated constructor */
  @VisibleForTesting
  RoutePrefixListId(
      String prefixListId, State state, @Nullable String targetId, TargetType targetType) {
    super(state, new RouteTarget(targetId, targetType));
    _prefixListId = prefixListId;
  }

  RoutePrefixListId(String prefixListId, State state, RouteTarget routeTarget) {
    super(state, routeTarget);
    _prefixListId = prefixListId;
  }

  @Nonnull
  public String getPrefixListId() {
    return _prefixListId;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RoutePrefixListId)) {
      return false;
    }
    RoutePrefixListId route = (RoutePrefixListId) o;
    return Objects.equals(_prefixListId, route._prefixListId)
        && _state == route._state
        && Objects.equals(_routeTarget, route._routeTarget);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefixListId, _state, _routeTarget);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("prefixListId", _prefixListId)
        .add("baseRoute", super.toString())
        .toString();
  }
}
