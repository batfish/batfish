package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An IPv6 OSPFv3 intra-area route. */
@ParametersAreNonnullByDefault
public final class Ospfv3IntraAreaRoute6 extends AbstractRoute6 {

  private static final String PROP_AREA = "area";

  @JsonCreator
  private static Ospfv3IntraAreaRoute6 create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix6 network,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE)
          @Nullable String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip6 nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST)
          @Nullable Long admin,
      @JsonProperty(PROP_METRIC) @Nullable Long metric,
      @JsonProperty(PROP_AREA) @Nullable Long area,
      @JsonProperty(PROP_TAG) @Nullable Long tag) {
    checkArgument(network != null, "Missing %s", PROP_NETWORK);
    checkArgument(admin != null, "Missing %s", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "Missing %s", PROP_METRIC);
    checkArgument(area != null, "Missing %s", PROP_AREA);

    return new Ospfv3IntraAreaRoute6(
        network,
        firstNonNull(
            nextHopInterface,
            Route.UNSET_NEXT_HOP_INTERFACE),
        nextHopIp,
        admin,
        metric,
        area,
        firstNonNull(tag, Route.UNSET_ROUTE_TAG));
  }

  public Ospfv3IntraAreaRoute6(
      Prefix6 network,
      String nextHopInterface,
      @Nullable Ip6 nextHopIp,
      long admin,
      long metric,
      long area) {
    this(
        network,
        nextHopInterface,
        nextHopIp,
        admin,
        metric,
        area,
        Route.UNSET_ROUTE_TAG);
  }

  public Ospfv3IntraAreaRoute6(
      Prefix6 network,
      String nextHopInterface,
      @Nullable Ip6 nextHopIp,
      long admin,
      long metric,
      long area,
      long tag) {
    super(
        network,
        admin,
        tag,
        false,
        false,
        nextHopInterface,
        nextHopIp);
    _metric = metric;
    _area = area;
  }

  @JsonProperty(PROP_AREA)
  public long getArea() {
    return _area;
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
    if (!(o instanceof Ospfv3IntraAreaRoute6)) {
      return false;
    }

    Ospfv3IntraAreaRoute6 rhs =
        (Ospfv3IntraAreaRoute6) o;

    return _network.equals(rhs._network)
        && getAdministrativeCost()
            == rhs.getAdministrativeCost()
        && getMetric() == rhs.getMetric()
        && getArea() == rhs.getArea()
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
        getArea(),
        getNonRouting(),
        getNonForwarding(),
        getNextHopInterface(),
        getNextHopIp(),
        getTag());
  }

  private final long _area;
  private final long _metric;
}
