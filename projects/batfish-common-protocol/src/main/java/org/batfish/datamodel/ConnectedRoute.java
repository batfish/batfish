package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.NextHopInterface;

/**
 * Represents directly connected routes. These are typically generated based on interface
 * adjacencies.
 */
@ParametersAreNonnullByDefault
public final class ConnectedRoute extends AbstractRoute {

  @JsonCreator
  @SuppressWarnings("unused")
  private static ConnectedRoute create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable String nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int adminCost,
      @JsonProperty(PROP_TAG) long tag) {
    checkArgument(network != null, "Cannot create connected route: missing %s", PROP_NETWORK);
    return new ConnectedRoute(
        network, firstNonNull(nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE), adminCost, tag);
  }

  /** Create a connected route with admin cost of 0 */
  public ConnectedRoute(Prefix network, String nextHopInterface) {
    this(network, nextHopInterface, 0);
  }

  public ConnectedRoute(Prefix network, String nextHopInterface, long adminCost) {
    this(network, nextHopInterface, adminCost, Route.UNSET_ROUTE_TAG);
  }

  @Override
  public String toString() {
    return "ConnectedRoute{"
        + "_network="
        + _network
        + ", _admin="
        + _admin
        + ", _tag="
        + _tag
        + '}';
  }

  public ConnectedRoute(Prefix network, String nextHopInterface, long adminCost, long tag) {
    super(network, adminCost, tag, false, false);
    _nextHop = NextHopInterface.of(nextHopInterface);
  }

  @Override
  public long getMetric() {
    return 0L;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.CONNECTED;
  }

  /** Builder for {@link ConnectedRoute} */
  public static final class Builder extends AbstractRouteBuilder<Builder, ConnectedRoute> {

    @Override
    public @Nonnull ConnectedRoute build() {
      checkArgument(
          _nextHop != null && _nextHop instanceof NextHopInterface,
          "ConnectedRoute must have %s",
          PROP_NEXT_HOP_INTERFACE);
      return new ConnectedRoute(
          getNetwork(), ((NextHopInterface) _nextHop).getInterfaceName(), getAdmin(), getTag());
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setAdmin(_admin)
        .setNextHop(_nextHop)
        .setNonRouting(getNonRouting())
        .setNonForwarding(getNonForwarding())
        .setTag(_tag);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ConnectedRoute)) {
      return false;
    }
    ConnectedRoute rhs = (ConnectedRoute) o;
    return _network.equals(rhs._network)
        && _admin == rhs._admin
        && getNonRouting() == rhs.getNonRouting()
        && getNonForwarding() == rhs.getNonForwarding()
        && _nextHop.equals(rhs._nextHop)
        && _tag == rhs._tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_network, _admin, getNonRouting(), getNonForwarding(), _nextHop, _tag);
  }
}
