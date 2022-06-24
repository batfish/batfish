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
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
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
      checkArgument(_receivedFrom != null, "Missing %s", PROP_RECEIVED_FROM);
      checkArgument(_routeDistinguisher != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
      checkArgument(_vni != null, "Missing %s", PROP_VNI);
      checkArgument(_nextHop != null, "Missing next hop");
      return new EvpnType5Route(
          BgpRouteAttributes.create(
              _asPath,
              _clusterList,
              _communities,
              _localPreference,
              getMetric(),
              _originatorIp,
              _originMechanism,
              _originType,
              _protocol,
              _receivedFromRouteReflectorClient,
              _srcProtocol,
              _tunnelEncapsulationAttribute,
              _weight),
          _receivedFrom,
          getNetwork(),
          _nextHop,
          _routeDistinguisher,
          _vni,
          _pathId,
          getTag());
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
      @Nullable @JsonProperty(PROP_PATH_ID) Integer pathId,
      @Nullable @JsonProperty(PROP_PROTOCOL) RoutingProtocol protocol,
      @Nullable @JsonProperty(PROP_RECEIVED_FROM) ReceivedFrom receivedFrom,
      @JsonProperty(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT)
          boolean receivedFromRouteReflectorClient,
      @Nullable @JsonProperty(PROP_ROUTE_DISTINGUISHER) RouteDistinguisher routeDistinguisher,
      @Nullable @JsonProperty(PROP_VNI) Integer vni,
      @Nullable @JsonProperty(PROP_SRC_PROTOCOL) RoutingProtocol srcProtocol,
      @JsonProperty(PROP_TAG) long tag,
      @Nullable @JsonProperty(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE)
          TunnelEncapsulationAttribute tunnelEncapsulationAttribute,
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
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    return new EvpnType5Route(
        BgpRouteAttributes.create(
            firstNonNull(asPath, AsPath.empty()),
            firstNonNull(clusterList, ImmutableSet.of()),
            firstNonNull(communities, CommunitySet.empty()),
            localPreference,
            med,
            originatorIp,
            originMechanism,
            originType,
            protocol,
            receivedFromRouteReflectorClient,
            srcProtocol,
            tunnelEncapsulationAttribute,
            weight),
        receivedFrom,
        network,
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        routeDistinguisher,
        vni,
        pathId,
        tag);
  }

  private EvpnType5Route(
      BgpRouteAttributes attributes,
      ReceivedFrom receivedFrom,
      Prefix network,
      NextHop nextHop,
      RouteDistinguisher routeDistinguisher,
      int vni,
      @Nullable Integer pathId,
      long tag) {
    super(network, nextHop, attributes, receivedFrom, tag, routeDistinguisher, vni, pathId);
  }

  public static Builder builder() {
    return new Builder();
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setAsPath(_attributes._asPath)
        .setClusterList(_attributes._clusterList)
        .setCommunities(_attributes._communities)
        .setLocalPreference(_attributes._localPreference)
        .setMetric(_attributes._med)
        .setNextHop(_nextHop)
        .setOriginatorIp(_attributes._originatorIp)
        .setOriginMechanism(_attributes.getOriginMechanism())
        .setOriginType(_attributes.getOriginType())
        .setPathId(_pathId)
        .setProtocol(_attributes.getProtocol())
        .setReceivedFrom(_receivedFrom)
        .setReceivedFromRouteReflectorClient(_attributes._receivedFromRouteReflectorClient)
        .setRouteDistinguisher(_routeDistinguisher)
        .setSrcProtocol(_attributes.getSrcProtocol())
        .setTag(_tag)
        .setTunnelEncapsulationAttribute(_attributes._tunnelEncapsulationAttribute)
        .setVni(_vni)
        .setWeight(_attributes._weight);
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
        && _attributes.equals(other._attributes)
        && _receivedFrom.equals(other._receivedFrom)
        && Objects.equals(_nextHop, other._nextHop)
        && Objects.equals(_pathId, other._pathId)
        && Objects.equals(_routeDistinguisher, other._routeDistinguisher)
        && _vni == other._vni
        && _tag == other._tag;
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _attributes.hashCode();
      h = h * 31 + _receivedFrom.hashCode();
      h = h * 31 + _network.hashCode();
      h = h * 31 + _nextHop.hashCode();
      h = h * 31 + Objects.hashCode(_pathId);
      h = h * 31 + _routeDistinguisher.hashCode();
      h = h * 31 + Integer.hashCode(_vni);
      h = h * 31 + Long.hashCode(_tag);

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
        .add(PROP_VNI, _vni)
        .add("nextHop", _nextHop)
        .add(PROP_AS_PATH, _attributes._asPath)
        .add(PROP_CLUSTER_LIST, _attributes._clusterList)
        .add(PROP_COMMUNITIES, _attributes._communities)
        .add(PROP_LOCAL_PREFERENCE, _attributes._localPreference)
        .add(PROP_METRIC, _attributes._med)
        .add(PROP_ORIGINATOR_IP, _attributes._originatorIp)
        .add(PROP_ORIGIN_MECHANISM, _attributes._originMechanism)
        .add(PROP_ORIGIN_TYPE, _attributes._originType)
        .add(PROP_PATH_ID, _pathId)
        .add(PROP_PROTOCOL, _attributes._protocol)
        .add(PROP_RECEIVED_FROM, _receivedFrom)
        .add(
            PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT,
            _attributes._receivedFromRouteReflectorClient)
        .add(PROP_SRC_PROTOCOL, _attributes._srcProtocol)
        .add(PROP_TAG, _tag)
        .add(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE, _attributes._tunnelEncapsulationAttribute)
        .add(PROP_WEIGHT, _attributes._weight)
        .toString();
  }
}
