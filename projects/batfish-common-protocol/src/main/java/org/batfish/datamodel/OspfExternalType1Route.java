package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OspfExternalType1Route extends OspfExternalRoute {

  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  public OspfExternalType1Route(
      @JsonProperty(PROP_NETWORK) Prefix prefix,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_COST_TO_ADVERTISER) long costToAdvertiser,
      @JsonProperty(PROP_ADVERTISER) String advertiser) {
    super(prefix, nextHopIp, admin, metric, advertiser, costToAdvertiser);
  }

  @Override
  public OspfMetricType getOspfMetricType() {
    return OspfMetricType.E1;
  }

  @Override
  protected final String ospfExternalRouteString() {
    return "";
  }

  @Override
  public int routeCompare(AbstractRoute rhs) {
    return 0;
  }
}
