package org.batfish.vendor.arista.representation;

import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfMetricType;

public class OspfRedistributionPolicy extends RedistributionPolicy {

  public static final String BGP_AS = "BGP_AS";

  public static final OspfMetricType DEFAULT_METRIC_TYPE = OspfMetricType.E2;

  private Long _metric;

  private OspfMetricType _metricType;

  private Long _tag;

  public OspfRedistributionPolicy(RedistributionSourceProtocol sourceProtocol) {
    super(sourceProtocol, RoutingProtocol.OSPF);
  }

  public Long getMetric() {
    return _metric;
  }

  public OspfMetricType getMetricType() {
    return _metricType;
  }

  public Long getTag() {
    return _tag;
  }

  public void setMetric(long metric) {
    _metric = metric;
  }

  public void setOspfMetricType(OspfMetricType type) {
    _metricType = type;
  }

  public void setTag(long tag) {
    _tag = tag;
  }
}
