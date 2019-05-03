package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;

/** A generic BGP route containing the common properties among different types of BGP routes */
@ParametersAreNonnullByDefault
public abstract class BgpRoute extends AbstractRoute {

  /** Builder for {@link BgpRoute} */
  @ParametersAreNonnullByDefault
  public abstract static class Builder<B extends Builder<B, R>, R extends BgpRoute>
      extends AbstractRouteBuilder<B, R> {

    @Nonnull protected AsPath _asPath;
    @Nonnull protected ImmutableSortedSet.Builder<Long> _clusterList;
    @Nonnull protected SortedSet<Community> _communities;
    protected boolean _discard;
    protected long _localPreference;
    @Nullable protected String _nextHopInterface;
    @Nullable protected Ip _originatorIp;
    @Nullable protected OriginType _originType;
    @Nullable protected RoutingProtocol _protocol;
    @Nullable protected Ip _receivedFromIp;
    protected boolean _receivedFromRouteReflectorClient;
    @Nullable protected RoutingProtocol _srcProtocol;
    protected int _weight;

    public Builder() {
      _asPath = AsPath.empty();
      _communities = new TreeSet<>();
      _clusterList = new ImmutableSortedSet.Builder<>(Ordering.natural());
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
    @Nonnull
    public abstract B newBuilder();

    @Nonnull
    @Override
    public abstract R build();

    @Nonnull
    public AsPath getAsPath() {
      return _asPath;
    }

    @Nonnull
    public SortedSet<Long> getClusterList() {
      return _clusterList.build();
    }

    @Nonnull
    public SortedSet<Community> getCommunities() {
      return _communities;
    }

    public long getLocalPreference() {
      return _localPreference;
    }

    @Nullable
    public Ip getOriginatorIp() {
      return _originatorIp;
    }

    @Nullable
    public OriginType getOriginType() {
      return _originType;
    }

    @Nullable
    public RoutingProtocol getProtocol() {
      return _protocol;
    }

    @Override
    @Nonnull
    protected abstract B getThis();

    public int getWeight() {
      return _weight;
    }

    public B setAsPath(AsPath asPath) {
      _asPath = asPath;
      return getThis();
    }

    /** Overwrite the clusterList attribute */
    public B setClusterList(Set<Long> clusterList) {
      _clusterList = new ImmutableSortedSet.Builder<>(Ordering.natural());
      _clusterList.addAll(clusterList);
      return getThis();
    }

    /** Add to the cluster list attribute */
    public B addClusterList(Set<Long> clusterList) {
      _clusterList.addAll(clusterList);
      return getThis();
    }

    /** Add to the cluster list attribute */
    public B addToClusterList(Long cluster) {
      _clusterList.add(cluster);
      return getThis();
    }

    /** Overwrite communities */
    public B setCommunities(Set<Community> communities) {
      _communities = new TreeSet<Community>();
      _communities.addAll(communities);
      return getThis();
    }

    /** Add communities */
    public B addCommunities(Set<Community> communities) {
      _communities.addAll(communities);
      return getThis();
    }

    /** Add a single community */
    public B addCommunity(Community community) {
      _communities.add(community);
      return getThis();
    }

    /** Add communities */
    public B removeCommunities(Set<Community> communities) {
      _communities.removeAll(communities);
      return getThis();
    }

    public B setDiscard(boolean discard) {
      _discard = discard;
      return getThis();
    }

    public B setLocalPreference(long localPreference) {
      _localPreference = localPreference;
      return getThis();
    }

    public @Nonnull B setNextHopInterface(String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
      return getThis();
    }

    public B setOriginatorIp(Ip originatorIp) {
      _originatorIp = originatorIp;
      return getThis();
    }

    public B setOriginType(OriginType originType) {
      _originType = originType;
      return getThis();
    }

    public B setProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
      return getThis();
    }

    public B setReceivedFromIp(@Nullable Ip receivedFromIp) {
      _receivedFromIp = receivedFromIp;
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

    public B setWeight(int weight) {
      _weight = weight;
      return getThis();
    }
  }

  /** Default local preference for a BGP route if one is not set explicitly */
  public static final long DEFAULT_LOCAL_PREFERENCE = 100L;

  public static final String PROP_AS_PATH = "asPath";
  static final String PROP_CLUSTER_LIST = "clusterList";
  public static final String PROP_COMMUNITIES = "communities";
  static final String PROP_DISCARD = "discard";
  public static final String PROP_LOCAL_PREFERENCE = "localPreference";
  static final String PROP_ORIGIN_TYPE = "originType";
  static final String PROP_ORIGINATOR_IP = "originatorIp";
  static final String PROP_RECEIVED_FROM_IP = "receivedFromIp";
  static final String PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT =
      "receivedFromRouteReflectorClient";
  static final String PROP_SRC_PROTOCOL = "srcProtocol";
  static final String PROP_WEIGHT = "weight";

  private static final long serialVersionUID = 1L;

  @Nonnull protected final AsPath _asPath;
  @Nonnull protected final SortedSet<Long> _clusterList;
  @Nonnull protected final SortedSet<Community> _communities;
  protected final boolean _discard;
  protected final long _localPreference;
  protected final long _med;
  @Nonnull protected final String _nextHopInterface;
  @Nonnull protected final Ip _nextHopIp;
  @Nonnull protected final Ip _originatorIp;
  @Nonnull protected final OriginType _originType;
  @Nonnull protected final RoutingProtocol _protocol;
  @Nullable protected final Ip _receivedFromIp;
  protected final boolean _receivedFromRouteReflectorClient;
  @Nullable protected final RoutingProtocol _srcProtocol;
  /* NOTE: Cisco-only attribute */
  protected final int _weight;

  protected BgpRoute(
      @Nullable Prefix network,
      @Nullable Ip nextHopIp,
      int admin,
      @Nullable AsPath asPath,
      @Nullable SortedSet<Community> communities,
      boolean discard,
      long localPreference,
      long med,
      String nextHopInterface,
      Ip originatorIp,
      @Nullable SortedSet<Long> clusterList,
      boolean receivedFromRouteReflectorClient,
      OriginType originType,
      RoutingProtocol protocol,
      @Nullable Ip receivedFromIp,
      @Nullable RoutingProtocol srcProtocol,
      int weight,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, admin, nonRouting, nonForwarding);
    checkArgument(
        protocol == RoutingProtocol.BGP
            || protocol == RoutingProtocol.IBGP
            || protocol == RoutingProtocol.AGGREGATE,
        "Invalid BgpRoute protocol");
    _asPath = firstNonNull(asPath, AsPath.empty());
    _clusterList =
        clusterList == null ? ImmutableSortedSet.of() : ImmutableSortedSet.copyOf(clusterList);
    _communities =
        communities == null ? ImmutableSortedSet.of() : ImmutableSortedSet.copyOf(communities);
    _discard = discard;
    _localPreference = localPreference;
    _med = med;
    _nextHopInterface = nextHopInterface;
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
    _originatorIp = originatorIp;
    _originType = originType;
    _protocol = protocol;
    _receivedFromIp = receivedFromIp;
    _receivedFromRouteReflectorClient = receivedFromRouteReflectorClient;
    _srcProtocol = srcProtocol;
    _weight = weight;
  }

