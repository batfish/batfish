package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.OriginMechanism.NETWORK;
import static org.batfish.datamodel.OriginMechanism.REDISTRIBUTE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/** An EVPN type 5 route */
@ParametersAreNonnullByDefault
public final class EvpnType5Route extends EvpnRoute<EvpnType5Route.Builder, EvpnType5Route> {

  /** Builder for {@link EvpnType5Route} */
  @ParametersAreNonnullByDefault
  public static final class Builder extends EvpnRoute.Builder<Builder, EvpnType5Route> {

    @Nonnull
    @Override
    public Builder newBuilder() {
      return new Builder();
    }

    private Builder() {}

    @Nonnull
    @Override
    public EvpnType5Route build() {
      checkArgument(getNetwork() != null, "Missing %s", PROP_NETWORK);
      checkArgument(_originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
      checkArgument(_originMechanism != null, "Missing %s", PROP_ORIGIN_MECHANISM);
      checkArgument(
          _srcProtocol != null || (_originMechanism != NETWORK && _originMechanism != REDISTRIBUTE),
          "Local routes must have a source protocol");
      checkArgument(_originType != null, "Missing %s", PROP_ORIGIN_TYPE);
      checkArgument(_protocol != null, "Missing %s", PROP_PROTOCOL);
      checkArgument(_routeDistinguisher != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
      checkArgument(_nextHop != null, "Missing next hop");
      return new EvpnType5Route(
          _asPath,
          _clusterList,
          _communities,
          _localPreference,
          getMetric(),
          getNetwork(),
          _nextHop,
          _originatorIp,
          _originMechanism,
          _originType,
          _protocol,
          _receivedFromIp,
          _receivedFromRouteReflectorClient,
          _routeDistinguisher,
          _srcProtocol,
          getTag(),
          _weight);
    }

    @Override
    @Nonnull
    public Builder getThis() {
      return this;
    }
  }

  /* Cache the hashcode */
  private transient int _hashCode = 0;

  @JsonCreator
  private static EvpnType5Route jsonCreator(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @Nullable @JsonProperty(PROP_AS_PATH) AsPath asPath,
      @Nullable @JsonProperty(PROP_CLUSTER_LIST) Set<Long> clusterList,
      @Nullable @JsonProperty(PROP_COMMUNITIES) CommunitySet communities,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long med,
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_ORIGINATOR_IP) Ip originatorIp,
      @Nullable @JsonProperty(PROP_ORIGIN_MECHANISM) OriginMechanism originMechanism,
      @Nullable @JsonProperty(PROP_ORIGIN_TYPE) OriginType originType,
      @Nullable @JsonProperty(PROP_PROTOCOL) RoutingProtocol protocol,
      @Nullable @JsonProperty(PROP_RECEIVED_FROM_IP) Ip receivedFromIp,
      @JsonProperty(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT)
          boolean receivedFromRouteReflectorClient,
      @Nullable @JsonProperty(PROP_ROUTE_DISTINGUISHER) RouteDistinguisher routeDistinguisher,
      @Nullable @JsonProperty(PROP_SRC_PROTOCOL) RoutingProtocol srcProtocol,
      @JsonProperty(PROP_TAG) long tag,
      @JsonProperty(PROP_WEIGHT) int weight) {
    checkArgument(admin == EVPN_ADMIN, "Cannot create EVPN route with non-default admin");
    checkArgument(network != null, "Missing %s", PROP_NETWORK);
    checkArgument(originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
    checkArgument(originMechanism != null, "Missing %s", PROP_ORIGIN_MECHANISM);
    checkArgument(
        srcProtocol != null || (originMechanism != NETWORK && originMechanism != REDISTRIBUTE),
        "Local routes must have a source protocol");
    checkArgument(originType != null, "Missing %s", PROP_ORIGIN_TYPE);
    checkArgument(protocol != null, "Missing %s", PROP_PROTOCOL);
    checkArgument(routeDistinguisher != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
    return new EvpnType5Route(
        firstNonNull(asPath, AsPath.empty()),
        firstNonNull(clusterList, ImmutableSet.of()),
        firstNonNull(communities, CommunitySet.empty()),
        localPreference,
        med,
        network,
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        originatorIp,
        originMechanism,
        originType,
        protocol,
        receivedFromIp,
        receivedFromRouteReflectorClient,
        routeDistinguisher,
        srcProtocol,
        tag,
        weight);
  }

  private EvpnType5Route(
      AsPath asPath,
      Set<Long> clusterList,
      CommunitySet communities,
      long localPreference,
      long med,
      Prefix network,
      NextHop nextHop,
      Ip originatorIp,
      OriginMechanism originMechanism,
      OriginType originType,
      RoutingProtocol protocol,
      @Nullable Ip receivedFromIp,
      boolean receivedFromRouteReflectorClient,
      RouteDistinguisher routeDistinguisher,
      @Nullable RoutingProtocol srcProtocol,
      long tag,
      int weight) {
    super(
        network,
        nextHop,
        asPath,
        communities,
        localPreference,
        med,
        originatorIp,
        clusterList,
        receivedFromRouteReflectorClient,
        originMechanism,
        originType,
        protocol,
        receivedFromIp,
        srcProtocol,
        tag,
        weight,
        routeDistinguisher);
  }

  public static Builder builder() {
    return new Builder();
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setAsPath(_asPath)
        .setClusterList(_clusterList)
        .setCommunities(_communities)
        .setLocalPreference(_localPreference)
        .setMetric(_med)
        .setNextHop(_nextHop)
        .setOriginatorIp(_originatorIp)
        .setOriginMechanism(_originMechanism)
        .setOriginType(_originType)
        .setProtocol(_protocol)
        .setReceivedFromIp(_receivedFromIp)
        .setReceivedFromRouteReflectorClient(_receivedFromRouteReflectorClient)
        .setRouteDistinguisher(_routeDistinguisher)
        .setSrcProtocol(_srcProtocol)
        .setTag(_tag)
        .setWeight(_weight);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EvpnType5Route)) {
      return false;
    }
    EvpnType5Route other = (EvpnType5Route) o;
    return (_hashCode == other._hashCode || _hashCode == 0 || other._hashCode == 0)
        && _network.equals(other._network)
        && _localPreference == other._localPreference
        && _med == other._med
        && _receivedFromRouteReflectorClient == other._receivedFromRouteReflectorClient
        && _weight == other._weight
        && Objects.equals(_asPath, other._asPath)
        && Objects.equals(_clusterList, other._clusterList)
        && Objects.equals(_communities, other._communities)
        && Objects.equals(_nextHop, other._nextHop)
        && Objects.equals(_originatorIp, other._originatorIp)
        && _originMechanism == other._originMechanism
        && _originType == other._originType
        && _protocol == other._protocol
        && Objects.equals(_receivedFromIp, other._receivedFromIp)
        && Objects.equals(_routeDistinguisher, other._routeDistinguisher)
        && _srcProtocol == other._srcProtocol
        && _tag == other._tag;
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _asPath.hashCode();
      h = h * 31 + _clusterList.hashCode();
      h = h * 31 + _communities.hashCode();
      h = h * 31 + Long.hashCode(_localPreference);
      h = h * 31 + Long.hashCode(_med);
      h = h * 31 + _network.hashCode();
      h = h * 31 + _nextHop.hashCode();
      h = h * 31 + _originatorIp.hashCode();
      h = h * 31 + _originMechanism.ordinal();
      h = h * 31 + _originType.ordinal();
      h = h * 31 + _protocol.ordinal();
      h = h * 31 + Objects.hashCode(_receivedFromIp);
      h = h * 31 + Boolean.hashCode(_receivedFromRouteReflectorClient);
      h = h * 31 + _routeDistinguisher.hashCode();
      h = h * 31 + (_srcProtocol == null ? 0 : _srcProtocol.ordinal());
      h = h * 31 + Long.hashCode(_tag);
      h = h * 31 + _weight;

      _hashCode = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return toStringHelper(EvpnType5Route.class)
        .omitNullValues()
        .add(PROP_NETWORK, _network)
        .add(PROP_ROUTE_DISTINGUISHER, _routeDistinguisher)
        .add("nextHop", _nextHop)
        .add(PROP_AS_PATH, _asPath)
        .add(PROP_CLUSTER_LIST, _clusterList)
        .add(PROP_COMMUNITIES, _communities)
        .add(PROP_LOCAL_PREFERENCE, _localPreference)
        .add(PROP_METRIC, _med)
        .add(PROP_ORIGINATOR_IP, _originatorIp)
        .add(PROP_ORIGIN_MECHANISM, _originMechanism)
        .add(PROP_ORIGIN_TYPE, _originType)
        .add(PROP_PROTOCOL, _protocol)
        .add(PROP_RECEIVED_FROM_IP, _receivedFromIp)
        .add(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT, _receivedFromRouteReflectorClient)
        .add(PROP_SRC_PROTOCOL, _srcProtocol)
        .add(PROP_TAG, _tag)
        .add(PROP_WEIGHT, _weight)
        .toString();
  }
}
