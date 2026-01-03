package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
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

/** An EVPN type 3 route */
@ParametersAreNonnullByDefault
public final class EvpnType3Route extends EvpnRoute<EvpnType3Route.Builder, EvpnType3Route> {

  /** Builder for {@link EvpnType3Route} */
  @ParametersAreNonnullByDefault
  public static final class Builder extends EvpnRoute.Builder<Builder, EvpnType3Route> {

    private @Nullable Ip _vniIp;

    private Builder() {}

    @Override
    public @Nonnull Builder newBuilder() {
      return new Builder();
    }

    @Override
    public @Nonnull EvpnType3Route build() {
      checkArgument(_vniIp != null, "Missing %s", PROP_VNI_IP);
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
      return new EvpnType3Route(
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
          _nextHop,
          _routeDistinguisher,
          _vni,
          _pathId,
          getTag(),
          _vniIp);
    }

    public Builder setVniIp(@Nonnull Ip vniIp) {
      _vniIp = vniIp;
      return this;
    }

    public @Nullable Ip getVniIp() {
      return _vniIp;
    }

    @Override
    public @Nonnull Builder getThis() {
      return this;
    }
  }

  private static final String PROP_VNI_IP = "vniIp";

  private final @Nonnull Ip _vniIp;

  /* Cache the hashcode */
  private transient int _hashCode = 0;

  @JsonCreator
  private static EvpnType3Route jsonCreator(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_AS_PATH) @Nullable AsPath asPath,
      @JsonProperty(PROP_CLUSTER_LIST) @Nullable Set<Long> clusterList,
      @JsonProperty(PROP_COMMUNITIES) @Nullable CommunitySet communities,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long med,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_ORIGINATOR_IP) @Nullable Ip originatorIp,
      @JsonProperty(PROP_ORIGIN_MECHANISM) @Nullable OriginMechanism originMechanism,
      @JsonProperty(PROP_ORIGIN_TYPE) @Nullable OriginType originType,
      @JsonProperty(PROP_PATH_ID) @Nullable Integer pathId,
      @JsonProperty(PROP_PROTOCOL) @Nullable RoutingProtocol protocol,
      @JsonProperty(PROP_RECEIVED_FROM) @Nullable ReceivedFrom receivedFrom,
      @JsonProperty(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT)
          boolean receivedFromRouteReflectorClient,
      @JsonProperty(PROP_ROUTE_DISTINGUISHER) @Nullable RouteDistinguisher routeDistinguisher,
      @JsonProperty(PROP_VNI) @Nullable Integer vni,
      @JsonProperty(PROP_SRC_PROTOCOL) @Nullable RoutingProtocol srcProtocol,
      @JsonProperty(PROP_TAG) long tag,
      @JsonProperty(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE) @Nullable
          TunnelEncapsulationAttribute tunnelEncapsulationAttribute,
      @JsonProperty(PROP_VNI_IP) @Nullable Ip vniIp,
      @JsonProperty(PROP_WEIGHT) int weight) {
    checkArgument(admin == EVPN_ADMIN, "Cannot create EVPN route with non-default admin");
    checkArgument(originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
    checkArgument(originMechanism != null, "Missing %s", PROP_ORIGIN_MECHANISM);
    checkArgument(
        srcProtocol != null || (originMechanism != NETWORK && originMechanism != REDISTRIBUTE),
        "Local routes must have a source protocol");
    checkArgument(originType != null, "Missing %s", PROP_ORIGIN_TYPE);
    checkArgument(protocol != null, "Missing %s", PROP_PROTOCOL);
    checkArgument(routeDistinguisher != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    checkArgument(vniIp != null, "Missing %s", PROP_VNI_IP);
    return new EvpnType3Route(
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
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        routeDistinguisher,
        vni,
        pathId,
        tag,
        vniIp);
  }

  private EvpnType3Route(
      BgpRouteAttributes attributes,
      ReceivedFrom receivedFrom,
      NextHop nextHop,
      RouteDistinguisher routeDistinguisher,
      int vni,
      @Nullable Integer pathId,
      long tag,
      Ip vniIp) {
    super(
        vniIp.toPrefix(), nextHop, attributes, receivedFrom, tag, routeDistinguisher, vni, pathId);
    _vniIp = vniIp;
  }

  public @Nonnull Ip getVniIp() {
    return _vniIp;
  }

  public static Builder builder() {
    return new Builder();
  }

  // value of network is ignored during deserialization
  @JsonProperty(PROP_NETWORK)
  private void setNetwork(@Nullable Prefix ignoredNetwork) {}

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
        .setVniIp(_vniIp)
        .setWeight(_attributes._weight);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EvpnType3Route)) {
      return false;
    }
    EvpnType3Route other = (EvpnType3Route) o;
    return (_hashCode == other._hashCode || _hashCode == 0 || other._hashCode == 0)
        && _network.equals(other._network)
        && _attributes.equals(other._attributes)
        && _receivedFrom.equals(other._receivedFrom)
        && Objects.equals(_nextHop, other._nextHop)
        && Objects.equals(_pathId, other._pathId)
        && Objects.equals(_routeDistinguisher, other._routeDistinguisher)
        && _vni == other._vni
        && _tag == other._tag
        && Objects.equals(_vniIp, other._vniIp);
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
      h = h * 31 + _vniIp.hashCode();

      _hashCode = h;
    }
    return h;
  }
}
