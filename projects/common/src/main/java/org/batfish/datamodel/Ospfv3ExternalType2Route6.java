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

  @JsonCreator
  private static Ospfv3ExternalType2Route6 create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix6 network,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE)
          @Nullable String nextHopInterface,
      @JsonProperty(PROP_ADMINISTRATIVE_COST)
          @Nullable Long admin,
      @JsonProperty(PROP_METRIC) @Nullable Long metric,
      @JsonProperty(PROP_ADVERTISER) @Nullable Ip advertiser,
      @JsonProperty(PROP_TAG) @Nullable Long tag) {
    checkArgument(network != null, "Missing %s", PROP_NETWORK);
    checkArgument(admin != null, "Missing %s", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "Missing %s", PROP_METRIC);
    checkArgument(advertiser != null, "Missing %s", PROP_ADVERTISER);

    return new Ospfv3ExternalType2Route6(
        network,
        firstNonNull(
            nextHopInterface,
            Route.UNSET_NEXT_HOP_INTERFACE),
        admin,
        metric,
        advertiser,
        firstNonNull(tag, Route.UNSET_ROUTE_TAG));
  }

  public Ospfv3ExternalType2Route6(
      Prefix6 network,
      String nextHopInterface,
      long admin,
      long metric,
      Ip advertiser) {
    this(
        network,
        nextHopInterface,
        admin,
        metric,
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
    super(
        network,
        admin,
        tag,
        false,
        false,
        nextHopInterface,
        null);
    _metric = metric;
    _advertiser = advertiser;
  }

  @JsonProperty(PROP_ADVERTISER)
  public @Nonnull Ip getAdvertiser() {
    return _advertiser;
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
        && _advertiser.equals(rhs._advertiser)
        && getNonRouting() == rhs.getNonRouting()
        && getNonForwarding() == rhs.getNonForwarding()
        && getNextHopInterface()
            .equals(rhs.getNextHopInterface())
        && Objects.equals(
            getNextHopIp(), rhs.getNextHopIp())
        && getTag() == rhs.getTag();
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _network,
        getAdministrativeCost(),
        getMetric(),
        _advertiser,
        getNonRouting(),
        getNonForwarding(),
        getNextHopInterface(),
        getNextHopIp(),
        getTag());
  }

  private final @Nonnull Ip _advertiser;
  private final long _metric;
}
