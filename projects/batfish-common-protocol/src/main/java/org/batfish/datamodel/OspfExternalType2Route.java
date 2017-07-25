package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OspfExternalType2Route extends OspfExternalRoute {

  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  public OspfExternalType2Route(
      @JsonProperty(NETWORK_VAR) Prefix network,
      @JsonProperty(NEXT_HOP_IP_VAR) Ip nextHopIp,
      @JsonProperty(ADMINISTRATIVE_COST_VAR) int admin,
      @JsonProperty(METRIC_VAR) int metric,
      @JsonProperty(COST_TO_ADVERTISER_VAR) int costToAdvertiser,
      @JsonProperty(ADVERTISER_VAR) String advertiser) {
    super(network, nextHopIp, admin, metric, advertiser, costToAdvertiser);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    OspfExternalType2Route other = (OspfExternalType2Route) obj;
    if (getCostToAdvertiser() != other.getCostToAdvertiser()) {
      return false;
    }
    return true;
  }

  @Override
  public OspfMetricType getOspfMetricType() {
    return OspfMetricType.E2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + getCostToAdvertiser();
    return result;
  }

  @Override
  protected final String ospfExternalRouteString() {
    return " costToAdvertiser:" + getCostToAdvertiser();
  }

  @Override
  public int routeCompare(AbstractRoute rhs) {
    if (getClass() != rhs.getClass()) {
      return 0;
    }
    OspfExternalType2Route castRhs = (OspfExternalType2Route) rhs;
    int ret = Integer.compare(getCostToAdvertiser(), castRhs.getCostToAdvertiser());
    return ret;
  }
}
