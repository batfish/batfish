package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.route.nh.NextHop;

/** OSPF external route of type 2 */
@ParametersAreNonnullByDefault
public class OspfExternalType2Route extends OspfExternalRoute {

  @JsonCreator
  private static OspfExternalType2Route jsonCreator(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix prefix,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) @Nullable Integer admin,
      @JsonProperty(PROP_METRIC) @Nullable Long metric,
      @JsonProperty(PROP_LSA_METRIC) @Nullable Long lsaMetric,
      @JsonProperty(PROP_AREA) @Nullable Long area,
      @JsonProperty(PROP_COST_TO_ADVERTISER) @Nullable Long costToAdvertiser,
      @JsonProperty(PROP_ADVERTISER) @Nullable String advertiser,
      @JsonProperty(PROP_TAG) long tag) {
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

  OspfExternalType2Route(
      Prefix network,
      NextHop nextHop,
      long admin,
      long metric,
      long lsaMetric,
      long area,
      long costToAdvertiser,
      String advertiser,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(
        network,
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
    return OspfExternalRoute.builder().setOspfMetricType(OspfMetricType.E2);
  }

  @Override
  public @Nonnull OspfMetricType getOspfMetricType() {
    return OspfMetricType.E2;
  }

  // toBuilder, equals and hashCode inherited from OspfExternalRoute, since it has all the fields
}
