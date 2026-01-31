package org.batfish.vendor.cisco_nxos.representation;

/** A {@link RouteMapMatch} that matches routes based on the metric/MED attribute. */
public final class RouteMapMatchMetric implements RouteMapMatch {

  private final long _metric;

  public RouteMapMatchMetric(long metric) {
    _metric = metric;
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchMetric(this);
  }

  public long getMetric() {
    return _metric;
  }
}
