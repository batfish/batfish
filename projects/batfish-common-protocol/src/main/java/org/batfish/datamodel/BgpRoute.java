package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute.Builder;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.route.nh.NextHop;

/** A generic BGP route containing the common properties among different types of BGP routes */
@ParametersAreNonnullByDefault
public abstract class BgpRoute<B extends Builder<B, R>, R extends BgpRoute<B, R>>
    extends BgpAttributesRoute<B, R> {

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^16: Just some upper bound on cache size, well less than GiB.
  //   (8 bytes seems smallest possible entry (set(long)), would be 1 MiB total).
  private static final LoadingCache<Set<Long>, Set<Long>> CLUSTER_CACHE =
      CacheBuilder.newBuilder()
          .softValues()
          .maximumSize(1 << 16)
          .build(CacheLoader.from(ImmutableSet::copyOf));

  /** Builder for {@link BgpRoute} */
  @ParametersAreNonnullByDefault
  public abstract static class Builder<B extends Builder<B, R>, R extends BgpRoute<B, R>>
      extends BgpAttributesRoute.Builder<B, R> {

    @Nonnull protected Set<Long> _clusterList;
    // Invariant: either immutable or a local copy shielded from external mutations.
    @Nullable protected Ip _originatorIp;
    @Nullable protected RoutingProtocol _protocol;
    @Nullable protected Ip _receivedFromIp;
    protected boolean _receivedFromRouteReflectorClient;
    @Nullable protected RoutingProtocol _srcProtocol;

    protected Builder() {
      _clusterList = ImmutableSet.of();
    }

    @Nonnull
    public Set<Long> getClusterList() {
      return _clusterList instanceof ImmutableSet
          ? _clusterList
          : Collections.unmodifiableSet(_clusterList);
    }

    @Nullable
    public Ip getOriginatorIp() {
      return _originatorIp;
    }

    @Nullable
    public RoutingProtocol getProtocol() {
      return _protocol;
    }

    /** Overwrite the clusterList attribute */
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

    public B setOriginatorIp(Ip originatorIp) {
      _originatorIp = originatorIp;
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
  }

  static final String PROP_CLUSTER_LIST = "clusterList";
  static final String PROP_ORIGINATOR_IP = "originatorIp";
  static final String PROP_RECEIVED_FROM_IP = "receivedFromIp";
  static final String PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT =
      "receivedFromRouteReflectorClient";
  static final String PROP_SRC_PROTOCOL = "srcProtocol";

  @Nonnull protected final Set<Long> _clusterList;
  @Nonnull protected final Ip _originatorIp;
  @Nonnull protected final RoutingProtocol _protocol;

  /**
   * The {@link Ip} address of the (I)BGP peer from which the route was learned, or {@link Ip#ZERO}
   * if the BGP route was originated locally.
   *
   * <p>Set on origination and on import.
   */
  @Nullable protected final Ip _receivedFromIp;

  protected final boolean _receivedFromRouteReflectorClient;
  @Nullable protected final RoutingProtocol _srcProtocol;

  protected BgpRoute(
      @Nullable Prefix network,
      @Nonnull NextHop nextHop,
      int admin,
      @Nullable AsPath asPath,
      @Nullable Set<Community> communities,
      long localPreference,
      long med,
      Ip originatorIp,
      @Nullable Set<Long> clusterList,
      boolean receivedFromRouteReflectorClient,
      OriginType originType,
      RoutingProtocol protocol,
      @Nullable Ip receivedFromIp,
      @Nullable RoutingProtocol srcProtocol,
      long tag,
      int weight,
      boolean nonForwarding,
      boolean nonRouting) {
    super(
        network,
        nextHop,
        admin,
        asPath,
        communities,
        localPreference,
        med,
        originType,
        tag,
        weight,
        nonForwarding,
        nonRouting);
    checkArgument(
        protocol == RoutingProtocol.BGP
            || protocol == RoutingProtocol.IBGP
            || protocol == RoutingProtocol.AGGREGATE,
        "Invalid BgpRoute protocol");
    _clusterList =
        clusterList == null ? ImmutableSet.of() : CLUSTER_CACHE.getUnchecked(clusterList);
    _nextHop = nextHop;
    _originatorIp = originatorIp;
    _protocol = protocol;
    _receivedFromIp = receivedFromIp;
    _receivedFromRouteReflectorClient = receivedFromRouteReflectorClient;
    _srcProtocol = srcProtocol;
  }

  public @Nonnull Set<Long> getClusterList() {
    return _clusterList;
  }

  @Nonnull
  @JsonProperty(PROP_ORIGINATOR_IP)
  public Ip getOriginatorIp() {
    return _originatorIp;
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

  @JsonProperty(PROP_CLUSTER_LIST)
  private @Nonnull SortedSet<Long> getJsonClusterList() {
    return ImmutableSortedSet.copyOf(_clusterList);
  }
}
