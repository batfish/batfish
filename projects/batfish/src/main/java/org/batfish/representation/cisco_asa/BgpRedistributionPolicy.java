package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import org.batfish.datamodel.RoutingProtocol;

public class BgpRedistributionPolicy extends RedistributionPolicy implements Serializable {

  public static final String OSPF_PROCESS_NUMBER = "OSPF_PROCESS_NUMBER";
  public static final String OSPF_ROUTE_TYPES = "OSPF_ROUTE_TYPES";

  private Long _metric;

  public BgpRedistributionPolicy(RoutingProtocol sourceProtocol) {
    super(sourceProtocol);
  }

  public Long getMetric() {
    return _metric;
  }

  public void setMetric(long metric) {
    _metric = metric;
  }
}
