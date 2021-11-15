package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.OriginMechanism.LEARNED;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
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
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.community.Community;

/** A user facing representation for IPv4 BGP route */
@ParametersAreNonnullByDefault
public final class BgpRoute {
  public static final String PROP_AS_PATH = "asPath";
  public static final String PROP_COMMUNITIES = "communities";
  public static final String PROP_LOCAL_PREFERENCE = "localPreference";
  public static final String PROP_METRIC = "metric";
  public static final String PROP_NETWORK = "network";
  public static final String PROP_NEXT_HOP_IP = "nextHopIp";
  public static final String PROP_ORIGINATOR_IP = "originatorIp";
  public static final String PROP_ORIGIN_MECHANISM = "originMechanism";
  public static final String PROP_ORIGIN_TYPE = "originType";
  public static final String PROP_PROTOCOL = "protocol";
  public static final String PROP_SRC_PROTOCOL = "srcProtocol";
  public static final String PROP_TAG = "tag";
  public static final String PROP_WEIGHT = "weight";
  public static final String PROP_CLASS = "class";

  @Nonnull private final AsPath _asPath;
  @Nonnull private final SortedSet<Community> _communities;
  private final long _localPreference;
  private final long _metric;
  @Nonnull private final Prefix _network;
  @Nonnull private final Ip _nextHopIp;
  @Nonnull private final Ip _originatorIp;
  @Nonnull private final OriginMechanism _originMechanism;
  @Nonnull private final OriginType _originType;
  @Nonnull private final RoutingProtocol _protocol;
  @Nullable private final RoutingProtocol _srcProtocol;
  private final long _tag;
  private final int _weight;

  private BgpRoute(
      AsPath asPath,
      SortedSet<Community> communities,
      long localPreference,
      long metric,
      Prefix network,
      Ip nextHopIp,
      Ip originatorIp,
      OriginMechanism originMechanism,
      OriginType originType,
      RoutingProtocol protocol,
      @Nullable RoutingProtocol srcProtocol,
      long tag,
      int weight) {
    _asPath = asPath;
    _communities = communities;
    _localPreference = localPreference;
    _metric = metric;
    _network = network;
    _nextHopIp = nextHopIp;
    _originatorIp = originatorIp;
    _originMechanism = originMechanism;
    _originType = originType;
    _protocol = protocol;
    _srcProtocol = srcProtocol;
    _tag = tag;
    _weight = weight;
  }

