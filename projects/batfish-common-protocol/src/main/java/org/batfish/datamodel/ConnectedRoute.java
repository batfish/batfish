package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents directly connected routes. These are typically generated based on interface
 * adjacencies.
 */
@ParametersAreNonnullByDefault
public final class ConnectedRoute extends AbstractRoute {

  @Nonnull private final String _nextHopInterface;

  @JsonCreator
  private static ConnectedRoute create(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
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

  public ConnectedRoute(Prefix network, String nextHopInterface, int adminCost) {
    this(network, nextHopInterface, adminCost, Route.UNSET_ROUTE_TAG);
  }

  public ConnectedRoute(Prefix network, String nextHopInterface, int adminCost, long tag) {
    super(network, adminCost, tag, false, false);
    _nextHopInterface = nextHopInterface;
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
        && _nextHopInterface.equals(rhs._nextHopInterface)
        && _tag == rhs._tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _network, _admin, getNonRouting(), getNonForwarding(), _nextHopInterface, _tag);
  }

  @Override
  public Long getMetric() {
    return 0L;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_INTERFACE)
  @Override
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  @Nonnull
  @Override
  public Ip getNextHopIp() {
    return Route.UNSET_ROUTE_NEXT_HOP_IP;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.CONNECTED;
  }

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setAdmin(_admin)
        .setNextHopInterface(_nextHopInterface)
        .setTag(_tag)
        .setNonRouting(getNonRouting())
        .setNonForwarding(getNonForwarding());
  }

  /** Builder for {@link ConnectedRoute} */
  public static final class Builder extends AbstractRouteBuilder<Builder, ConnectedRoute> {
    @Nullable private String _nextHopInterface;

    @Nonnull
    @Override
    public ConnectedRoute build() {
      checkArgument(
          _nextHopInterface != null, "ConnectedRoute must have %s", PROP_NEXT_HOP_INTERFACE);
      return new ConnectedRoute(getNetwork(), _nextHopInterface, getAdmin());
    }

    @Nonnull
    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setNextHopInterface(String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
      return getThis();
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
