package org.batfish.vendor.cisco_nxos.representation;

/**
 * A {@link RouteMapSetAsPathPrepend} that prepends the last AS to the route's as-path attribute a
 * fixed number of times.
 */
public final class RouteMapSetAsPathPrependLastAs implements RouteMapSetAsPathPrepend {

  private final int _numPrepends;

  public RouteMapSetAsPathPrependLastAs(int numPrepends) {
    _numPrepends = numPrepends;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetAsPathPrependLastAs(this);
  }

  public int getNumPrepends() {
    return _numPrepends;
  }
}
