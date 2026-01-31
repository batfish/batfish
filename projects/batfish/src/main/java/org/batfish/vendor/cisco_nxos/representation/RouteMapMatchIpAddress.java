package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;

/** A {@link RouteMapMatch} that matches packets in PBR using a named {@link IpAccessList}. */
public final class RouteMapMatchIpAddress implements RouteMapMatch {

  private final @Nonnull String _name;

  public RouteMapMatchIpAddress(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchIpAddress(this);
  }

  public @Nonnull String getName() {
    return _name;
  }
}
