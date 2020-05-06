package org.batfish.representation.aws;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Representation of an IPv4 route in AWS */
@ParametersAreNonnullByDefault
final class RoutePrefixListId extends Route {

  @Nonnull private final String _prefixListId;

  RoutePrefixListId(
      String prefixListId, State state, @Nullable String target, TargetType targetType) {
    super(state, target, targetType);
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
        && Objects.equals(_target, route._target)
        && _targetType == route._targetType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefixListId, _state, _target, _targetType);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("prefixListId", _prefixListId)
        .add("baseRoute", super.toString())
        .toString();
  }
}
