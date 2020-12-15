package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;

/** A {@link RouteMapSet} that sets the EIGRP metric for a route. */
public final class RouteMapSetMetricEigrp implements RouteMapSet {

  @Nonnull private final EigrpMetric _metric;

  public RouteMapSetMetricEigrp(EigrpMetric metric) {
    _metric = metric;
  }

  @Nonnull
  public EigrpMetric getMetric() {
    return _metric;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetMetricEigrp(this);
  }
}
