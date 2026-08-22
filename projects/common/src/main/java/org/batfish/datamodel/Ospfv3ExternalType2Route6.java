package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An IPv6 OSPFv3 external type-2 route. */
@ParametersAreNonnullByDefault
public final class Ospfv3ExternalType2Route6
    extends AbstractRoute6 {

  private static final String PROP_ADVERTISER = "advertiser";
  private static final String PROP_AREA = "area";
  private static final String PROP_COST_TO_ADVERTISER =
      "costToAdvertiser";

  @JsonCreator
  private static Ospfv3ExternalType2Route6 create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix6 network,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE)
          @Nullable String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP)
          @Nullable Ip6 nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST)
          @Nullable Long admin,
      @JsonProperty(PROP_METRIC)
          @Nullable Long metric,
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
        advertiser != null,
        "Missing %s",
        PROP_ADVERTISER);

    return new Ospfv3ExternalType2Route6(
        network,
        firstNonNull(
            nextHopInterface,
            Route.UNSET_NEXT_HOP_INTERFACE),
        nextHopIp,
        admin,
        metric,
        firstNonNull(area, 0L),
        firstNonNull(costToAdvertiser, 0L),
        advertiser,
        firstNonNull(
            tag,
            Route.UNSET_ROUTE_TAG));
  }

  /**
   * Create a locally originated E2 route.
   *
   * <p>The local ASBR has cost-to-advertiser 0.
   */
  public Ospfv3ExternalType2Route6(
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
        0L,
        0L,
        advertiser,
        Route.UNSET_ROUTE_TAG);
  }

  public Ospfv3ExternalType2Route6(
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
        0L,
        0L,
        advertiser,
        tag);
  }

  /** Create a learned E2 route. */
  public Ospfv3ExternalType2Route6(
      Prefix6 network,
      String nextHopInterface,
      @Nullable Ip6 nextHopIp,
      long admin,
      long metric,
      long area,
      long costToAdvertiser,
      Ip advertiser) {
    this(
        network,
        nextHopInterface,
        nextHopIp,
        admin,
        metric,
        area,
        costToAdvertiser,
        advertiser,
        Route.UNSET_ROUTE_TAG);
  }

  public Ospfv3ExternalType2Route6(
      Prefix6 network,
      String nextHopInterface,
      @Nullable Ip6 nextHopIp,
      long admin,
      long metric,
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
        metric >= 0,
        "Invalid OSPFv3 external metric %s",
        metric);
    checkArgument(
        costToAdvertiser >= 0,
        "Invalid OSPFv3 cost to advertiser %s",
        costToAdvertiser);

    _metric = metric;
    _area = area;
    _costToAdvertiser = costToAdvertiser;
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
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Ospfv3ExternalType2Route6)) {
      return false;
    }

    Ospfv3ExternalType2Route6 rhs =
        (Ospfv3ExternalType2Route6) o;

    return _network.equals(rhs._network)
        && getAdministrativeCost()
            == rhs.getAdministrativeCost()
        && getMetric() == rhs.getMetric()
        && getArea() == rhs.getArea()
        && getCostToAdvertiser()
            == rhs.getCostToAdvertiser()
        && _advertiser.equals(rhs._advertiser)
        && getNonRouting() == rhs.getNonRouting()
        && getNonForwarding() == rhs.getNonForwarding()
        && getNextHopInterface()
            .equals(rhs.getNextHopInterface())
        && Objects.equals(
            getNextHopIp(),
            rhs.getNextHopIp())
        && getTag() == rhs.getTag();
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _network,
        getAdministrativeCost(),
        getMetric(),
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
  private final long _metric;
}
