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
import org.batfish.datamodel.bgp.community.Community;

/** An EVPN type 3 route */
@ParametersAreNonnullByDefault
public final class EvpnType3Route extends BgpRoute {

  private static final long serialVersionUID = 1L;

  /** Builder for {@link EvpnType3Route} */
  @ParametersAreNonnullByDefault
  public static final class Builder extends BgpRoute.Builder<Builder, EvpnType3Route> {

    @Nullable private Ip _vniIp;
    @Nullable private RouteDistinguisher _routeDistinguisher;

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
      checkArgument(_originType != null, "Missing %s", PROP_ORIGIN_TYPE);
      checkArgument(_protocol != null, "Missing %s", PROP_PROTOCOL);
      checkArgument(_routeDistinguisher != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
      return new EvpnType3Route(
          getAdmin(),
          _asPath,
          _clusterList.build(),
          _communities,
          _discard,
          _localPreference,
          getMetric(),
          firstNonNull(_nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE),
          getNextHopIp(),
          getNonForwarding(),
          getNonRouting(),
          _originatorIp,
          _originType,
          _protocol,
          _receivedFromIp,
          _receivedFromRouteReflectorClient,
          _routeDistinguisher,
          _srcProtocol,
          _vniIp,
          _weight);
    }

    public Builder setRouteDistinguisher(@Nonnull RouteDistinguisher routeDistinguisher) {
      _routeDistinguisher = routeDistinguisher;
      return this;
    }

    public Builder setVniIp(@Nonnull Ip vniIp) {
      _vniIp = vniIp;
      return this;
    }

    @Nullable
    public Ip getVniIp() {
      return _vniIp;
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

  private static final String PROP_VNI_IP = "vniIp";
  private static final String PROP_ROUTE_DISTINGUISHER = "routeDistinguisher";

  private static final Comparator<EvpnType3Route> COMPARATOR =
      Comparator.comparing(EvpnType3Route::getAsPath)
          .thenComparing(
              EvpnType3Route::getClusterList, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(
              EvpnType3Route::getCommunities, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(EvpnType3Route::getDiscard)
          .thenComparing(EvpnType3Route::getLocalPreference)
          .thenComparing(EvpnType3Route::getNextHopInterface)
          .thenComparing(EvpnType3Route::getOriginType)
          .thenComparing(EvpnType3Route::getOriginatorIp)
          .thenComparing(EvpnType3Route::getReceivedFromIp)
          .thenComparing(EvpnType3Route::getReceivedFromRouteReflectorClient)
          .thenComparing(EvpnType3Route::getRouteDistinguisher)
          .thenComparing(EvpnType3Route::getSrcProtocol)
          .thenComparing(EvpnType3Route::getVniIp)
          .thenComparing(EvpnType3Route::getWeight);

  @Nonnull private final Ip _vniIp;
  @Nonnull private final RouteDistinguisher _routeDistinguisher;
  /* Cache the hashcode */
  private transient int _hashCode = 0;

  @SuppressWarnings("unused")
  @JsonCreator
  private static EvpnType3Route jsonCreator(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @Nullable @JsonProperty(PROP_AS_PATH) AsPath asPath,
      @Nullable @JsonProperty(PROP_CLUSTER_LIST) SortedSet<Long> clusterList,
      @Nullable @JsonProperty(PROP_COMMUNITIES) SortedSet<Community> communities,
      @JsonProperty(PROP_DISCARD) boolean discard,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long med,
      // value of network is ignored while calling class constructor
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_ORIGINATOR_IP) Ip originatorIp,
      @Nullable @JsonProperty(PROP_ORIGIN_TYPE) OriginType originType,
      @Nullable @JsonProperty(PROP_PROTOCOL) RoutingProtocol protocol,
      @Nullable @JsonProperty(PROP_RECEIVED_FROM_IP) Ip receivedFromIp,
      @JsonProperty(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT)
          boolean receivedFromRouteReflectorClient,
      @Nullable @JsonProperty(PROP_ROUTE_DISTINGUISHER) RouteDistinguisher routeDistinguisher,
      @Nullable @JsonProperty(PROP_SRC_PROTOCOL) RoutingProtocol srcProtocol,
      @Nullable @JsonProperty(PROP_VNI_IP) Ip vniIp,
      @JsonProperty(PROP_WEIGHT) int weight) {
    checkArgument(originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
    checkArgument(originType != null, "Missing %s", PROP_ORIGIN_TYPE);
    checkArgument(protocol != null, "Missing %s", PROP_PROTOCOL);
    checkArgument(routeDistinguisher != null, "Missing %s", PROP_ROUTE_DISTINGUISHER);
    checkArgument(vniIp != null, "Missing %s", PROP_VNI_IP);
    return new EvpnType3Route(
        admin,
        asPath,
        clusterList,
        communities,
        discard,
        localPreference,
        med,
        firstNonNull(nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE),
        nextHopIp,
        false,
        false,
        originatorIp,
        originType,
        protocol,
        receivedFromIp,
        receivedFromRouteReflectorClient,
        routeDistinguisher,
        srcProtocol,
        vniIp,
        weight);
  }

  private EvpnType3Route(
      int admin,
      @Nullable AsPath asPath,
      @Nullable SortedSet<Long> clusterList,
      @Nullable SortedSet<Community> communities,
      boolean discard,
      long localPreference,
      long med,
      String nextHopInterface,
      @Nullable Ip nextHopIp,
      boolean nonForwarding,
      boolean nonRouting,
      Ip originatorIp,
      OriginType originType,
      RoutingProtocol protocol,
      @Nullable Ip receivedFromIp,
      boolean receivedFromRouteReflectorClient,
      RouteDistinguisher routeDistinguisher,
      @Nullable RoutingProtocol srcProtocol,
      Ip vniIp,
      int weight) {
    super(
        Prefix.create(vniIp, Prefix.MAX_PREFIX_LENGTH),
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
    _vniIp = vniIp;
  }

  @Nonnull
  public RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  @Nonnull
  public Ip getVniIp() {
    return _vniIp;
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
        .setVniIp(_vniIp)
        .setWeight(_weight);
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
        && _srcProtocol == other._srcProtocol
        && Objects.equals(_vniIp, other._vniIp);
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
      h = h * 31 + _routeDistinguisher.hashCode();
      h = h * 31 + (_srcProtocol == null ? 0 : _srcProtocol.ordinal());
      h = h * 31 + _vniIp.hashCode();
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
    return COMPARATOR.compare(this, (EvpnType3Route) rhs);
  }
}
