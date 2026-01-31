package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;

/** A {@link RouteMapMatch} that matches routes based on type of the route. */
public final class RouteMapMatchRouteType implements RouteMapMatch {
  public enum Type {
    EXTERNAL,
    INTERNAL,
    LOCAL,
    NSSA_EXTERNAL,
    TYPE_1,
    TYPE_2,
  }

  public RouteMapMatchRouteType(@Nonnull Set<Type> types) {
    _types = ImmutableSet.copyOf(types);
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchRouteType(this);
  }

  public @Nonnull Set<Type> getTypes() {
    return _types;
  }

  private final Set<Type> _types;
}
