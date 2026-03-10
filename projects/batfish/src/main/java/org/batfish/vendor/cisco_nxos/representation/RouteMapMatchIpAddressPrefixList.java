package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A {@link RouteMapMatch} that matches routes based on whether the route's network is matched by at
 * least one of a set of named {@link IpPrefixList}s.
 */
public final class RouteMapMatchIpAddressPrefixList implements RouteMapMatch {

  private final @Nonnull List<String> _names;

  public RouteMapMatchIpAddressPrefixList(Iterable<String> names) {
    _names = ImmutableList.copyOf(names);
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchIpAddressPrefixList(this);
  }

  public @Nonnull List<String> getNames() {
    return _names;
  }
}
