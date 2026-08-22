package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a directly connected IPv6 route. */
@ParametersAreNonnullByDefault
public final class ConnectedRoute6 extends AbstractRoute6 {

  @JsonCreator
  private static ConnectedRoute6 create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix6 network,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) long adminCost,
      @JsonProperty(PROP_TAG) long tag) {
    checkArgument(
        network != null,
        "Cannot create IPv6 connected route: missing %s",
        PROP_NETWORK);

    return new ConnectedRoute6(
        network,
        firstNonNull(
            nextHopInterface,
            Route.UNSET_NEXT_HOP_INTERFACE),
        adminCost,
        tag);
  }

  /** Create an IPv6 connected route with administrative cost 0. */
  public ConnectedRoute6(
      Prefix6 network, String nextHopInterface) {
    this(network, nextHopInterface, 0);
  }

  public ConnectedRoute6(
      Prefix6 network,
      String nextHopInterface,
      long adminCost) {
    this(
        network,
        nextHopInterface,
        adminCost,
        Route.UNSET_ROUTE_TAG);
  }

  public ConnectedRoute6(
      Prefix6 network,
      String nextHopInterface,
      long adminCost,
      long tag) {
    super(
        network,
        adminCost,
        tag,
        false,
        false,
        nextHopInterface,
        null);
  }

  @Override
  public long getMetric() {
    return 0L;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.CONNECTED;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConnectedRoute6)) {
      return false;
    }

    ConnectedRoute6 rhs = (ConnectedRoute6) o;
    return _network.equals(rhs._network)
        && getAdministrativeCost()
            == rhs.getAdministrativeCost()
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
        getNonRouting(),
        getNonForwarding(),
        getNextHopInterface(),
        getNextHopIp(),
        getTag());
  }
}
