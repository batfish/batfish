package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.NextHop;

@ParametersAreNonnullByDefault
public abstract class RipRoute extends AbstractRoute {

  /** Maximum allowable route metric in RIP */
  public static final long MAX_ROUTE_METRIC = 16;

  protected final long _metric;

  protected RipRoute(Prefix network, NextHop nextHop, long admin, long metric, long tag) {
    super(network, admin, tag, false, false);
    checkArgument(
        metric >= 0 && metric <= MAX_ROUTE_METRIC,
        "Invalid RIP route metric %s. Must be between 0 and %s",
        metric,
        MAX_ROUTE_METRIC);
    _metric = metric;
    _nextHop = nextHop;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  @Override
  public final long getMetric() {
    return _metric;
  }
}
