package org.batfish.vendor.cisco_nxos.representation;

/** A {@link RouteMapSet} that sets the metric/MED attribute of a route. */
public final class RouteMapSetMetric implements RouteMapSet {

  private final long _metric;

  public RouteMapSetMetric(long metric) {
    _metric = metric;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetMetric(this);
  }

  public long getMetric() {
    return _metric;
  }
}
