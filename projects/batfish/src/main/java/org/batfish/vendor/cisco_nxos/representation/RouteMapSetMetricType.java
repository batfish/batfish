package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;

/**
 * A {@link RouteMapSet} that sets the metric-type an OSPF or IS-IS route, or dynamically sets the
 * MED for a BGP route based on IGP cost.
 */
public final class RouteMapSetMetricType implements RouteMapSet {

  private final @Nonnull RouteMapMetricType _metricType;

  public RouteMapSetMetricType(RouteMapMetricType metricType) {
    _metricType = metricType;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetMetricType(this);
  }

  public @Nonnull RouteMapMetricType getMetricType() {
    return _metricType;
  }
}
