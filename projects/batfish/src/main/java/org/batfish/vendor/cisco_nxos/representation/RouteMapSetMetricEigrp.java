package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;

/** A {@link RouteMapSet} that sets the EIGRP metric for a route. */
public final class RouteMapSetMetricEigrp implements RouteMapSet {

  private final @Nonnull EigrpMetric _metric;

  public RouteMapSetMetricEigrp(EigrpMetric metric) {
    _metric = metric;
  }

  public @Nonnull EigrpMetric getMetric() {
    return _metric;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetMetricEigrp(this);
  }
}
