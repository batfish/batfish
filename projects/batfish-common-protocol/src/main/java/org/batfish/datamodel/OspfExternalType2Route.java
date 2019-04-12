package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ospf.OspfMetricType;

/** OSPF external route of type 2 */
@ParametersAreNonnullByDefault
public class OspfExternalType2Route extends OspfExternalRoute {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static OspfExternalType2Route jsonCreator(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix prefix,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_ADMINISTRATIVE_COST) Integer admin,
      @Nullable @JsonProperty(PROP_METRIC) Long metric,
      @Nullable @JsonProperty(PROP_LSA_METRIC) Long lsaMetric,
      @Nullable @JsonProperty(PROP_AREA) Long area,
      @Nullable @JsonProperty(PROP_COST_TO_ADVERTISER) Long costToAdvertiser,
      @Nullable @JsonProperty(PROP_ADVERTISER) String advertiser) {
    checkArgument(prefix != null, "Missing %s", PROP_NETWORK);
    checkArgument(nextHopIp != null, "Missing %s", PROP_NEXT_HOP_IP);
    checkArgument(admin != null, "Missing %s", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "Missing %s", PROP_METRIC);
    checkArgument(lsaMetric != null, "Missing %s", PROP_LSA_METRIC);
    checkArgument(area != null, "Missing %s", PROP_AREA);
    checkArgument(costToAdvertiser != null, "Missing %s", PROP_COST_TO_ADVERTISER);
    checkArgument(advertiser != null, "Missing %s", PROP_ADVERTISER);
    return new OspfExternalType2Route(
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

  OspfExternalType2Route(
      Prefix network,
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
        network,
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

  public static OspfExternalRoute.Builder builder() {
    return OspfExternalRoute.builder().setOspfMetricType(OspfMetricType.E2);
  }

  @Nonnull
  @Override
  public OspfMetricType getOspfMetricType() {
    return OspfMetricType.E2;
  }

  @Override
  public int routeCompare(AbstractRoute rhs) {
    if (getClass() != rhs.getClass()) {
      return 0;
    }
    OspfExternalType2Route castRhs = (OspfExternalType2Route) rhs;
    return Long.compare(getCostToAdvertiser(), castRhs.getCostToAdvertiser());
  }

  @Override
  public OspfExternalRoute.Builder toBuilder() {
    return OspfExternalRoute.builder()
        // AbstractRoute properties
        .setNetwork(getNetwork())
        .setNextHopIp(getNextHopIp())
        .setAdmin(getAdministrativeCost())
        .setMetric(getMetric())
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        // OspfExternalType2Route properties
        .setOspfMetricType(getOspfMetricType())
        .setLsaMetric(getLsaMetric())
        .setArea(getArea())
        .setCostToAdvertiser(getCostToAdvertiser())
        .setAdvertiser(getAdvertiser());
  }

  // Equals and hashcode inherited from OspfExternalRoute, since it has all the fields
}
