package org.batfish.vendor.arista.representation;

import org.batfish.datamodel.RoutingProtocol;

public class RipRedistributionPolicy extends RedistributionPolicy {

  public static final long DEFAULT_REDISTRIBUTE_CONNECTED_METRIC = 1L;

  public static final long DEFAULT_REDISTRIBUTE_STATIC_METRIC = 1L;

  public static final long DEFAULT_REDISTRIBUTE_BGP_METRIC = 1L;

  private Long _metric;

  public RipRedistributionPolicy(RedistributionSourceProtocol sourceProtocol) {
    super(sourceProtocol, RoutingProtocol.RIP);
  }

  public Long getMetric() {
    return _metric;
  }

  public void setMetric(Long metric) {
    _metric = metric;
  }
}