  @JsonCreator
  private static BgpRoute jsonCreator(
      @Nullable @JsonProperty(PROP_AS_PATH) AsPath asPath,
      @Nullable @JsonProperty(PROP_COMMUNITIES) SortedSet<Community> communities,
      @JsonProperty(PROP_LOCAL_PREFERENCE) long localPreference,
      @JsonProperty(PROP_METRIC) long metric,
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_ORIGINATOR_IP) Ip originatorIp,
      @Nullable @JsonProperty(PROP_ORIGIN_MECHANISM) OriginMechanism originMechanism,
      @Nullable @JsonProperty(PROP_ORIGIN_TYPE) OriginType originType,
      @Nullable @JsonProperty(PROP_PROTOCOL) RoutingProtocol protocol,
      @Nullable @JsonProperty(PROP_SRC_PROTOCOL) RoutingProtocol srcProtocol,
      @JsonProperty(PROP_TAG) long tag,
      @JsonProperty(PROP_WEIGHT) int weight,
      // For backwards compatibility, does nothing
      @Nullable @JsonProperty(PROP_CLASS) String clazz) {
    checkArgument(network != null, "%s must be specified", PROP_NETWORK);
    checkArgument(originatorIp != null, "%s must be specified", PROP_ORIGINATOR_IP);
    checkArgument(originType != null, "%s must be specified", PROP_ORIGIN_TYPE);
    checkArgument(protocol != null, "%s must be specified", PROP_PROTOCOL);
    return new BgpRoute(
        firstNonNull(asPath, AsPath.empty()),
        firstNonNull(communities, ImmutableSortedSet.of()),
        localPreference,
        metric,
        network,
        firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP),
        originatorIp,
        firstNonNull(originMechanism, LEARNED),
        originType,
        protocol,
        srcProtocol,
        tag,
        weight);
  }

  @Nonnull
  @JsonProperty(PROP_AS_PATH)
  public AsPath getAsPath() {
    return _asPath;
  }

  @Nonnull
  @JsonProperty(PROP_COMMUNITIES)
  public SortedSet<Community> getCommunities() {
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

  @Nonnull
  @JsonProperty(PROP_NETWORK)
  public Prefix getNetwork() {
    return _network;
  }

  @Nonnull
  @JsonProperty(PROP_NEXT_HOP_IP)
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Nonnull
  @JsonProperty(PROP_ORIGINATOR_IP)
  public Ip getOriginatorIp() {
    return _originatorIp;
  }

  @Nonnull
  @JsonProperty(PROP_ORIGIN_MECHANISM)
  public OriginMechanism getOriginMechanism() {
    return _originMechanism;
  }

  @Nonnull
  @JsonProperty(PROP_ORIGIN_TYPE)
  public OriginType getOriginType() {
    return _originType;
  }

  @Nonnull
  @JsonProperty(PROP_PROTOCOL)
  public RoutingProtocol getProtocol() {
    return _protocol;
  }

  @Nullable
  @JsonProperty(PROP_SRC_PROTOCOL)
  public RoutingProtocol getSrcProtocol() {
    return _srcProtocol;
  }

  @JsonProperty(PROP_TAG)
  public long getTag() {
    return _tag;
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
        && Objects.equals(_asPath, bgpRoute._asPath)
        && Objects.equals(_communities, bgpRoute._communities)
        && Objects.equals(_network, bgpRoute._network)
        && Objects.equals(_nextHopIp, bgpRoute._nextHopIp)
        && Objects.equals(_originatorIp, bgpRoute._originatorIp)
        && _originMechanism == bgpRoute._originMechanism
        && _originType == bgpRoute._originType
        && _protocol == bgpRoute._protocol
        && _srcProtocol == bgpRoute._srcProtocol;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _asPath,
        _communities,
        _localPreference,
        _metric,
        _network,
        _nextHopIp,
        _originatorIp,
        _originMechanism.ordinal(),
        _originType.ordinal(),
        _protocol.ordinal(),
        _srcProtocol != null ? _srcProtocol.ordinal() : null,
        _tag,
        _weight);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return builder()
        .setAsPath(_asPath)
        .setCommunities(_communities)
        .setLocalPreference(_localPreference)
        .setMetric(_metric)
        .setNetwork(_network)
        .setNextHopIp(_nextHopIp)
        .setProtocol(_protocol)
        .setOriginatorIp(_originatorIp)
        .setOriginMechanism(_originMechanism)
        .setOriginType(_originType)
        .setSrcProtocol(_srcProtocol)
        .setTag(_tag)
        .setWeight(_weight);
  }

  /** Builder for {@link BgpRoute} */
  @ParametersAreNonnullByDefault
  public static final class Builder {

    @Nonnull private AsPath _asPath;
    @Nonnull private SortedSet<Community> _communities;
    private long _localPreference;
    private long _metric;
    @Nullable private Prefix _network;
    @Nullable private Ip _nextHopIp;
    @Nullable private Ip _originatorIp;
    @Nullable private OriginMechanism _originMechanism;
    @Nullable private OriginType _originType;
    @Nullable private RoutingProtocol _protocol;
    @Nullable private RoutingProtocol _srcProtocol;
    private long _tag;
    private int _weight;

    public Builder() {
      _asPath = AsPath.empty();
      _communities = ImmutableSortedSet.of();
      _nextHopIp = Route.UNSET_ROUTE_NEXT_HOP_IP;
    }

    public BgpRoute build() {
      checkArgument(_network != null, "%s must be specified", PROP_NETWORK);
      checkArgument(_nextHopIp != null, "%s must be specified", PROP_NEXT_HOP_IP);
      checkArgument(_originatorIp != null, "%s must be specified", PROP_ORIGINATOR_IP);
      checkArgument(_originType != null, "%s must be specified", PROP_ORIGIN_TYPE);
      checkArgument(_protocol != null, "%s must be specified", PROP_PROTOCOL);
      return new BgpRoute(
          _asPath,
          _communities,
          _localPreference,
          _metric,
          _network,
          _nextHopIp,
          _originatorIp,
          firstNonNull(_originMechanism, LEARNED),
          _originType,
          _protocol,
          _srcProtocol,
          _tag,
          _weight);
    }

    public Builder setAsPath(AsPath asPath) {
      _asPath = asPath;
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

    public Builder setNextHopIp(Ip nextHopIp) {
      _nextHopIp = nextHopIp;
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
        .add("communities", _communities)
        .add("localPreference", _localPreference)
        .add("nextHopIp", _nextHopIp)
        .add("originatorIp", _originatorIp)
        .add("originMechanism", _originMechanism)
        .add("originType", _originType)
        .add("protocol", _protocol)
        .add("srcProtocol", _srcProtocol)
        .add("tag", _tag)
        .add("weight", _weight)
        .toString();
  }
}
