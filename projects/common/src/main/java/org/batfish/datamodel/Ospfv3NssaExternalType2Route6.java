package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An IPv6 OSPFv3 NSSA external type-2 route.
 *
 * <p>This represents an NSSA Type-7 external advertisement before an ABR
 * translates it into an ordinary Type-5 external advertisement.
 */
@ParametersAreNonnullByDefault
public final class Ospfv3NssaExternalType2Route6
    extends AbstractRoute6 {

  private static final String PROP_ADVERTISER =
      "advertiser";
  private static final String PROP_AREA =
      "area";
  private static final String PROP_COST_TO_ADVERTISER =
      "costToAdvertiser";

  @JsonCreator
  private static Ospfv3NssaExternalType2Route6 create(
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
        area != null,
        "Missing %s",
        PROP_AREA);

    checkArgument(
        advertiser != null,
        "Missing %s",
        PROP_ADVERTISER);

    return new Ospfv3NssaExternalType2Route6(
        network,
        firstNonNull(
            nextHopInterface,
            Route.UNSET_NEXT_HOP_INTERFACE),
        nextHopIp,
        admin,
        metric,
        area,
        firstNonNull(
            costToAdvertiser,
            0L),
        advertiser,
        firstNonNull(
            tag,
            Route.UNSET_ROUTE_TAG));
  }

  /** Create a locally originated NSSA Type-7 external route. */
  public Ospfv3NssaExternalType2Route6(
      Prefix6 network,
      String nextHopInterface,
      long admin,
      long metric,
      long area,
      Ip advertiser,
      long tag) {
    this(
        network,
        nextHopInterface,
        null,
        admin,
        metric,
        area,
        0L,
        advertiser,
        tag);
  }

  /** Create a learned NSSA Type-7 external route. */
  public Ospfv3NssaExternalType2Route6(
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
        metric >= 0L,
        "Invalid OSPFv3 NSSA external metric %s",
        metric);

    checkArgument(
        costToAdvertiser >= 0L,
        "Invalid OSPFv3 NSSA cost to advertiser %s",
        costToAdvertiser);

    _metric = metric;
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

    if (!(o
        instanceof Ospfv3NssaExternalType2Route6)) {
      return false;
    }

    Ospfv3NssaExternalType2Route6 rhs =
        (Ospfv3NssaExternalType2Route6) o;

    return _network.equals(rhs._network)
        && getAdministrativeCost()
            == rhs.getAdministrativeCost()
        && _metric == rhs._metric
        && _area == rhs._area
        && _costToAdvertiser
            == rhs._costToAdvertiser
        && _advertiser.equals(rhs._advertiser)
        && getNonRouting() == rhs.getNonRouting()
        && getNonForwarding()
            == rhs.getNonForwarding()
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
        _metric,
        _area,
        _costToAdvertiser,
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
