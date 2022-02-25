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

    @Nonnull
    @Override
    public Builder newBuilder() {
      return new Builder();
    }

    @Nonnull
    @Override
    public Bgpv4Route build() {
      checkArgument(_originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
      checkArgument(_originMechanism != null, "Missing %s", PROP_ORIGIN_MECHANISM);
      checkArgument(
          _srcProtocol != null || (_originMechanism != NETWORK && _originMechanism != REDISTRIBUTE),
          "Local routes must have a source protocol");
      checkArgument(_originType != null, "Missing %s", PROP_ORIGIN_TYPE);
      checkArgument(_protocol != null, "Missing %s", PROP_PROTOCOL);
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
              _weight),
          _receivedFromIp,
          getNetwork(),
          _nextHop,
          getAdmin(),
          getTag(),
          getNonForwarding(),
          getNonRouting());
    }

    @Override
    @Nonnull
    public Builder getThis() {
      return this;
    }

    private Builder() {}
  }

  /* Cache the hashcode */
  private transient int _hashCode = 0;

  @JsonCreator
  private static Bgpv4Route jsonCreator(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @Nullable @JsonProperty(PROP_AS_PATH) AsPath asPath,
      @Nullable @JsonProperty(PROP_COMMUNITIES) CommunitySet communities,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long med,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_ORIGINATOR_IP) Ip originatorIp,
      @Nullable @JsonProperty(PROP_CLUSTER_LIST) Set<Long> clusterList,
      @JsonProperty(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT)
          boolean receivedFromRouteReflectorClient,
      @Nullable @JsonProperty(PROP_ORIGIN_MECHANISM) OriginMechanism originMechanism,
      @Nullable @JsonProperty(PROP_ORIGIN_TYPE) OriginType originType,
      @Nullable @JsonProperty(PROP_PROTOCOL) RoutingProtocol protocol,
      @Nullable @JsonProperty(PROP_RECEIVED_FROM_IP) Ip receivedFromIp,
      @Nullable @JsonProperty(PROP_SRC_PROTOCOL) RoutingProtocol srcProtocol,
      @JsonProperty(PROP_TAG) long tag,
      @JsonProperty(PROP_WEIGHT) int weight) {
    checkArgument(originatorIp != null, "Missing %s", PROP_ORIGINATOR_IP);
    checkArgument(originMechanism != null, "Missing %s", PROP_ORIGIN_MECHANISM);
    checkArgument(
        srcProtocol != null || (originMechanism != NETWORK && originMechanism != REDISTRIBUTE),
        "Local routes must have a source protocol");
    checkArgument(originType != null, "Missing %s", PROP_ORIGIN_TYPE);
    checkArgument(protocol != null, "Missing %s", PROP_PROTOCOL);
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
            weight),
        receivedFromIp,
        network,
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        admin,
        tag,
        false,
        false);
  }

  private Bgpv4Route(
      BgpRouteAttributes attributes,
      @Nullable Ip receivedFromIp,
      @Nullable Prefix network,
      @Nonnull NextHop nextHop,
      int admin,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, nextHop, admin, attributes, receivedFromIp, tag, nonForwarding, nonRouting);
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
        .setProtocol(RoutingProtocol.BGP);
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
        .setOriginMechanism(_attributes._originMechanism)
        .setOriginType(_attributes._originType)
        .setProtocol(_attributes._protocol)
        .setReceivedFromIp(_receivedFromIp)
        .setReceivedFromRouteReflectorClient(_attributes._receivedFromRouteReflectorClient)
        .setSrcProtocol(_attributes._srcProtocol)
        .setTag(_tag)
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
        && _attributes.equals(other._attributes)
        && Objects.equals(_receivedFromIp, other._receivedFromIp)
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
      h = h * 31 + (_receivedFromIp == null ? 0 : _receivedFromIp.hashCode());
      h = h * 31 + _network.hashCode();
      h = h * 31 + _nextHop.hashCode();
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
        .add("_protocol", _attributes._protocol)
        .add("_receivedFromIp", _receivedFromIp)
        .add("_receivedFromRouteReflectorClient", _attributes._receivedFromRouteReflectorClient)
        .add("_srcProtocol", _attributes._srcProtocol)
        .add("_weight", _attributes._weight)
        .toString();
  }
}
