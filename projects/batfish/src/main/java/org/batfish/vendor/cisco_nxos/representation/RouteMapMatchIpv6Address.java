package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;

/** A {@link RouteMapMatch} that matches packets in PBR using a named ipv6 access-list. */
public final class RouteMapMatchIpv6Address implements RouteMapMatch {

  private final @Nonnull String _name;

  public RouteMapMatchIpv6Address(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchIpv6Address(this);
  }

  public @Nonnull String getName() {
    return _name;
  }
}
