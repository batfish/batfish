package org.batfish.representation.cisco;

import org.batfish.datamodel.RoutingProtocol;

public class RipRedistributionPolicy extends RedistributionPolicy {

  /** */
  private static final long serialVersionUID = 1L;

  public static final Integer DEFAULT_REDISTRIBUTE_CONNECTED_METRIC = 1;

  public static final Integer DEFAULT_REDISTRIBUTE_STATIC_METRIC = 1;

  public static final Integer DEFAULT_REDISTRIBUTE_BGP_METRIC = 1;

  private Integer _metric;

  public RipRedistributionPolicy(RoutingProtocol sourceProtocol) {
    super(sourceProtocol, RoutingProtocol.RIP);
  }

  public Integer getMetric() {
    return _metric;
  }

  public void setMetric(Integer metric) {
    _metric = metric;
  }
}
