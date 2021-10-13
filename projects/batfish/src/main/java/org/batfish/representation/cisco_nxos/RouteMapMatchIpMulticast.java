package org.batfish.representation.cisco_nxos;

/** A {@link RouteMapMatch} that matches multicast routes. Unimplemented and evaluates to false. */
// TODO: flesh out.
public final class RouteMapMatchIpMulticast implements RouteMapMatch {

  public RouteMapMatchIpMulticast() {}

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchIpMulticast(this);
  }

  @Override
  public int hashCode() {
    return RouteMapMatchIpMulticast.class.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof RouteMapMatchIpMulticast;
  }
}
