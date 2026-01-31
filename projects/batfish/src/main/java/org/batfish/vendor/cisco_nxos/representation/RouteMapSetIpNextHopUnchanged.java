package org.batfish.vendor.cisco_nxos.representation;

/**
 * A {@link RouteMapSetIpNextHop} that retains the NEXT_HOP attribute of the route received from an
 * eBGP peer.
 */
public final class RouteMapSetIpNextHopUnchanged implements RouteMapSetIpNextHop {

  public static final RouteMapSetIpNextHopUnchanged INSTANCE = new RouteMapSetIpNextHopUnchanged();

  private RouteMapSetIpNextHopUnchanged() {}

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetIpNextHopUnchanged(this);
  }
}
