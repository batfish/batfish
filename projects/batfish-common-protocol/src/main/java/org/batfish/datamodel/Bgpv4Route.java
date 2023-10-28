package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.OriginMechanism.LEARNED;
import static org.batfish.datamodel.OriginMechanism.NETWORK;
import static org.batfish.datamodel.OriginMechanism.REDISTRIBUTE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/**
 * A BGP Route. Captures attributes of both iBGP and eBGP routes.
 *
 * <p>For computational efficiency may contain additional attributes (that would otherwise be
 * present only in BGP advertisements on the wire)
 */
@ParametersAreNonnullByDefault
public final class Bgpv4Route extends BgpRoute<Bgpv4Route.Builder, Bgpv4Route> {
  /** Builder for {@link Bgpv4Route} */
  public static final class Builder extends BgpRoute.Builder<Builder, Bgpv4Route> {

    @Override
    public @Nonnull Builder newBuilder() {
      return new Builder();
    }

    @Override
    public @Nonnull Bgpv4Route build() {
      checkArgument(_originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
      checkArgument(_originMechanism != null, "Missing %s", PROP_ORIGIN_MECHANISM);
      checkArgument(
          _srcProtocol != null || (_originMechanism != NETWORK && _originMechanism != REDISTRIBUTE),
          "Local routes must have a source protocol");
      checkArgument(_originType != null, "Missing %s", PROP_ORIGIN_TYPE);
      checkArgument(_protocol != null, "Missing %s", PROP_PROTOCOL);
      checkArgument(_receivedFrom != null, "Missing %s", PROP_RECEIVED_FROM);
      checkArgument(_nextHop != null, "Missing next hop");
      return new Bgpv4Route(
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
          _pathId,
          getAdmin(),
          getTag(),
          getNonForwarding(),
          getNonRouting());
    }

    @Override
    public @Nonnull Builder getThis() {
      return this;
    }

    private Builder() {}
  }

  /* Cache the hashcode */
  private transient int _hashCode = 0;

  @JsonCreator
  private static Bgpv4Route jsonCreator(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_AS_PATH) @Nullable AsPath asPath,
      @JsonProperty(PROP_COMMUNITIES) @Nullable CommunitySet communities,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long med,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_ORIGINATOR_IP) @Nullable Ip originatorIp,
      @JsonProperty(PROP_CLUSTER_LIST) @Nullable Set<Long> clusterList,
      @JsonProperty(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT)
          boolean receivedFromRouteReflectorClient,
      @JsonProperty(PROP_ORIGIN_MECHANISM) @Nullable OriginMechanism originMechanism,
      @JsonProperty(PROP_ORIGIN_TYPE) @Nullable OriginType originType,
      @JsonProperty(PROP_PATH_ID) @Nullable Integer pathId,
      @JsonProperty(PROP_PROTOCOL) @Nullable RoutingProtocol protocol,
      @JsonProperty(PROP_RECEIVED_FROM) @Nullable ReceivedFrom receivedFrom,
      @JsonProperty(PROP_SRC_PROTOCOL) @Nullable RoutingProtocol srcProtocol,
      @JsonProperty(PROP_TAG) long tag,
      @JsonProperty(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE) @Nullable
          TunnelEncapsulationAttribute tunnelEncapsulationAttribute,
      @JsonProperty(PROP_WEIGHT) int weight) {
    checkArgument(originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
    checkArgument(originMechanism != null, "Missing %s", PROP_ORIGIN_MECHANISM);
    checkArgument(
        srcProtocol != null || (originMechanism != NETWORK && originMechanism != REDISTRIBUTE),
        "Local routes must have a source protocol");
    checkArgument(originType != null, "Missing %s", PROP_ORIGIN_TYPE);
    checkArgument(protocol != null, "Missing %s", PROP_PROTOCOL);
    checkArgument(receivedFrom != null, "Missing %s", PROP_RECEIVED_FROM);
    return new Bgpv4Route(
        BgpRouteAttributes.create(
            asPath,
            clusterList,
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
        pathId,
        admin,
        tag,
        false,
        false);
  }

  private Bgpv4Route(
      BgpRouteAttributes attributes,
      @Nonnull ReceivedFrom receivedFrom,
      @Nullable Prefix network,
      @Nonnull NextHop nextHop,
      @Nullable Integer pathId,
      int admin,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(
        network, nextHop, pathId, admin, attributes, receivedFrom, tag, nonForwarding, nonRouting);
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Return a route builder with pre-filled mandatory values. To be used in tests only */
  @VisibleForTesting
  public static Builder testBuilder() {
    return builder()
        .setNextHop(NextHopDiscard.instance())
        .setOriginMechanism(LEARNED)
        .setOriginType(OriginType.IGP)
        .setOriginatorIp(Ip.parse("1.1.1.1"))
        .setAdmin(170)
        .setProtocol(RoutingProtocol.BGP)
        .setReceivedFrom(ReceivedFromSelf.instance());
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setAdmin(getAdministrativeCost())
        .setNonRouting(getNonRouting())
        .setNonForwarding(getNonForwarding())
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
        .setSrcProtocol(_attributes.getSrcProtocol())
        .setTag(_tag)
        .setTunnelEncapsulationAttribute(_attributes._tunnelEncapsulationAttribute)
        .setWeight(_attributes._weight);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Bgpv4Route)) {
      return false;
    }
    Bgpv4Route other = (Bgpv4Route) o;
    return (_hashCode == other._hashCode || _hashCode == 0 || other._hashCode == 0)
        && _network.equals(other._network)
        && _nextHop.equals(other._nextHop)
        && Objects.equals(_pathId, other._pathId)
        && _attributes.equals(other._attributes)
        && _receivedFrom.equals(other._receivedFrom)
        // Things above this line are more likely to cause false earlier.
        && _admin == other._admin
        && _tag == other._tag
        && getNonRouting() == other.getNonRouting()
        && getNonForwarding() == other.getNonForwarding();
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _admin;
      h = h * 31 + _attributes.hashCode();
      h = h * 31 + _receivedFrom.hashCode();
      h = h * 31 + _network.hashCode();
      h = h * 31 + _nextHop.hashCode();
      h = h * 31 + (_pathId != null ? _pathId : 0);
      h = h * 31 + Boolean.hashCode(getNonForwarding());
      h = h * 31 + Boolean.hashCode(getNonRouting());
      h = h * 31 + Long.hashCode(_tag);

      _hashCode = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("_network", _network)
        .add("_admin", _admin)
        .add("_tag", _tag)
        .add("_asPath", _attributes._asPath)
        .add("_clusterList", _attributes._clusterList)
        .add("_communities", _attributes._communities)
        .add("_localPreference", _attributes._localPreference)
        .add("_med", _attributes._med)
        .add("_nextHop", _nextHop)
        .add("_originatorIp", _attributes._originatorIp)
        .add("_originMechanism", _attributes._originMechanism)
        .add("_originType", _attributes._originType)
        .add("_pathId", _pathId)
        .add("_protocol", _attributes._protocol)
        .add("_receivedFrom", _receivedFrom)
        .add("_receivedFromRouteReflectorClient", _attributes._receivedFromRouteReflectorClient)
        .add("_srcProtocol", _attributes._srcProtocol)
        .add("_tunnelEncapsulationAttribute", _attributes._tunnelEncapsulationAttribute)
        .add("_weight", _attributes._weight)
        .toString();
  }
}
