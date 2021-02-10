package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;

/** OSPF external route of type 1 */
@ParametersAreNonnullByDefault
public class OspfExternalType1Route extends OspfExternalRoute {

  @JsonCreator
  private static OspfExternalType1Route jsonCreator(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix prefix,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_ADMINISTRATIVE_COST) Integer admin,
      @Nullable @JsonProperty(PROP_METRIC) Long metric,
      @Nullable @JsonProperty(PROP_LSA_METRIC) Long lsaMetric,
      @Nullable @JsonProperty(PROP_AREA) Long area,
      @Nullable @JsonProperty(PROP_COST_TO_ADVERTISER) Long costToAdvertiser,
      @Nullable @JsonProperty(PROP_ADVERTISER) String advertiser,
      @JsonProperty(PROP_TAG) long tag) {
    checkArgument(prefix != null, "Missing %s", PROP_NETWORK);
    checkArgument(nextHopIp != null, "Missing %s", PROP_NEXT_HOP_IP);
    checkArgument(admin != null, "Missing %s", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "Missing %s", PROP_METRIC);
    checkArgument(lsaMetric != null, "Missing %s", PROP_LSA_METRIC);
    checkArgument(area != null, "Missing %s", PROP_AREA);
    checkArgument(costToAdvertiser != null, "Missing %s", PROP_COST_TO_ADVERTISER);
    checkArgument(advertiser != null, "Missing %s", PROP_ADVERTISER);
    return new OspfExternalType1Route(
        prefix,
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        admin,
        metric,
        lsaMetric,
        area,
        costToAdvertiser,
        advertiser,
        tag,
        false,
        false);
  }

  OspfExternalType1Route(
      Prefix prefix,
      NextHop nextHop,
      int admin,
      long metric,
      long lsaMetric,
      long area,
      long costToAdvertiser,
      String advertiser,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(
        prefix,
        nextHop,
        admin,
        metric,
        lsaMetric,
        area,
        advertiser,
        costToAdvertiser,
        tag,
        nonForwarding,
        nonRouting);
  }

  public static OspfExternalRoute.Builder builder() {
    return OspfExternalRoute.builder().setOspfMetricType(OspfMetricType.E1);
  }

  /** Return a route builder with pre-filled mandatory values. To be used in tests only */
  @VisibleForTesting
  public static OspfExternalRoute.Builder testBuilder() {
    return builder()
        .setAdvertiser("adv")
        .setArea(0L)
        .setCostToAdvertiser(1L)
        .setLsaMetric(2L)
        .setNextHop(NextHopDiscard.instance());
  }

  @Nonnull
  @Override
  public OspfMetricType getOspfMetricType() {
    return OspfMetricType.E1;
  }

  // toBuilder, equals and hashcode inherited from OspfExternalRoute, since it has all the fields
}
