package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute.Builder;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/** A generic BGP route containing the common properties among different types of BGP routes */
@ParametersAreNonnullByDefault
public abstract class BgpRoute<B extends Builder<B, R>, R extends BgpRoute<B, R>>
    extends AbstractRoute
    implements HasReadableAsPath,
        HasReadableClusterList,
        HasReadableCommunities,
        HasReadableLocalPreference,
        HasReadableOriginatorIp,
        HasReadableOriginType,
        HasReadableSourceProtocol,
        HasReadableWeight {

  /**
   * Holds the common properties of BGP routes that are likely to be identical across many different
   * routes.
   *
   * <p>Interned for memory overhead.
   */
  protected static final class BgpRouteAttributes implements Serializable {
    public static BgpRouteAttributes create(
        @Nullable AsPath asPath,
        @Nullable Set<Long> clusterList,
        CommunitySet communities,
        long localPreference,
        long med,
        Ip originatorIp,
        OriginMechanism originMechanism,
        OriginType originType,
        RoutingProtocol protocol,
        boolean receivedFromRouteReflectorClient,
        @Nullable RoutingProtocol srcProtocol,
        @Nullable TunnelEncapsulationAttribute tunnelEncapsulationAttribute,
        int weight) {
      checkArgument(
          protocol == RoutingProtocol.BGP
              || protocol == RoutingProtocol.IBGP
              || protocol == RoutingProtocol.AGGREGATE,
          "Invalid BgpRoute protocol");
      // Intern.
      return ATTRIBUTE_CACHE.get(
          new BgpRouteAttributes(
              asPath,
              clusterList,
              communities,
              localPreference,
              med,
              originatorIp,
              originMechanism,
              originType,
              protocol,
              receivedFromRouteReflectorClient,
              srcProtocol,
              tunnelEncapsulationAttribute,
              weight));
    }

    private BgpRouteAttributes(
        @Nullable AsPath asPath,
        @Nullable Set<Long> clusterList,
        CommunitySet communities,
        long localPreference,
        long med,
        Ip originatorIp,
        OriginMechanism originMechanism,
        OriginType originType,
        RoutingProtocol protocol,
        boolean receivedFromRouteReflectorClient,
        @Nullable RoutingProtocol srcProtocol,
        @Nullable TunnelEncapsulationAttribute tunnelEncapsulationAttribute,
        int weight) {
      _asPath = firstNonNull(asPath, AsPath.empty());
      _clusterList = clusterList == null ? ImmutableSet.of() : CLUSTER_CACHE.get(clusterList);
      _communities = communities;
      _localPreference = localPreference;
      _med = med;
      _originatorIp = originatorIp;
      _originMechanism = (byte) originMechanism.ordinal();
      _originType = (byte) originType.ordinal();
      _protocol = (byte) protocol.ordinal();
      _receivedFromRouteReflectorClient = receivedFromRouteReflectorClient;
      _srcProtocol = srcProtocol == null ? -1 : (byte) srcProtocol.ordinal();
      _tunnelEncapsulationAttribute = tunnelEncapsulationAttribute;
      _weight = weight;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o == null || !(o instanceof BgpRouteAttributes)) {
        return false;
      }
      BgpRouteAttributes that = (BgpRouteAttributes) o;
      return (_hashCode == that._hashCode || _hashCode == 0 || that._hashCode == 0)
          && _protocol == that._protocol
          && _localPreference == that._localPreference
          && _med == that._med
          && _receivedFromRouteReflectorClient == that._receivedFromRouteReflectorClient
          && _weight == that._weight
          && _asPath.equals(that._asPath)
          && _clusterList.equals(that._clusterList)
          && _communities.equals(that._communities)
          && _originatorIp.equals(that._originatorIp)
          && _originMechanism == that._originMechanism
          && _originType == that._originType
          && _srcProtocol == that._srcProtocol
          && Objects.equals(_tunnelEncapsulationAttribute, that._tunnelEncapsulationAttribute);
    }

    private transient int _hashCode;

    @Override
    public int hashCode() {
      int h = _hashCode;
      if (h == 0) {
        h = _asPath.hashCode();
        h = h * 31 + _clusterList.hashCode();
        h = h * 31 + _communities.hashCode();
        h = h * 31 + Long.hashCode(_localPreference);
        h = h * 31 + Long.hashCode(_med);
        h = h * 31 + _originatorIp.hashCode();
        h = h * 31 + _originMechanism;
        h = h * 31 + _originType;
        h = h * 31 + _protocol;
        h = h * 31 + Boolean.hashCode(_receivedFromRouteReflectorClient);
        h = h * 31 + _srcProtocol;
        h = h * 31 + Objects.hashCode(_tunnelEncapsulationAttribute);
        h = h * 31 + _weight;
        _hashCode = h;
      }
      return h;
    }

    protected final @Nonnull AsPath _asPath;
    protected final @Nonnull Set<Long> _clusterList;
    protected final @Nonnull CommunitySet _communities;
    protected final long _localPreference;
    protected final long _med;
    protected final @Nonnull Ip _originatorIp;
    protected final boolean _receivedFromRouteReflectorClient;
    protected final @Nullable TunnelEncapsulationAttribute _tunnelEncapsulationAttribute;
    protected final int _weight;

    /////
    // Java enums are by-reference; using bytes of their ordinals (-1 for null) saves substantial
    // memory.
    /////
    /** Non-null */
    protected final byte _originMechanism;

    /** Non-null */
    protected final byte _originType;

    /** Non-null */
    protected final byte _protocol;

    /** -1 for null */
    protected final byte _srcProtocol;

    public @Nonnull OriginMechanism getOriginMechanism() {
      return OriginMechanism.fromOrdinal(_originMechanism);
    }

    public @Nonnull OriginType getOriginType() {
      return OriginType.fromOrdinal(_originType);
    }

    public @Nonnull RoutingProtocol getProtocol() {
      return RoutingProtocol.fromOrdinal(_protocol);
    }

    public @Nullable RoutingProtocol getSrcProtocol() {
      return _srcProtocol == -1 ? null : RoutingProtocol.fromOrdinal(_srcProtocol);
    }

    /** Re-intern after deserialization. */
    @Serial
    private Object readResolve() throws ObjectStreamException {
      return ATTRIBUTE_CACHE.get(this);
    }
  }

  /** Local-preference has a maximum value of u32 max. */
  public static final long MAX_LOCAL_PREFERENCE = (1L << 32) - 1;

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (100 bytes seems smallest possible object size, would be 100 MiB total).
  private static final LoadingCache<BgpRouteAttributes, BgpRouteAttributes> ATTRIBUTE_CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 20).build(a -> a);

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^16: Just some upper bound on cache size, well less than GiB.
  //   (8 bytes seems smallest possible entry (set(long)), would be 1 MiB total).
  private static final LoadingCache<Set<Long>, Set<Long>> CLUSTER_CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 16).build(ImmutableSet::copyOf);

  /** Builder for {@link BgpRoute} */
  @ParametersAreNonnullByDefault
  public abstract static class Builder<B extends Builder<B, R>, R extends BgpRoute<B, R>>
      extends AbstractRouteBuilder<B, R>
      implements HasWritableAsPath<B, R>,
          HasWritableClusterList<B, R>,
          HasWritableCommunities<B, R>,
          HasWritableLocalPreference<B, R>,
          HasWritableOriginatorIp<B, R>,
          HasWritableOriginType<B, R>,
          HasWritableWeight<B, R> {

    protected @Nonnull AsPath _asPath;
    // Invariant: either immutable or a local copy shielded from external mutations.
    protected @Nonnull Set<Long> _clusterList;
    protected @Nonnull CommunitySet _communities;
    protected long _localPreference;
    protected @Nullable Ip _originatorIp;
    protected @Nullable OriginMechanism _originMechanism;
    protected @Nullable OriginType _originType;
    protected @Nullable Integer _pathId;
    protected @Nullable RoutingProtocol _protocol;
    protected @Nullable ReceivedFrom _receivedFrom;

    protected boolean _receivedFromRouteReflectorClient;
    protected @Nullable RoutingProtocol _srcProtocol;
    protected @Nullable TunnelEncapsulationAttribute _tunnelEncapsulationAttribute;
    protected int _weight;

    protected Builder() {
      _asPath = AsPath.empty();
      _communities = CommunitySet.empty();
      _clusterList = ImmutableSet.of();
    }

    /**
     * Returns a completely new builder of type {@link B} which has all the fields unset.
     *
     * @return A completely new builder of type {@link B}.
     */
    /* This is needed in cases where we need to create a new builder having type same as any of the
    subclasses of BgpRoute's builder but we are not sure of the exact type of the concrete child
    class.
    For example while evaluating a routing policy and executing its statements we need
    to create a completely new builder which should be of the same type as environment's output
    route builder but we are not sure of the concrete type and only know that it extends the
    abstract BgpRoute's builder. */
    public @Nonnull abstract B newBuilder();

    @Override
    public @Nonnull abstract R build();

    @Override
    public @Nonnull AsPath getAsPath() {
      return _asPath;
    }

    @Override
    public @Nonnull Set<Long> getClusterList() {
      return _clusterList instanceof ImmutableSet
          ? _clusterList
          : Collections.unmodifiableSet(_clusterList);
    }

    @Override
    public @Nonnull CommunitySet getCommunities() {
      return _communities;
    }

    @Override
    public @Nonnull Set<Community> getCommunitiesAsSet() {
      return _communities.getCommunities();
    }

    @Override
    public long getLocalPreference() {
      return _localPreference;
    }

    @Override
    public @Nullable Ip getOriginatorIp() {
      return _originatorIp;
    }

    public @Nullable OriginMechanism getOriginMechanism() {
      return _originMechanism;
    }

    @Override
    public @Nullable OriginType getOriginType() {
      return _originType;
    }

    public @Nullable Integer getPathId() {
      return _pathId;
    }

    public @Nullable RoutingProtocol getProtocol() {
      return _protocol;
    }

    @Override
    protected @Nonnull abstract B getThis();

    @Override
    public int getWeight() {
      return _weight;
    }

    @Override
    public @Nonnull B setAsPath(AsPath asPath) {
      _asPath = asPath;
      return getThis();
    }

    /** Overwrite the clusterList attribute */
    @Override
    public B setClusterList(Set<Long> clusterList) {
      _clusterList = clusterList instanceof ImmutableSet ? clusterList : new HashSet<>(clusterList);
      return getThis();
    }

    /** Add to the cluster list attribute */
    public B addClusterList(Set<Long> clusterList) {
      if (_clusterList instanceof ImmutableSet) {
        _clusterList = new HashSet<>(_clusterList);
      }
      _clusterList.addAll(clusterList);
      return getThis();
    }

    /** Add to the cluster list attribute */
    public B addToClusterList(Long cluster) {
      if (_clusterList instanceof ImmutableSet) {
        _clusterList = new HashSet<>(_clusterList);
      }
      _clusterList.add(cluster);
      return getThis();
    }

    /** Overwrite communities */
    @Override
    public @Nonnull B setCommunities(CommunitySet communities) {
      _communities = communities;
      return getThis();
    }

    /** Overwrite communities */
    // TODO: remove in favor of setCommunities(CommunitySet)
    public B setCommunities(Collection<? extends Community> communities) {
      _communities = CommunitySet.of(communities);
      return getThis();
    }

    /** Add communities */
    public B addCommunities(Collection<? extends Community> communities) {
      if (communities.isEmpty()) {
        return getThis();
      }
      Set<Community> currentCommunities = _communities.getCommunities();
      if (currentCommunities.isEmpty()) {
        return setCommunities(communities);
      }
      if (currentCommunities.containsAll(communities)) {
        return getThis();
      }
      Set<Community> combined =
          ImmutableSet.<Community>builderWithExpectedSize(
                  currentCommunities.size() + communities.size())
              .addAll(currentCommunities)
              .addAll(communities)
              .build();
      _communities = CommunitySet.of(combined);
      return getThis();
    }

    @Override
    public @Nonnull B setLocalPreference(long localPreference) {
      _localPreference = localPreference;
      return getThis();
    }

    @Override
    public @Nonnull B setOriginatorIp(Ip originatorIp) {
      _originatorIp = originatorIp;
      return getThis();
    }

    public @Nonnull B setOriginMechanism(OriginMechanism originMechanism) {
      _originMechanism = originMechanism;
      return getThis();
    }

    @Override
    public @Nonnull B setOriginType(OriginType originType) {
      _originType = originType;
      return getThis();
    }

    public B setPathId(@Nullable Integer pathId) {
      _pathId = pathId;
      return getThis();
    }

    public B setProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
      return getThis();
    }

    public B setReceivedFrom(ReceivedFrom receivedFrom) {
      _receivedFrom = receivedFrom;
      return getThis();
    }

    public B setReceivedFromRouteReflectorClient(boolean receivedFromRouteReflectorClient) {
      _receivedFromRouteReflectorClient = receivedFromRouteReflectorClient;
      return getThis();
    }

    public B setSrcProtocol(@Nullable RoutingProtocol srcProtocol) {
      _srcProtocol = srcProtocol;
      return getThis();
    }

    public @Nonnull B setTunnelEncapsulationAttribute(
        @Nullable TunnelEncapsulationAttribute tunnelEncapsulationAttribute) {
      _tunnelEncapsulationAttribute = tunnelEncapsulationAttribute;
      return getThis();
    }

    @Override
    public @Nonnull B setWeight(int weight) {
      _weight = weight;
      return getThis();
    }
  }

  /** Default local preference for a BGP route if one is not set explicitly */
  public static final long DEFAULT_LOCAL_PREFERENCE = 100L;

  /** Default weight for a local BGP route. */
  public static final int DEFAULT_LOCAL_WEIGHT = 32768;

  public static final String PROP_AS_PATH = "asPath";
  static final String PROP_CLUSTER_LIST = "clusterList";
  public static final String PROP_COMMUNITIES = "communities";
  public static final String PROP_LOCAL_PREFERENCE = "localPreference";
  static final String PROP_ORIGIN_MECHANISM = "originMechanism";
  static final String PROP_ORIGIN_TYPE = "originType";
  static final String PROP_ORIGINATOR_IP = "originatorIp";
  static final String PROP_PATH_ID = "pathId";
  static final String PROP_RECEIVED_FROM = "receivedFrom";
  static final String PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT =
      "receivedFromRouteReflectorClient";
  static final String PROP_SRC_PROTOCOL = "srcProtocol";
  static final String PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE = "tunnelEncapsulationAttribute";
  static final String PROP_WEIGHT = "weight";

  protected final @Nonnull BgpRouteAttributes _attributes;
  protected final @Nullable Integer _pathId;

  protected final @Nonnull ReceivedFrom _receivedFrom;

  protected BgpRoute(
      @Nullable Prefix network,
      @Nonnull NextHop nextHop,
      @Nullable Integer pathId,
      int admin,
      BgpRouteAttributes attributes,
      ReceivedFrom receivedFrom,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, admin, tag, nonRouting, nonForwarding);
    _attributes = attributes;
    _nextHop = nextHop;
    _pathId = pathId;
    _receivedFrom = receivedFrom;
  }

  @JsonProperty(PROP_AS_PATH)
  @Override
  public @Nonnull AsPath getAsPath() {
    return _attributes._asPath;
  }

  @Override
  public @Nonnull Set<Long> getClusterList() {
    return _attributes._clusterList;
  }

  /** Return the set of all community attributes */
  @Override
  public final @Nonnull CommunitySet getCommunities() {
    return _attributes._communities;
  }

  /** Return the set of all community attributes */
  @Override
  public final @Nonnull Set<Community> getCommunitiesAsSet() {
    return _attributes._communities.getCommunities();
  }

  /** Return only standard community attributes */
  @JsonIgnore
  public @Nonnull Set<StandardCommunity> getStandardCommunities() {
    return _attributes._communities.getStandardCommunities();
  }

  /** Return only extended community attributes */
  @JsonIgnore
  public @Nonnull Set<ExtendedCommunity> getExtendedCommunities() {
    return _attributes._communities.getExtendedCommunities();
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  @Override
  public long getLocalPreference() {
    return _attributes._localPreference;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  @Override
  public long getMetric() {
    return _attributes._med;
  }

  @JsonProperty(PROP_ORIGINATOR_IP)
  @Override
  public @Nonnull Ip getOriginatorIp() {
    return _attributes._originatorIp;
  }

  @JsonProperty(PROP_ORIGIN_MECHANISM)
  public @Nonnull OriginMechanism getOriginMechanism() {
    return _attributes.getOriginMechanism();
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  @Override
  public @Nonnull OriginType getOriginType() {
    return _attributes.getOriginType();
  }

  /**
   * Path ID specified in the BGP advertisement that resulted in this route. Null if the
   * advertisement specified no path ID or if the route is local.
   */
  @JsonProperty(PROP_PATH_ID)
  public @Nullable Integer getPathId() {
    return _pathId;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_PROTOCOL)
  @Override
  public @Nonnull RoutingProtocol getProtocol() {
    return _attributes.getProtocol();
  }

  @JsonProperty(PROP_RECEIVED_FROM)
  public @Nonnull ReceivedFrom getReceivedFrom() {
    return _receivedFrom;
  }

  @JsonProperty(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT)
  public boolean getReceivedFromRouteReflectorClient() {
    return _attributes._receivedFromRouteReflectorClient;
  }

  @JsonProperty(PROP_SRC_PROTOCOL)
  @Override
  public @Nullable RoutingProtocol getSrcProtocol() {
    return _attributes.getSrcProtocol();
  }

  @JsonProperty(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE)
  public @Nullable TunnelEncapsulationAttribute getTunnelEncapsulationAttribute() {
    return _attributes._tunnelEncapsulationAttribute;
  }

  @JsonProperty(PROP_WEIGHT)
  @Override
  public int getWeight() {
    return _attributes._weight;
  }

  @Override
  public abstract B toBuilder();

  @JsonProperty(PROP_CLUSTER_LIST)
  private @Nonnull SortedSet<Long> getJsonClusterList() {
    return ImmutableSortedSet.copyOf(_attributes._clusterList);
  }

  @JsonProperty(PROP_COMMUNITIES)
  private @Nonnull CommunitySet getJsonCommunities() {
    return _attributes._communities;
  }

  /** Whether the route is a trackable redistributed local route. */
  @JsonIgnore
  public boolean isTrackableLocalRoute() {
    return switch (_attributes.getOriginMechanism()) {
      case NETWORK, REDISTRIBUTE -> true;
      case GENERATED, LEARNED -> false;
    };
  }
}
