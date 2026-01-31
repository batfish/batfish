package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A {@link RouteMapMatch} that matches routes based on whether the route's network is matched by at
 * least one of a set of named ipv6 prefix-lists.
 */
public final class RouteMapMatchIpv6AddressPrefixList implements RouteMapMatch {

  private final @Nonnull List<String> _names;

  public RouteMapMatchIpv6AddressPrefixList(Iterable<String> names) {
    _names = ImmutableList.copyOf(names);
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchIpv6AddressPrefixList(this);
  }

  public @Nonnull List<String> getNames() {
    return _names;
  }
}