  @Nonnull
  @JsonProperty(PROP_AS_PATH)
  public AsPath getAsPath() {
    return _asPath;
  }

  @Nonnull
  @JsonProperty(PROP_CLUSTER_LIST)
  public SortedSet<Long> getClusterList() {
    return _clusterList;
  }

  @Nonnull
  @JsonProperty(PROP_COMMUNITIES)
  public SortedSet<Community> getCommunities() {
    return _communities;
  }

  @JsonProperty(PROP_DISCARD)
  public boolean getDiscard() {
    return _discard;
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public long getLocalPreference() {
    return _localPreference;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  @Override
  public Long getMetric() {
    return _med;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_INTERFACE)
  @Nonnull
  @Override
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_IP)
  @Override
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Nonnull
  @JsonProperty(PROP_ORIGINATOR_IP)
  public Ip getOriginatorIp() {
    return _originatorIp;
  }

  @Nonnull
  @JsonProperty(PROP_ORIGIN_TYPE)
  public OriginType getOriginType() {
    return _originType;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_PROTOCOL)
  @Override
  public RoutingProtocol getProtocol() {
    return _protocol;
  }

  @Nullable
  @JsonProperty(PROP_RECEIVED_FROM_IP)
  public Ip getReceivedFromIp() {
    return _receivedFromIp;
  }

  @JsonProperty(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT)
  public boolean getReceivedFromRouteReflectorClient() {
    return _receivedFromRouteReflectorClient;
  }

  @Nullable
  @JsonProperty(PROP_SRC_PROTOCOL)
  public RoutingProtocol getSrcProtocol() {
    return _srcProtocol;
  }

  @Override
  public int getTag() {
    return NO_TAG;
  }

  @JsonProperty(PROP_WEIGHT)
  public int getWeight() {
    return _weight;
  }
}
