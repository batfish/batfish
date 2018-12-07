package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.OspfMetricType;

public class OspfExternalType1Route extends OspfExternalRoute {

  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static OspfExternalType1Route jsonCreator(
      @JsonProperty(PROP_NETWORK) Prefix prefix,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_LSA_METRIC) long lsaMetric,
      @JsonProperty(PROP_AREA) long area,
      @JsonProperty(PROP_COST_TO_ADVERTISER) long costToAdvertiser,
      @JsonProperty(PROP_ADVERTISER) String advertiser) {
    return new OspfExternalType1Route(
        prefix,
        nextHopIp,
        admin,
        metric,
        lsaMetric,
        area,
        costToAdvertiser,
        advertiser,
        false,
        false);
  }

  OspfExternalType1Route(
      Prefix prefix,
      Ip nextHopIp,
      int admin,
      long metric,
      long lsaMetric,
      long area,
      long costToAdvertiser,
      String advertiser,
      boolean nonForwarding,
      boolean nonRouting) {
    super(
        prefix,
        nextHopIp,
        admin,
        metric,
        lsaMetric,
        area,
        advertiser,
        costToAdvertiser,
        nonForwarding,
        nonRouting);
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
  public int routeCompare(@Nonnull AbstractRoute rhs) {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OspfExternalType1Route)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    OspfExternalType1Route other = (OspfExternalType1Route) obj;
    return getCostToAdvertiser() == other.getCostToAdvertiser();
  }
}
