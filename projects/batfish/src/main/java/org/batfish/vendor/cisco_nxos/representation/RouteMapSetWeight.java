package org.batfish.vendor.cisco_nxos.representation;

/** A {@link RouteMapSet} that sets the weight of a BGP route. */
public class RouteMapSetWeight implements RouteMapSet {

  private final int _weight;

  public RouteMapSetWeight(int weight) {
    _weight = weight;
  }

  public int getWeight() {
    return _weight;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetWeight(this);
  }
}
