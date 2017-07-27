package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OspfExternalType1Route extends OspfExternalRoute {

  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  public OspfExternalType1Route(
      @JsonProperty(NETWORK_VAR) Prefix prefix,
      @JsonProperty(NEXT_HOP_IP_VAR) Ip nextHopIp,
      @JsonProperty(ADMINISTRATIVE_COST_VAR) int admin,
      @JsonProperty(METRIC_VAR) int metric,
      @JsonProperty(COST_TO_ADVERTISER_VAR) int costToAdvertiser,
      @JsonProperty(ADVERTISER_VAR) String advertiser) {
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
