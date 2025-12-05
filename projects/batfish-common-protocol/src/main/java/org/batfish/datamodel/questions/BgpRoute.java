package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.OriginMechanism.LEARNED;
import static org.batfish.datamodel.Route.UNSET_ROUTE_NEXT_HOP_IP;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.NextHopConcrete;
import org.batfish.datamodel.answers.NextHopResult;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.route.nh.LegacyNextHops;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;

/** A user facing representation for IPv4 BGP route */
@ParametersAreNonnullByDefault
public final class BgpRoute {

  public static final long DEFAULT_AD = 0;
  public static final String PROP_ADMINISTRATIVE_DISTANCE = "adminDistance";
  public static final String PROP_AS_PATH = "asPath";
  public static final String PROP_CLUSTER_LIST = "clusterList";
  public static final String PROP_COMMUNITIES = "communities";
  public static final String PROP_LOCAL_PREFERENCE = "localPreference";
  public static final String PROP_METRIC = "metric";
  public static final String PROP_NETWORK = "network";
  public static final String PROP_NEXT_HOP_IP = "nextHopIp";
  public static final String PROP_NEXT_HOP = "nextHop";
  public static final String PROP_ORIGINATOR_IP = "originatorIp";
  public static final String PROP_ORIGIN_MECHANISM = "originMechanism";
  public static final String PROP_ORIGIN_TYPE = "originType";
  public static final String PROP_PATH_ID = "pathId";
  public static final String PROP_PROTOCOL = "protocol";
  public static final String PROP_SRC_PROTOCOL = "srcProtocol";
  public static final String PROP_TAG = "tag";
  public static final String PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE = "tunnelEncapsulationAttribute";
  public static final String PROP_WEIGHT = "weight";
  public static final String PROP_CLASS = "class";

  private final @Nullable Long _adminDist;
  private final @Nonnull AsPath _asPath;
  private final @Nonnull Set<Long> _clusterList;
  private final @Nonnull SortedSet<Community> _communities;
  private final long _localPreference;
  private final long _metric;
  private final @Nonnull Prefix _network;
  private final @Nonnull NextHopResult _nextHop;
  private final @Nonnull Ip _originatorIp;
  private final @Nonnull OriginMechanism _originMechanism;
  private final @Nonnull OriginType _originType;
  private final @Nullable Integer _pathId;
  private final @Nonnull RoutingProtocol _protocol;
  private final @Nullable RoutingProtocol _srcProtocol;
  private final long _tag;
  private final @Nullable TunnelEncapsulationAttribute _tunnelEncapsulationAttribute;
  private final int _weight;

  private BgpRoute(
      @Nullable Long adminDist,
      AsPath asPath,
      Set<Long> clusterList,
      SortedSet<Community> communities,
      long localPreference,
      long metric,
      Prefix network,
      NextHopResult nextHop,
      Ip originatorIp,
      OriginMechanism originMechanism,
      OriginType originType,
      @Nullable Integer pathId,
      RoutingProtocol protocol,
      @Nullable RoutingProtocol srcProtocol,
      long tag,
      @Nullable TunnelEncapsulationAttribute tunnelEncapsulationAttribute,
      int weight) {
    _adminDist = adminDist;
    _asPath = asPath;
    _clusterList = clusterList;
    _communities = communities;
    _localPreference = localPreference;
    _metric = metric;
    _network = network;
    _nextHop = nextHop;
    _originatorIp = originatorIp;
    _originMechanism = originMechanism;
    _originType = originType;
    _pathId = pathId;
    _protocol = protocol;
    _srcProtocol = srcProtocol;
    _tag = tag;
    _tunnelEncapsulationAttribute = tunnelEncapsulationAttribute;
    _weight = weight;
  }

