package org.batfish.representation.cisco;

import java.io.Serializable;

public class BgpRedistributionPolicy extends RedistributionPolicy implements Serializable {

  public static final String OSPF_PROCESS_NUMBER = "OSPF_PROCESS_NUMBER";
  public static final String OSPF_ROUTE_TYPES = "OSPF_ROUTE_TYPES";

  private Long _metric;

  public BgpRedistributionPolicy(RoutingProtocolInstance instance) {
    super(instance);
  }

  public Long getMetric() {
    return _metric;
  }

  public void setMetric(long metric) {
    _metric = metric;
  }
}
