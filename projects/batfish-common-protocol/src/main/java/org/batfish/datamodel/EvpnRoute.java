package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Comparators;
import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.RouteDistinguisher;

/** An EVPN type 1 route */
@ParametersAreNonnullByDefault
public final class EvpnRoute extends BgpRoute {

  private static final long serialVersionUID = 1L;

  /** Builder for {@link EvpnRoute} */
  @ParametersAreNonnullByDefault
  public static final class Builder extends BgpRoute.Builder<EvpnRoute.Builder, EvpnRoute> {

    @Nullable private RouteDistinguisher _routeDistinguisher;

    @Nonnull
    @Override
    public Builder newBuilder() {
      return new Builder();
    }

    @Nonnull
    @Override
    public EvpnRoute build() {
      checkArgument(_originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
      checkArgument(_originType != null, "Missing %s", PROP_ORIGIN_TYPE);
      checkArgument(_protocol != null, "Missing %s", PROP_PROTOCOL);
      checkArgument(_routeDistinguisher != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
      return new EvpnRoute(
          getNetwork(),
          getNextHopIp(),
          getAdmin(),
          _asPath,
          _communities,
          _discard,
          _localPreference,
          getMetric(),
          firstNonNull(_nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE),
          _originatorIp,
          _clusterList.build(),
          _receivedFromRouteReflectorClient,
          _originType,
          _protocol,
          _receivedFromIp,
          _srcProtocol,
          _weight,
          getNonForwarding(),
          getNonRouting(),
          _routeDistinguisher);
    }

    public Builder setRouteDistinguisher(@Nonnull RouteDistinguisher routeDistinguisher) {
      _routeDistinguisher = routeDistinguisher;
      return this;
    }

    @Nullable
    public RouteDistinguisher getRouteDistinguisher() {
      return _routeDistinguisher;
    }

    @Override
    @Nonnull
    public Builder getThis() {
      return this;
    }
  }

  private static final String PROP_ROUTE_DISTINGUISHER = "routeDistinguisher";

  private static final Comparator<EvpnRoute> COMPARATOR =
      Comparator.comparing(EvpnRoute::getAsPath)
          .thenComparing(EvpnRoute::getClusterList, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(EvpnRoute::getCommunities, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(EvpnRoute::getDiscard)
          .thenComparing(EvpnRoute::getLocalPreference)
          .thenComparing(EvpnRoute::getNextHopInterface)
          .thenComparing(EvpnRoute::getOriginType)
          .thenComparing(EvpnRoute::getOriginatorIp)
          .thenComparing(EvpnRoute::getReceivedFromIp)
          .thenComparing(EvpnRoute::getReceivedFromRouteReflectorClient)
          .thenComparing(EvpnRoute::getRouteDistinguisher)
          .thenComparing(EvpnRoute::getSrcProtocol)
          .thenComparing(EvpnRoute::getWeight);

  @Nonnull private final RouteDistinguisher _routeDistinguisher;
  /* Cache the hashcode */
  private transient int _hashCode = 0;

  @JsonCreator
  private static EvpnRoute jsonCreator(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @Nullable @JsonProperty(PROP_AS_PATH) AsPath asPath,
      @Nullable @JsonProperty(PROP_COMMUNITIES) SortedSet<Long> communities,
      @JsonProperty(PROP_DISCARD) boolean discard,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long med,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_ORIGINATOR_IP) Ip originatorIp,
      @Nullable @JsonProperty(PROP_CLUSTER_LIST) SortedSet<Long> clusterList,
      @JsonProperty(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT)
          boolean receivedFromRouteReflectorClient,
      @Nullable @JsonProperty(PROP_ORIGIN_TYPE) OriginType originType,
      @Nullable @JsonProperty(PROP_PROTOCOL) RoutingProtocol protocol,
      @Nullable @JsonProperty(PROP_RECEIVED_FROM_IP) Ip receivedFromIp,
      @Nullable @JsonProperty(PROP_ROUTE_DISTINGUISHER) RouteDistinguisher routeDistinguisher,
      @Nullable @JsonProperty(PROP_SRC_PROTOCOL) RoutingProtocol srcProtocol,
      @JsonProperty(PROP_WEIGHT) int weight) {
    checkArgument(originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
    checkArgument(originType != null, "Missing %s", PROP_ORIGIN_TYPE);
    checkArgument(protocol != null, "Missing %s", PROP_PROTOCOL);
    checkArgument(routeDistinguisher != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
    return new EvpnRoute(
        network,
        nextHopIp,
        admin,
        asPath,
        communities,
        discard,
        localPreference,
        med,
        firstNonNull(nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE),
        originatorIp,
        clusterList,
        receivedFromRouteReflectorClient,
        originType,
        protocol,
        receivedFromIp,
        srcProtocol,
        weight,
        false,
        false,
        routeDistinguisher);
  }

  private EvpnRoute(
      @Nullable Prefix network,
      @Nullable Ip nextHopIp,
      int admin,
      @Nullable AsPath asPath,
      @Nullable SortedSet<Long> communities,
      boolean discard,
      long localPreference,
      long med,
      String nextHopInterface,
      Ip originatorIp,
      @Nullable SortedSet<Long> clusterList,
      boolean receivedFromRouteReflectorClient,
      OriginType originType,
      RoutingProtocol protocol,
      @Nullable Ip receivedFromIp,
      @Nullable RoutingProtocol srcProtocol,
      int weight,
      boolean nonForwarding,
      boolean nonRouting,
      RouteDistinguisher routeDistinguisher) {
    super(
        network,
        nextHopIp,
        admin,
        asPath,
        communities,
        discard,
        localPreference,
        med,
        nextHopInterface,
        originatorIp,
        clusterList,
        receivedFromRouteReflectorClient,
        originType,
        protocol,
        receivedFromIp,
        srcProtocol,
        weight,
        nonForwarding,
        nonRouting);
    _routeDistinguisher = routeDistinguisher;
  }

  @Nonnull
  public RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setAdmin(getAdministrativeCost())
        .setNonRouting(getNonRouting())
        .setNonForwarding(getNonForwarding())
        .setAsPath(_asPath)
        .setClusterList(_clusterList)
        .setCommunities(_communities)
        .setDiscard(_discard)
        .setLocalPreference(_localPreference)
        .setMetric(_med)
        .setNextHopInterface(_nextHopInterface)
        .setNextHopIp(_nextHopIp)
        .setOriginatorIp(_originatorIp)
        .setOriginType(_originType)
        .setProtocol(_protocol)
        .setReceivedFromIp(_receivedFromIp)
        .setReceivedFromRouteReflectorClient(_receivedFromRouteReflectorClient)
        .setRouteDistinguisher(_routeDistinguisher)
        .setSrcProtocol(_srcProtocol)
        .setWeight(_weight);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EvpnRoute)) {
      return false;
    }
    EvpnRoute other = (EvpnRoute) o;
    return Objects.equals(_network, other._network)
        && _admin == other._admin
        && getNonRouting() == other.getNonRouting()
        && getNonForwarding() == other.getNonForwarding()
        && _discard == other._discard
        && _localPreference == other._localPreference
        && _med == other._med
        && _receivedFromRouteReflectorClient == other._receivedFromRouteReflectorClient
        && _weight == other._weight
        && Objects.equals(_asPath, other._asPath)
        && Objects.equals(_clusterList, other._clusterList)
        && Objects.equals(_communities, other._communities)
        && _nextHopInterface.equals(other._nextHopInterface)
        && Objects.equals(_nextHopIp, other._nextHopIp)
        && Objects.equals(_originatorIp, other._originatorIp)
        && _originType == other._originType
        && _protocol == other._protocol
        && Objects.equals(_receivedFromIp, other._receivedFromIp)
        && Objects.equals(_routeDistinguisher, other._routeDistinguisher)
        && _srcProtocol == other._srcProtocol;
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _admin;
      h = h * 31 + _asPath.hashCode();
      h = h * 31 + _clusterList.hashCode();
      h = h * 31 + _communities.hashCode();
      h = h * 31 + Boolean.hashCode(_discard);
      h = h * 31 + Long.hashCode(_localPreference);
      h = h * 31 + Long.hashCode(_med);
      h = h * 31 + _network.hashCode();
      h = h * 31 + _nextHopInterface.hashCode();
      h = h * 31 + _nextHopIp.hashCode();
      h = h * 31 + _originatorIp.hashCode();
      h = h * 31 + _originType.ordinal();
      h = h * 31 + _protocol.ordinal();
      h = h * 31 + Objects.hashCode(_receivedFromIp);
      h = h * 31 + Boolean.hashCode(_receivedFromRouteReflectorClient);
      h = h * 31 + Objects.hashCode(_routeDistinguisher);
      h = h * 31 + (_srcProtocol == null ? 0 : _srcProtocol.ordinal());
      h = h * 31 + _weight;

      _hashCode = h;
    }
    return h;
  }

  @Override
  public int routeCompare(@Nonnull AbstractRoute rhs) {
    if (getClass() != rhs.getClass()) {
      return 0;
    }
    return COMPARATOR.compare(this, (EvpnRoute) rhs);
  }
}
