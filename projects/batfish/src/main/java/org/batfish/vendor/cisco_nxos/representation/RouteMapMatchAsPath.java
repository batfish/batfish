package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A {@link RouteMapMatch} that matches routes based on whether the route's as-path attribute is
 * matched by named {@link IpAsPathAccessList}s.
 */
public final class RouteMapMatchAsPath implements RouteMapMatch {

  private final @Nonnull List<String> _names;

  public RouteMapMatchAsPath(Iterable<String> names) {
    _names = ImmutableList.copyOf(names);
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchAsPath(this);
  }

  public @Nonnull List<String> getNames() {
    return _names;
  }
}