  @JsonCreator
  private static BgpRoute jsonCreator(
      @JsonProperty(PROP_ADMINISTRATIVE_DISTANCE) @Nullable Integer adminDist,
      @JsonProperty(PROP_AS_PATH) @Nullable AsPath asPath,
      @JsonProperty(PROP_CLUSTER_LIST) @Nullable Set<Long> clusterList,
      @JsonProperty(PROP_COMMUNITIES) @Nullable SortedSet<Community> communities,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP) @Nullable NextHopResult nextHop,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_ORIGINATOR_IP) @Nullable Ip originatorIp,
      @JsonProperty(PROP_ORIGIN_MECHANISM) @Nullable OriginMechanism originMechanism,
      @JsonProperty(PROP_ORIGIN_TYPE) @Nullable OriginType originType,
      @JsonProperty(PROP_PATH_ID) @Nullable Integer pathId,
      @JsonProperty(PROP_PROTOCOL) @Nullable RoutingProtocol protocol,
      @JsonProperty(PROP_SRC_PROTOCOL) @Nullable RoutingProtocol srcProtocol,
      @JsonProperty(PROP_TAG) long tag,
      @JsonProperty(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE) @Nullable
          TunnelEncapsulationAttribute tunnelEncapsulationAttribute,
      @JsonProperty(PROP_WEIGHT) int weight,
      // For backwards compatibility, does nothing
      @JsonProperty(PROP_CLASS) @Nullable String ignoredClazz) {
    checkArgument(network != null, "%s must be specified", PROP_NETWORK);
    checkArgument(originatorIp != null, "%s must be specified", PROP_ORIGINATOR_IP);
    checkArgument(originType != null, "%s must be specified", PROP_ORIGIN_TYPE);
    checkArgument(protocol != null, "%s must be specified", PROP_PROTOCOL);
    Long effectiveAdminDist = adminDist != null ? adminDist : DEFAULT_AD;
    return new BgpRoute(
        effectiveAdminDist,
        firstNonNull(asPath, AsPath.empty()),
        firstNonNull(clusterList, ImmutableSet.of()),
        firstNonNull(communities, ImmutableSortedSet.of()),
        localPreference,
        metric,
        network,
        firstNonNull(nextHop, ipToNextHopResult(nextHopIp)),
        originatorIp,
        firstNonNull(originMechanism, LEARNED),
        originType,
        pathId,
        protocol,
        srcProtocol,
        tag,
        tunnelEncapsulationAttribute,
        weight);
  }

  private static NextHopResult ipToNextHopResult(@Nullable Ip nextHopIp) {
    NextHop nh;
    if (nextHopIp == null || nextHopIp.equals(UNSET_ROUTE_NEXT_HOP_IP)) {
      nh = NextHopDiscard.instance();
    } else {
      nh = NextHopIp.of(nextHopIp);
    }
    return new NextHopConcrete(nh);
  }

  @JsonProperty(PROP_ADMINISTRATIVE_DISTANCE)
  public @Nullable Long getAdminDist() {
    return _adminDist;
  }

  @JsonProperty(PROP_AS_PATH)
  public @Nonnull AsPath getAsPath() {
    return _asPath;
  }

  @JsonProperty(PROP_CLUSTER_LIST)
  public @Nonnull Set<Long> getClusterList() {
    return _clusterList;
  }

  @JsonProperty(PROP_COMMUNITIES)
  public @Nonnull SortedSet<Community> getCommunities() {
    return _communities;
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public long getLocalPreference() {
    return _localPreference;
  }

  @JsonProperty(PROP_METRIC)
  public long getMetric() {
    return _metric;
  }

  @JsonProperty(PROP_NETWORK)
  public @Nonnull Prefix getNetwork() {
    return _network;
  }

  @JsonProperty(PROP_NEXT_HOP)
  public @Nonnull NextHopResult getNextHop() {
    return _nextHop;
  }

  @JsonProperty(PROP_NEXT_HOP_IP)
  private @Nullable Ip getNextHopIpJson() {
    if (_nextHop instanceof NextHopConcrete) {
      return LegacyNextHops.getNextHopIp(((NextHopConcrete) _nextHop).getNextHop()).orElse(null);
    } else {
      return null;
    }
  }

  @JsonProperty(PROP_ORIGINATOR_IP)
  public @Nonnull Ip getOriginatorIp() {
    return _originatorIp;
  }

  @JsonProperty(PROP_ORIGIN_MECHANISM)
  public @Nonnull OriginMechanism getOriginMechanism() {
    return _originMechanism;
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  public @Nonnull OriginType getOriginType() {
    return _originType;
  }

  @JsonProperty(PROP_PATH_ID)
  public @Nullable Integer getPathId() {
    return _pathId;
  }

  @JsonProperty(PROP_PROTOCOL)
  public @Nonnull RoutingProtocol getProtocol() {
    return _protocol;
  }

  @JsonProperty(PROP_SRC_PROTOCOL)
  public @Nullable RoutingProtocol getSrcProtocol() {
    return _srcProtocol;
  }

  @JsonProperty(PROP_TAG)
  public long getTag() {
    return _tag;
  }

  @JsonProperty(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE)
  public @Nullable TunnelEncapsulationAttribute getTunnelEncapsulationAttribute() {
    return _tunnelEncapsulationAttribute;
  }

  @JsonProperty(PROP_WEIGHT)
  public int getWeight() {
    return _weight;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpRoute)) {
      return false;
    }
    BgpRoute bgpRoute = (BgpRoute) o;
    return _localPreference == bgpRoute._localPreference
        && _metric == bgpRoute._metric
        && _tag == bgpRoute._tag
        && _weight == bgpRoute._weight
        && Objects.equals(_adminDist, bgpRoute._adminDist)
        && Objects.equals(_asPath, bgpRoute._asPath)
        && Objects.equals(_clusterList, bgpRoute._clusterList)
        && Objects.equals(_communities, bgpRoute._communities)
        && Objects.equals(_network, bgpRoute._network)
        && Objects.equals(_nextHop, bgpRoute._nextHop)
        && Objects.equals(_originatorIp, bgpRoute._originatorIp)
        && _originMechanism == bgpRoute._originMechanism
        && _originType == bgpRoute._originType
        && Objects.equals(_pathId, bgpRoute._pathId)
        && _protocol == bgpRoute._protocol
        && _srcProtocol == bgpRoute._srcProtocol
        && Objects.equals(_tunnelEncapsulationAttribute, bgpRoute._tunnelEncapsulationAttribute);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _adminDist,
        _asPath,
        _clusterList,
        _communities,
        _localPreference,
        _metric,
        _network,
        _nextHop,
        _originatorIp,
        _originMechanism.ordinal(),
        _originType.ordinal(),
        _pathId,
        _protocol.ordinal(),
        _srcProtocol != null ? _srcProtocol.ordinal() : null,
        _tag,
        _tunnelEncapsulationAttribute,
        _weight);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return builder()
        .setAdminDist(_adminDist)
        .setAsPath(_asPath)
        .setClusterList(_clusterList)
        .setCommunities(_communities)
        .setLocalPreference(_localPreference)
        .setMetric(_metric)
        .setNetwork(_network)
        .setNextHop(_nextHop)
        .setOriginatorIp(_originatorIp)
        .setOriginMechanism(_originMechanism)
        .setOriginType(_originType)
        .setPathId(_pathId)
        .setProtocol(_protocol)
        .setSrcProtocol(_srcProtocol)
        .setTag(_tag)
        .setTunnelEncapsulationAttribute(_tunnelEncapsulationAttribute)
        .setWeight(_weight);
  }

  /** Builder for {@link BgpRoute} */
  @ParametersAreNonnullByDefault
  public static final class Builder {

    private @Nullable Long _adminDist;
    private @Nonnull AsPath _asPath;
    private @Nonnull Set<Long> _clusterList;
    private @Nonnull SortedSet<Community> _communities;
    private long _localPreference;
    private long _metric;
    private @Nullable Prefix _network;
    private @Nullable NextHopResult _nextHop;
    private @Nullable Ip _originatorIp;
    private @Nullable OriginMechanism _originMechanism;
    private @Nullable OriginType _originType;
    private @Nullable Integer _pathId;
    private @Nullable RoutingProtocol _protocol;
    private @Nullable RoutingProtocol _srcProtocol;
    private long _tag;
    private @Nullable TunnelEncapsulationAttribute _tunnelEncapsulationAttribute;
    private int _weight;

    public Builder() {
      _asPath = AsPath.empty();
      _clusterList = ImmutableSet.of();
      _communities = ImmutableSortedSet.of();
    }

    public BgpRoute build() {
      checkArgument(_network != null, "%s must be specified", PROP_NETWORK);
      checkArgument(_originatorIp != null, "%s must be specified", PROP_ORIGINATOR_IP);
      checkArgument(_originType != null, "%s must be specified", PROP_ORIGIN_TYPE);
      checkArgument(_protocol != null, "%s must be specified", PROP_PROTOCOL);
      return new BgpRoute(
          firstNonNull(_adminDist, DEFAULT_AD),
          _asPath,
          _clusterList,
          _communities,
          _localPreference,
          _metric,
          _network,
          firstNonNull(_nextHop, new NextHopConcrete(NextHopDiscard.instance())),
          _originatorIp,
          firstNonNull(_originMechanism, LEARNED),
          _originType,
          _pathId,
          _protocol,
          _srcProtocol,
          _tag,
          _tunnelEncapsulationAttribute,
          _weight);
    }

    public Builder setAdminDist(@Nullable Long adminDist) {
      _adminDist = adminDist;
      return this;
    }

    public Builder setAsPath(AsPath asPath) {
      _asPath = asPath;
      return this;
    }

    public Builder setClusterList(Set<Long> clusterList) {
      _clusterList = ImmutableSet.copyOf(clusterList);
      return this;
    }

    public Builder setCommunities(Set<Community> communities) {
      _communities = ImmutableSortedSet.copyOf(communities);
      return this;
    }

    public Builder setLocalPreference(long localPreference) {
      _localPreference = localPreference;
      return this;
    }

    public Builder setMetric(long metric) {
      _metric = metric;
      return this;
    }

    public Builder setNetwork(Prefix network) {
      _network = network;
      return this;
    }

    public Builder setNextHop(@Nullable NextHopResult nextHop) {
      _nextHop = nextHop;
      return this;
    }

    public Builder setNextHopIp(Ip nextHopIp) {
      _nextHop = ipToNextHopResult(nextHopIp);
      return this;
    }

    public Builder setNextHopConcrete(NextHop nh) {
      _nextHop = new NextHopConcrete(nh);
      return this;
    }

    public Builder setOriginatorIp(Ip originatorIp) {
      _originatorIp = originatorIp;
      return this;
    }

    public Builder setOriginMechanism(OriginMechanism originMechanism) {
      _originMechanism = originMechanism;
      return this;
    }

    public Builder setOriginType(OriginType originType) {
      _originType = originType;
      return this;
    }

    public Builder setPathId(@Nullable Integer pathId) {
      _pathId = pathId;
      return this;
    }

    public Builder setProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
      return this;
    }

    public Builder setSrcProtocol(@Nullable RoutingProtocol srcProtocol) {
      _srcProtocol = srcProtocol;
      return this;
    }

    public Builder setTag(long tag) {
      _tag = tag;
      return this;
    }

    public Builder setTunnelEncapsulationAttribute(
        @Nullable TunnelEncapsulationAttribute tunnelEncapsulationAttribute) {
      _tunnelEncapsulationAttribute = tunnelEncapsulationAttribute;
      return this;
    }

    public Builder setWeight(int weight) {
      _weight = weight;
      return this;
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("network", _network)
        .add("metric", _metric)
        .add("asPath", _asPath)
        .add("clusterList", _clusterList)
        .add("communities", _communities)
        .add("localPreference", _localPreference)
        .add("nextHop", _nextHop)
        .add("originatorIp", _originatorIp)
        .add("originMechanism", _originMechanism)
        .add("originType", _originType)
        .add("pathId", _pathId)
        .add("protocol", _protocol)
        .add("srcProtocol", _srcProtocol)
        .add("tag", _tag)
        .add("tunnelEncapsulationAttribute", _tunnelEncapsulationAttribute)
        .add("weight", _weight)
        .add("adminDist", _adminDist)
        .toString();
  }
}
