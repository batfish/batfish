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
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/** An EVPN type 3 route */
@ParametersAreNonnullByDefault
public final class EvpnType3Route extends EvpnRoute<EvpnType3Route.Builder, EvpnType3Route> {

  /** Builder for {@link EvpnType3Route} */
  @ParametersAreNonnullByDefault
  public static final class Builder extends EvpnRoute.Builder<Builder, EvpnType3Route> {

    @Nullable private Ip _vniIp;

    private Builder() {}

    @Nonnull
    @Override
    public Builder newBuilder() {
      return new Builder();
    }

    @Nonnull
    @Override
    public EvpnType3Route build() {
      checkArgument(_vniIp != null, "Missing %s", PROP_VNI_IP);
      checkArgument(_originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
      checkArgument(_originMechanism != null, "Missing %s", PROP_ORIGIN_MECHANISM);
      checkArgument(
          _srcProtocol != null || (_originMechanism != NETWORK && _originMechanism != REDISTRIBUTE),
          "Local routes must have a source protocol");

      checkArgument(_originType != null, "Missing %s", PROP_ORIGIN_TYPE);
      checkArgument(_protocol != null, "Missing %s", PROP_PROTOCOL);
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
              _weight),
          _receivedFromIp,
          _nextHop,
          _routeDistinguisher,
          _vni,
          getTag(),
          _vniIp);
    }

    public Builder setVniIp(@Nonnull Ip vniIp) {
      _vniIp = vniIp;
      return this;
    }

    @Nullable
    public Ip getVniIp() {
      return _vniIp;
    }

    @Override
    @Nonnull
    public Builder getThis() {
      return this;
    }
  }

  private static final String PROP_VNI_IP = "vniIp";

  @Nonnull private final Ip _vniIp;

  /* Cache the hashcode */
  private transient int _hashCode = 0;

  @JsonCreator
  private static EvpnType3Route jsonCreator(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @Nullable @JsonProperty(PROP_AS_PATH) AsPath asPath,
      @Nullable @JsonProperty(PROP_CLUSTER_LIST) Set<Long> clusterList,
      @Nullable @JsonProperty(PROP_COMMUNITIES) CommunitySet communities,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long med,
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
      @Nullable @JsonProperty(PROP_VNI) Integer vni,
      @Nullable @JsonProperty(PROP_SRC_PROTOCOL) RoutingProtocol srcProtocol,
      @JsonProperty(PROP_TAG) long tag,
      @Nullable @JsonProperty(PROP_VNI_IP) Ip vniIp,
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
            weight),
        receivedFromIp,
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        routeDistinguisher,
        vni,
        tag,
        vniIp);
  }

  private EvpnType3Route(
      BgpRouteAttributes attributes,
      @Nullable Ip receivedFromIp,
      NextHop nextHop,
      RouteDistinguisher routeDistinguisher,
      int vni,
      long tag,
      Ip vniIp) {
    super(vniIp.toPrefix(), nextHop, attributes, receivedFromIp, tag, routeDistinguisher, vni);
    _vniIp = vniIp;
  }

  @Nonnull
  public Ip getVniIp() {
    return _vniIp;
  }

  public static Builder builder() {
    return new Builder();
  }

  // value of network is ignored during deserialization
  @JsonProperty(PROP_NETWORK)
  private void setNetwork(@Nullable Prefix network) {}

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
        .setOriginMechanism(_attributes._originMechanism)
        .setOriginType(_attributes._originType)
        .setProtocol(_attributes._protocol)
        .setReceivedFromIp(_receivedFromIp)
        .setReceivedFromRouteReflectorClient(_attributes._receivedFromRouteReflectorClient)
        .setRouteDistinguisher(_routeDistinguisher)
        .setSrcProtocol(_attributes._srcProtocol)
        .setTag(_tag)
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
        && Objects.equals(_receivedFromIp, other._receivedFromIp)
        && Objects.equals(_nextHop, other._nextHop)
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
      h = h * 31 + (_receivedFromIp == null ? 0 : _receivedFromIp.hashCode());
      h = h * 31 + _network.hashCode();
      h = h * 31 + _nextHop.hashCode();
      h = h * 31 + _routeDistinguisher.hashCode();
      h = h * 31 + Integer.hashCode(_vni);
      h = h * 31 + Long.hashCode(_tag);
      h = h * 31 + _vniIp.hashCode();

      _hashCode = h;
    }
    return h;
  }
}
