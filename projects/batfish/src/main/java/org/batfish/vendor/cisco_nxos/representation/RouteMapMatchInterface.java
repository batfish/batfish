package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A {@link RouteMapMatch} that matches routes based on whether the route is assigned to one of the
 * named {@link Interface}s.
 */
public final class RouteMapMatchInterface implements RouteMapMatch {

  private final @Nonnull List<String> _names;

  public RouteMapMatchInterface(Iterable<String> names) {
    _names = ImmutableList.copyOf(names);
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchInterface(this);
  }

  public @Nonnull List<String> getNames() {
    return _names;
  }
}
