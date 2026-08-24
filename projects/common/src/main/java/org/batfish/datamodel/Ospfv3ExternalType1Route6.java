package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An IPv6 OSPFv3 external type-1 route. */
@ParametersAreNonnullByDefault
public final class Ospfv3ExternalType1Route6
    extends AbstractRoute6 {

  private static final String PROP_ADVERTISER =
      "advertiser";

  private static final String PROP_AREA =
      "area";

  private static final String PROP_COST_TO_ADVERTISER =
      "costToAdvertiser";

  private static final String PROP_LSA_METRIC =
      "lsaMetric";

  @JsonCreator
  private static Ospfv3ExternalType1Route6 create(
      @JsonProperty(PROP_NETWORK)
          @Nullable Prefix6 network,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE)
          @Nullable String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP)
          @Nullable Ip6 nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST)
          @Nullable Long admin,
      @JsonProperty(PROP_METRIC)
          @Nullable Long metric,
      @JsonProperty(PROP_LSA_METRIC)
          @Nullable Long lsaMetric,
      @JsonProperty(PROP_AREA)
          @Nullable Long area,
      @JsonProperty(PROP_COST_TO_ADVERTISER)
          @Nullable Long costToAdvertiser,
      @JsonProperty(PROP_ADVERTISER)
          @Nullable Ip advertiser,
      @JsonProperty(PROP_TAG)
          @Nullable Long tag) {

    checkArgument(
        network != null,
        "Missing %s",
        PROP_NETWORK);

    checkArgument(
        admin != null,
        "Missing %s",
        PROP_ADMINISTRATIVE_COST);

    checkArgument(
        metric != null,
        "Missing %s",
        PROP_METRIC);

    checkArgument(
        lsaMetric != null,
        "Missing %s",
        PROP_LSA_METRIC);

    checkArgument(
        advertiser != null,
        "Missing %s",
        PROP_ADVERTISER);

    return new Ospfv3ExternalType1Route6(
        network,
        firstNonNull(
            nextHopInterface,
            Route.UNSET_NEXT_HOP_INTERFACE),
        nextHopIp,
        admin,
        metric,
        lsaMetric,
        firstNonNull(
            area,
            0L),
        firstNonNull(
            costToAdvertiser,
            0L),
        advertiser,
        firstNonNull(
            tag,
            Route.UNSET_ROUTE_TAG));
  }

  /** Create a locally originated E1 route. */
  public Ospfv3ExternalType1Route6(
      Prefix6 network,
      String nextHopInterface,
      long admin,
      long metric,
      Ip advertiser) {

    this(
        network,
        nextHopInterface,
        null,
        admin,
        metric,
        metric,
        0L,
        0L,
        advertiser,
        Route.UNSET_ROUTE_TAG);
  }

  /** Create a locally originated tagged E1 route. */
  public Ospfv3ExternalType1Route6(
      Prefix6 network,
      String nextHopInterface,
      long admin,
      long metric,
      Ip advertiser,
      long tag) {

    this(
        network,
        nextHopInterface,
        null,
        admin,
        metric,
        metric,
        0L,
        0L,
        advertiser,
        tag);
  }

  /** Create a learned E1 route. */
  public Ospfv3ExternalType1Route6(
      Prefix6 network,
      String nextHopInterface,
      @Nullable Ip6 nextHopIp,
      long admin,
      long metric,
      long lsaMetric,
      long area,
      long costToAdvertiser,
      Ip advertiser,
      long tag) {

    super(
        network,
        admin,
        tag,
        false,
        false,
        nextHopInterface,
        nextHopIp);

    checkArgument(
        metric >= 0L,
        "Invalid OSPFv3 E1 metric %s",
        metric);

    checkArgument(
        lsaMetric >= 0L,
        "Invalid OSPFv3 E1 LSA metric %s",
        lsaMetric);

    checkArgument(
        costToAdvertiser >= 0L,
        "Invalid OSPFv3 E1 cost to advertiser %s",
        costToAdvertiser);

    checkArgument(
        metric >= lsaMetric,
        "OSPFv3 E1 metric %s is below LSA metric %s",
        metric,
        lsaMetric);

    checkArgument(
        metric - lsaMetric
            == costToAdvertiser,
        "OSPFv3 E1 metric %s does not equal LSA metric %s plus cost %s",
        metric,
        lsaMetric,
        costToAdvertiser);

    _metric = metric;
    _lsaMetric = lsaMetric;
    _area = area;
    _costToAdvertiser =
        costToAdvertiser;
    _advertiser = advertiser;
  }

  @JsonProperty(PROP_ADVERTISER)
  public @Nonnull Ip getAdvertiser() {
    return _advertiser;
  }

  @JsonProperty(PROP_AREA)
  public long getArea() {
    return _area;
  }

  @JsonProperty(PROP_COST_TO_ADVERTISER)
  public long getCostToAdvertiser() {
    return _costToAdvertiser;
  }

  @JsonProperty(PROP_LSA_METRIC)
  public long getLsaMetric() {
    return _lsaMetric;
  }

  @Override
  @JsonProperty(PROP_METRIC)
  public long getMetric() {
    return _metric;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.OSPF3;
  }

  @Override
  public boolean equals(
      @Nullable Object o) {

    if (this == o) {
      return true;
    }

    if (!(o
        instanceof Ospfv3ExternalType1Route6)) {
      return false;
    }

    Ospfv3ExternalType1Route6 rhs =
        (Ospfv3ExternalType1Route6) o;

    return _network.equals(rhs._network)
        && getAdministrativeCost()
            == rhs.getAdministrativeCost()
        && getMetric()
            == rhs.getMetric()
        && getLsaMetric()
            == rhs.getLsaMetric()
        && getArea()
            == rhs.getArea()
        && getCostToAdvertiser()
            == rhs.getCostToAdvertiser()
        && _advertiser.equals(
            rhs._advertiser)
        && getNonRouting()
            == rhs.getNonRouting()
        && getNonForwarding()
            == rhs.getNonForwarding()
        && getNextHopInterface()
            .equals(
                rhs.getNextHopInterface())
        && Objects.equals(
            getNextHopIp(),
            rhs.getNextHopIp())
        && getTag()
            == rhs.getTag();
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _network,
        getAdministrativeCost(),
        getMetric(),
        getLsaMetric(),
        getArea(),
        getCostToAdvertiser(),
        _advertiser,
        getNonRouting(),
        getNonForwarding(),
        getNextHopInterface(),
        getNextHopIp(),
        getTag());
  }

  private final @Nonnull Ip _advertiser;
  private final long _area;
  private final long _costToAdvertiser;
  private final long _lsaMetric;
  private final long _metric;
}
