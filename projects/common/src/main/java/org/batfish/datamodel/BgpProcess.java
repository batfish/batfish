package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.BgpAggregate;
import org.batfish.datamodel.bgp.BgpConfederation;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.bgp.NextHopIpTieBreaker;

/** Represents a bgp process on a router */
public class BgpProcess implements Serializable {

  /** Constructs a BgpProcess with the given router ID and default Cisco admin costs. */
  @VisibleForTesting
  public static BgpProcess testBgpProcess(@Nonnull Ip routerId) {
    return builder()
        .setRouterId(routerId)
        .setEbgpAdminCost(20)
        .setIbgpAdminCost(200)
        .setLocalAdminCost(220)
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .build();
  }

  public static class Builder {

    private @Nullable Boolean _clientToClientReflection;
    private @Nullable BgpConfederation _confederation;
    private @Nullable Integer _ebgpAdminCost;
    private @Nullable Integer _ibgpAdminCost;
    private @Nullable Integer _localAdminCost;
    private @Nullable Ip _routerId;
    private @Nullable Vrf _vrf;
    private @Nullable String _networkPolicy;
    private @Nullable String _mainRibIndependentNetworkPolicy;
    private @Nullable String _redistributionPolicy;
    private @Nullable LocalOriginationTypeTieBreaker _localOriginationTypeTieBreaker;
    private @Nullable NextHopIpTieBreaker _networkNextHopIpTieBreaker;
    private @Nullable NextHopIpTieBreaker _redistributeNextHopIpTieBreaker;

    public BgpProcess build() {
      checkArgument(_routerId != null, "Missing %s", PROP_ROUTER_ID);
      checkArgument(_ebgpAdminCost != null, "Missing %s", PROP_EBGP_ADMIN_COST);
      checkArgument(_ibgpAdminCost != null, "Missing %s", PROP_IBGP_ADMIN_COST);
      checkArgument(_localAdminCost != null, "Missing %s", PROP_LOCAL_ADMIN_COST);
      checkArgument(
          _localOriginationTypeTieBreaker != null,
          "Missing %s",
          PROP_LOCAL_ORIGINATION_TYPE_TIE_BREAKER);
      checkArgument(
          _networkNextHopIpTieBreaker != null, "Missing %s", PROP_NETWORK_NEXT_HOP_IP_TIE_BREAKER);
      checkArgument(
          _redistributeNextHopIpTieBreaker != null,
          "Missing %s",
          PROP_REDISTRIBUTE_NEXT_HOP_IP_TIE_BREAKER);
      checkArgument(
          _networkPolicy == null || _redistributionPolicy != null,
          "Impossible to have %s without %s",
          PROP_INDEPENDENT_NETWORK_POLICY,
          PROP_REDISTRIBUTION_POLICY);
      BgpProcess bgpProcess =
          new BgpProcess(
              _routerId,
              _ebgpAdminCost,
              _ibgpAdminCost,
              _localAdminCost,
              _clientToClientReflection,
              _confederation,
              _networkPolicy,
              _mainRibIndependentNetworkPolicy,
              _redistributionPolicy,
              _localOriginationTypeTieBreaker,
              _networkNextHopIpTieBreaker,
              _redistributeNextHopIpTieBreaker);
      if (_vrf != null) {
        _vrf.setBgpProcess(bgpProcess);
      }
      return bgpProcess;
    }

    public @Nonnull Builder setClientToClientReflection(
        @Nullable Boolean clientToClientReflection) {
      _clientToClientReflection = clientToClientReflection;
      return this;
    }

    public @Nonnull Builder setConfederation(@Nullable BgpConfederation confederation) {
      _confederation = confederation;
      return this;
    }

    /** Sets the EBGP administrative distance for this process. */
    public @Nonnull Builder setEbgpAdminCost(int ebgpAdminCost) {
      _ebgpAdminCost = ebgpAdminCost;
      return this;
    }

    /** Sets the IBGP administrative distance for this process. */
    public @Nonnull Builder setIbgpAdminCost(int ibgpAdminCost) {
      _ibgpAdminCost = ibgpAdminCost;
      return this;
    }

    /** Sets the Local administrative distance for this process. */
    public @Nonnull Builder setLocalAdminCost(int localAdminCost) {
      _localAdminCost = localAdminCost;
      return this;
    }

    public @Nonnull Builder setRouterId(Ip routerId) {
      _routerId = routerId;
      return this;
    }

    public @Nonnull Builder setVrf(Vrf vrf) {
      _vrf = vrf;
      return this;
    }

    public @Nonnull Builder setNetworkPolicy(@Nullable String networkPolicy) {
      _networkPolicy = networkPolicy;
      return this;
    }

    public @Nonnull Builder setMainRibIndependentNetworkPolicy(
        @Nullable String mainRibIndependentNetworkPolicy) {
      _mainRibIndependentNetworkPolicy = mainRibIndependentNetworkPolicy;
      return this;
    }

    public @Nonnull Builder setRedistributionPolicy(@Nullable String redistributionPolicy) {
      _redistributionPolicy = redistributionPolicy;
      return this;
    }

    public @Nonnull Builder setLocalOriginationTypeTieBreaker(
        LocalOriginationTypeTieBreaker localOriginationTypeTieBreaker) {
      _localOriginationTypeTieBreaker = localOriginationTypeTieBreaker;
      return this;
    }

    public @Nonnull Builder setNetworkNextHopIpTieBreaker(
        NextHopIpTieBreaker networkNextHopIpTieBreaker) {
      _networkNextHopIpTieBreaker = networkNextHopIpTieBreaker;
      return this;
    }

    public @Nonnull Builder setRedistributeNextHopIpTieBreaker(
        @Nullable NextHopIpTieBreaker redistributeNextHopIpTieBreaker) {
      _redistributeNextHopIpTieBreaker = redistributeNextHopIpTieBreaker;
      return this;
    }
  }

  private class ClusterIdsSupplier implements Serializable, Supplier<Set<Long>> {

    @Override
    public Set<Long> get() {
      return _activeNeighbors.values().stream()
          .map(BgpPeerConfig::getClusterId)
          .filter(Objects::nonNull)
          .collect(ImmutableSet.toImmutableSet());
    }
  }

  private static final String PROP_AGGREGATES = "aggregates";
  private static final String PROP_CLIENT_TO_CLIENT_REFLECTION = "clientToClientReflection";
  private static final String PROP_CONFEDERATION = "confederation";
  private static final String PROP_EBGP_ADMIN_COST = "ebgpAdminCost";
  private static final String PROP_IBGP_ADMIN_COST = "ibgpAdminCost";
  private static final String PROP_LOCAL_ADMIN_COST = "localAdminCost";
  private static final String PROP_INTERFACE_NEIGHBORS = "interfaceNeighbors";
  private static final String PROP_PASSIVE_NEIGHBORS = "dynamicNeighbors";
  private static final String PROP_MULTIPATH_EBGP = "multipathEbgp";
  private static final String PROP_MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE =
      "multipathEquivalentAsPathMatchMode";
  private static final String PROP_MULTIPATH_IBGP = "multipathIbgp";
  private static final String PROP_ACTIVE_NEIGHBORS = "neighbors";
  private static final String PROP_ROUTER_ID = "routerId";
  private static final String PROP_TIE_BREAKER = "tieBreaker";
  private static final String PROP_CLUSTER_LIST_AS_IBGP_COST = "clusterListAsIbgpCost";
  private static final String PROP_CLUSTER_LIST_AS_IGP_COST_DEPRECATED = "clusterListAsIgpCost";
  private static final String PROP_INDEPENDENT_NETWORK_POLICY = "independentNetworkPolicy";
  private static final String PROP_MAIN_RIB_INDEPENDENT_NETWORK_POLICY =
      "mainRibIndependentNetworkPolicy";
  private static final String PROP_REDISTRIBUTION_POLICY = "redistributionPolicy";
  private static final String PROP_LOCAL_ORIGINATION_TYPE_TIE_BREAKER =
      "localOriginationTypeTieBreaker";
  private static final String PROP_NETWORK_NEXT_HOP_IP_TIE_BREAKER = "networkNextHopIpTieBreaker";
  private static final String PROP_REDISTRIBUTE_NEXT_HOP_IP_TIE_BREAKER =
      "redistributeNextHopIpTieBreaker";
  private static final String PROP_TRACKS = "tracks";

  private static final String PROP_NEXT_HOP_IP_RESOLVER_RESTRICTION_POLICY =
      "nextHopIpResolverRestrictionPolicy";

  private boolean _clientToClientReflection;
  private @Nullable BgpConfederation _confederation;
  private final int _ebgpAdminCost;
  private final int _ibgpAdminCost;
  private final int _localAdminCost;
  private final @Nonnull Supplier<Set<Long>> _clusterIds;
  private @Nonnull Map<String, BgpUnnumberedPeerConfig> _interfaceNeighbors;
  private boolean _multipathEbgp;
  private MultipathEquivalentAsPathMatchMode _multipathEquivalentAsPathMatchMode;
  private boolean _multipathIbgp;
  private boolean _clusterListAsIbgpCost;

  private @Nonnull Map<Prefix, BgpAggregate> _aggregates;

  /**
   * A map of all non-dynamic bgp neighbors with which the router owning this process is configured
   * to peer, keyed by unique ID.
   */
  private @Nonnull Map<Ip, BgpActivePeerConfig> _activeNeighbors;

  /**
   * A map of all dynamic bgp neighbors with which the router owning this process is configured to
   * peer, keyed by unique ID.
   */
  private @Nonnull Map<Prefix, BgpPassivePeerConfig> _passiveNeighbors;

  /** Space of prefixes to be advertised using explicit network statements */
  private PrefixSpace _originationSpace;

  private final @Nonnull Ip _routerId;

  private BgpTieBreaker _tieBreaker;

  private @Nullable String _independentNetworkPolicy;

  private @Nullable String _mainRibIndependentNetworkPolicy;

  private @Nullable String _redistributionPolicy;

  private final @Nonnull LocalOriginationTypeTieBreaker _localOriginationTypeTieBreaker;
  private final @Nonnull NextHopIpTieBreaker _networkNextHopIpTieBreaker;
  private final @Nonnull NextHopIpTieBreaker _redistributeNextHopIpTieBreaker;

  private @Nonnull Set<String> _tracks;
  private @Nullable String _nextHopIpResolverRestrictionPolicy;

  /**
   * a list of prefixes from bgp network statements that will be unconditionally advertised if
   * _mainRibIndependentNetworkPolicy is set
   */
  private List<Prefix> _unconditionalNetworkStatements;

  private BgpProcess(
      @Nonnull Ip routerId,
      int ebgpAdminCost,
      int ibgpAdminCost,
      int localAdminCost,
      @Nullable Boolean clientToClientReflection,
      @Nullable BgpConfederation confederation,
      @Nullable String independentNetworkPolicy,
      @Nullable String mainRibIndependentNetworkPolicy,
      @Nullable String redistributionPolicy,
      @Nonnull LocalOriginationTypeTieBreaker localOriginationTypeTieBreaker,
      @Nonnull NextHopIpTieBreaker networkNextHopIpTieBreaker,
      @Nonnull NextHopIpTieBreaker redistributeNextHopIpTieBreaker) {
    _activeNeighbors = new HashMap<>();
    _aggregates = ImmutableMap.of();
    _clientToClientReflection = firstNonNull(clientToClientReflection, true);
    _confederation = confederation;
    _ebgpAdminCost = ebgpAdminCost;
    _ibgpAdminCost = ibgpAdminCost;
    _localAdminCost = localAdminCost;
    _interfaceNeighbors = new HashMap<>();
    _tieBreaker = BgpTieBreaker.ARRIVAL_ORDER;
    _clusterIds = Suppliers.memoize(new ClusterIdsSupplier());
    _originationSpace = new PrefixSpace();
    _passiveNeighbors = new HashMap<>();
    _routerId = routerId;
    _clusterListAsIbgpCost = false;
    _independentNetworkPolicy = independentNetworkPolicy;
    _mainRibIndependentNetworkPolicy = mainRibIndependentNetworkPolicy;
    _redistributionPolicy = redistributionPolicy;
    _localOriginationTypeTieBreaker = localOriginationTypeTieBreaker;
    _networkNextHopIpTieBreaker = networkNextHopIpTieBreaker;
    _redistributeNextHopIpTieBreaker = redistributeNextHopIpTieBreaker;
    _unconditionalNetworkStatements = new ArrayList<>();
    _tracks = ImmutableSet.of();
  }

  @JsonCreator
  private static BgpProcess create(
      @JsonProperty(PROP_ROUTER_ID) @Nullable Ip routerId,
      @JsonProperty(PROP_CLIENT_TO_CLIENT_REFLECTION) @Nullable Boolean clientToClientReflection,
      @JsonProperty(PROP_CONFEDERATION) @Nullable BgpConfederation confederation,
      @JsonProperty(PROP_EBGP_ADMIN_COST) @Nullable Integer ebgpAdminCost,
      @JsonProperty(PROP_IBGP_ADMIN_COST) @Nullable Integer ibgpAdminCost,
      @JsonProperty(PROP_LOCAL_ADMIN_COST) @Nullable Integer localAdminCost,
      @JsonProperty(PROP_INDEPENDENT_NETWORK_POLICY) @Nullable String networkPolicy,
      @JsonProperty(PROP_MAIN_RIB_INDEPENDENT_NETWORK_POLICY) @Nullable
          String mainRibIndependentNetworkPolicy,
      @JsonProperty(PROP_REDISTRIBUTION_POLICY) @Nullable String redistributionPolicy,
      @JsonProperty(PROP_LOCAL_ORIGINATION_TYPE_TIE_BREAKER) @Nullable
          LocalOriginationTypeTieBreaker localOriginationTypeTieBreaker,
      @JsonProperty(PROP_NETWORK_NEXT_HOP_IP_TIE_BREAKER) @Nullable
          NextHopIpTieBreaker networkNextHopIpTieBreaker,
      @JsonProperty(PROP_REDISTRIBUTE_NEXT_HOP_IP_TIE_BREAKER) @Nullable
          NextHopIpTieBreaker redistributeNextHopIpTieBreaker) {
    checkArgument(routerId != null, "Missing %s", PROP_ROUTER_ID);
    checkArgument(ebgpAdminCost != null, "Missing %s", PROP_EBGP_ADMIN_COST);
    checkArgument(ibgpAdminCost != null, "Missing %s", PROP_IBGP_ADMIN_COST);
    checkArgument(localAdminCost != null, "Missing %s", PROP_LOCAL_ADMIN_COST);
    checkArgument(
        localOriginationTypeTieBreaker != null,
        "Missing %s",
        PROP_LOCAL_ORIGINATION_TYPE_TIE_BREAKER);
    checkArgument(
        networkNextHopIpTieBreaker != null, "Missing %s", PROP_NETWORK_NEXT_HOP_IP_TIE_BREAKER);
    checkArgument(
        redistributeNextHopIpTieBreaker != null,
        "Missing %s",
        PROP_REDISTRIBUTE_NEXT_HOP_IP_TIE_BREAKER);
    // In the absence of provided values, default to Cisco IOS values
    return new BgpProcess(
        routerId,
        ebgpAdminCost,
        ibgpAdminCost,
        localAdminCost,
        clientToClientReflection,
        confederation,
        networkPolicy,
        mainRibIndependentNetworkPolicy,
        redistributionPolicy,
        localOriginationTypeTieBreaker,
        networkNextHopIpTieBreaker,
        redistributeNextHopIpTieBreaker);
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Add a {@link BgpAggregate}.
   *
   * @throws IllegalArgumentException if an entry already exists for the same network.
   */
  public void addAggregate(BgpAggregate aggregate) {
    Prefix network = aggregate.getNetwork();
    checkArgument(
        !_aggregates.containsKey(network),
        "Already contains an aggregate for network: %s",
        network);
    _aggregates =
        ImmutableMap.<Prefix, BgpAggregate>builderWithExpectedSize(_aggregates.size() + 1)
            .putAll(_aggregates)
            .put(network, aggregate)
            .build();
  }

  /**
   * Expand the origination space for this prefix
   *
   * @param space {@link PrefixSpace} to add
   */
  public void addToOriginationSpace(PrefixSpace space) {
    _originationSpace.addSpace(space);
  }

  /**
   * Expand the origination space for this prefix
   *
   * @param prefix {@link Prefix} to add
   */
  public void addToOriginationSpace(Prefix prefix) {
    _originationSpace.addPrefix(prefix);
  }

  /** Add a prefix announced in a network statement unconditionally (without consulting main RIB) */
  public void addUnconditionalNetworkStatements(Prefix prefix) {
    _unconditionalNetworkStatements.add(prefix);
  }

  /**
   * BGP Aggregates that may be generated for this process. Should only be populated on devices that
   * activate aggregates via entries from BGP RIB.
   */
  @JsonIgnore
  public @Nonnull Map<Prefix, BgpAggregate> getAggregates() {
    return _aggregates;
  }

  @JsonProperty(PROP_AGGREGATES)
  private @Nonnull List<BgpAggregate> getAggregatesList() {
    return ImmutableSortedMap.copyOf(_aggregates).values().asList();
  }

  @JsonProperty(PROP_AGGREGATES)
  private void setAggregates(List<BgpAggregate> aggregates) {
    _aggregates = toImmutableMap(aggregates, BgpAggregate::getNetwork, Function.identity());
  }

  /**
   * Returns set of all cluster IDs for all neighbors. The result is memoized, so this should only
   * be called after the neighbors are finalized.
   */
  @JsonIgnore
  public Set<Long> getClusterIds() {
    return _clusterIds.get();
  }

  /** Neighbor relationships configured for this BGP process. */
  @JsonIgnore
  public @Nonnull Map<Ip, BgpActivePeerConfig> getActiveNeighbors() {
    return _activeNeighbors;
  }

  @JsonProperty(PROP_ACTIVE_NEIGHBORS)
  private @Nonnull Map<Ip, BgpActivePeerConfig> getActiveNeighborsJson() {
    return ImmutableSortedMap.copyOf(_activeNeighbors);
  }

  /** Returns the admin cost of the given BGP protocol */
  @JsonIgnore
  public int getAdminCost(RoutingProtocol protocol) {
    switch (protocol) {
      case BGP:
        return _ebgpAdminCost;
      case IBGP:
        return _ibgpAdminCost;
      default:
        throw new IllegalArgumentException(String.format("Unrecognized BGP protocol %s", protocol));
    }
  }

  @JsonProperty(PROP_CLIENT_TO_CLIENT_REFLECTION)
  public boolean getClientToClientReflection() {
    return _clientToClientReflection;
  }

  @JsonProperty(PROP_CLIENT_TO_CLIENT_REFLECTION)
  public void setClientToClientReflection(boolean clientToClientReflection) {
    _clientToClientReflection = clientToClientReflection;
  }

  /** Return the global confederation config */
  @JsonProperty(PROP_CONFEDERATION)
  public @Nullable BgpConfederation getConfederation() {
    return _confederation;
  }

  public void setConfederation(@Nullable BgpConfederation confederation) {
    _confederation = confederation;
  }

  /** Returns the admin cost for eBGP routes in this process */
  @JsonProperty(PROP_EBGP_ADMIN_COST)
  public int getEbgpAdminCost() {
    return _ebgpAdminCost;
  }

  /** Returns the admin cost for iBGP routes in this process */
  @JsonProperty(PROP_IBGP_ADMIN_COST)
  public int getIbgpAdminCost() {
    return _ibgpAdminCost;
  }

  /** Returns the admin cost for iBGP routes in this process */
  @JsonProperty(PROP_LOCAL_ADMIN_COST)
  public int getLocalAdminCost() {
    return _localAdminCost;
  }

  /** Returns BGP unnumbered peer configurations keyed by peer-interface */
  @JsonIgnore
  public @Nonnull Map<String, BgpUnnumberedPeerConfig> getInterfaceNeighbors() {
    return _interfaceNeighbors;
  }

  @JsonProperty(PROP_INTERFACE_NEIGHBORS)
  private @Nonnull Map<String, BgpUnnumberedPeerConfig> getInterfaceNeighborsJson() {
    return ImmutableSortedMap.copyOf(_interfaceNeighbors);
  }

  @JsonProperty(PROP_MULTIPATH_EBGP)
  public boolean getMultipathEbgp() {
    return _multipathEbgp;
  }

  @JsonProperty(PROP_MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE)
  public MultipathEquivalentAsPathMatchMode getMultipathEquivalentAsPathMatchMode() {
    return _multipathEquivalentAsPathMatchMode;
  }

  @JsonProperty(PROP_MULTIPATH_IBGP)
  public boolean getMultipathIbgp() {
    return _multipathIbgp;
  }

  /** Neighbor relationships configured for this BGP process. */
  @JsonIgnore
  public @Nonnull Map<Prefix, BgpPassivePeerConfig> getPassiveNeighbors() {
    return _passiveNeighbors;
  }

  @JsonProperty(PROP_PASSIVE_NEIGHBORS)
  private @Nonnull Map<Prefix, BgpPassivePeerConfig> getPassiveNeighborsJson() {
    return ImmutableSortedMap.copyOf(_passiveNeighbors);
  }

  @JsonIgnore
  public PrefixSpace getOriginationSpace() {
    return _originationSpace;
  }

  @JsonIgnore
  public List<Prefix> getUnconditionalNetworkStatements() {
    return _unconditionalNetworkStatements;
  }

  /**
   * The configured router ID for this BGP process. Note that it can be overridden for individual
   * neighbors.
   */
  @JsonProperty(PROP_ROUTER_ID)
  public @Nonnull Ip getRouterId() {
    return _routerId;
  }

  @JsonProperty(PROP_TIE_BREAKER)
  public BgpTieBreaker getTieBreaker() {
    return _tieBreaker;
  }

  @JsonProperty(PROP_CLUSTER_LIST_AS_IBGP_COST)
  public boolean getClusterListAsIbgpCost() {
    return _clusterListAsIbgpCost;
  }

  /**
   * Return an iterable over all types of {@link BgpPeerConfig peer configurations} defined for this
   * process
   */
  @JsonIgnore
  public Iterable<BgpPeerConfig> getAllPeerConfigs() {
    return Iterables.concat(
        _activeNeighbors.values(), _passiveNeighbors.values(), _interfaceNeighbors.values());
  }

  /**
   * Return a stream over all types of {@link BgpPeerConfig peer configurations} defined for this
   * process
   */
  @JsonIgnore
  public Stream<BgpPeerConfig> allPeerConfigsStream() {
    return Streams.concat(
        _activeNeighbors.values().stream(),
        _passiveNeighbors.values().stream(),
        _interfaceNeighbors.values().stream());
  }

  @JsonProperty(PROP_INTERFACE_NEIGHBORS)
  public void setInterfaceNeighbors(
      @Nonnull Map<String, BgpUnnumberedPeerConfig> interfaceNeighbors) {
    _interfaceNeighbors = interfaceNeighbors;
  }

  @JsonProperty(PROP_MULTIPATH_EBGP)
  public void setMultipathEbgp(boolean multipathEbgp) {
    _multipathEbgp = multipathEbgp;
  }

  @JsonProperty(PROP_MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE)
  public void setMultipathEquivalentAsPathMatchMode(
      MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode) {
    _multipathEquivalentAsPathMatchMode = multipathEquivalentAsPathMatchMode;
  }

  @JsonProperty(PROP_MULTIPATH_IBGP)
  public void setMultipathIbgp(boolean multipathIbgp) {
    _multipathIbgp = multipathIbgp;
  }

  @JsonProperty(PROP_ACTIVE_NEIGHBORS)
  public void setNeighbors(Map<Ip, BgpActivePeerConfig> neighbors) {
    _activeNeighbors = firstNonNull(neighbors, new HashMap<>());
  }

  public void setOriginationSpace(PrefixSpace originationSpace) {
    _originationSpace = originationSpace;
  }

  @JsonProperty(PROP_PASSIVE_NEIGHBORS)
  public void setPassiveNeighbors(@Nullable Map<Prefix, BgpPassivePeerConfig> neighbors) {
    _passiveNeighbors = firstNonNull(neighbors, new HashMap<>());
  }

  @JsonProperty(PROP_TIE_BREAKER)
  public void setTieBreaker(BgpTieBreaker tieBreaker) {
    _tieBreaker = tieBreaker;
  }

  @JsonProperty(PROP_CLUSTER_LIST_AS_IBGP_COST)
  public void setClusterListAsIbgpCost(boolean clusterListAsIbgpCost) {
    _clusterListAsIbgpCost = clusterListAsIbgpCost;
  }

  @Deprecated // for old VI model with old JSON name.
  @JsonProperty(PROP_CLUSTER_LIST_AS_IGP_COST_DEPRECATED)
  private void setClusterListAsIbgpCostDeprecated(boolean clusterListAsIbgpCost) {
    setClusterListAsIbgpCost(clusterListAsIbgpCost);
  }

  /**
   * Name of the independent network origination policy for this process. This policy is expected to
   * be set only on vendors that export BGP from their BGP RIBs (Cisco-like behavior).
   *
   * <p>If non-null, indicates that {@code network} statements may redistribute routes independently
   * of {@code redistribute} statements, i.e. you may end up with up to two locally-originated
   * routes per prefix. Also in this case, {@link #getRedistributionPolicy()} must be non-null.
   *
   * <p>If {@code null}, indicates that either no networks are redistributed via {@code network}
   * statements, or networks are redistributed in the redistribution policy.redistributed routes
   * should not be added to the BGP RIB.
   */
  @JsonProperty(PROP_INDEPENDENT_NETWORK_POLICY)
  public @Nullable String getIndependentNetworkPolicy() {
    return _independentNetworkPolicy;
  }

  public void setIndependentNetworkPolicy(@Nullable String independentNetworkPolicy) {
    _independentNetworkPolicy = independentNetworkPolicy;
  }

  /**
   * Name of the main RIB independent network policy for this process. Set for vendors like FRR that
   * may announce networks via {@code network} statements regardless of if the route is present in
   * the main RIB.
   *
   * <p>If non-null, these networks will unquestionably be announced to BGP neighbors
   *
   * <p>If {@code null}, indicates that networks must be in the main RIB to be announced to BGP
   * neighbors.
   */
  @JsonProperty(PROP_MAIN_RIB_INDEPENDENT_NETWORK_POLICY)
  public @Nullable String getMainRibIndependentNetworkPolicy() {
    return _mainRibIndependentNetworkPolicy;
  }

  public void setMainRibIndependentNetworkPolicy(@Nullable String mainRibIndependentNetworkPolicy) {
    _mainRibIndependentNetworkPolicy = mainRibIndependentNetworkPolicy;
  }

  /**
   * Name of the redistribution policy for this process. This policy is expected to be set only on
   * vendors that export BGP from their BGP RIBs (Cisco-like behavior).
   *
   * <p>If {@code null}, indicates that redistributed routes should not be added to the BGP RIB. In
   * this case, any redistribution should be implemented directly in export policies.
   */
  @JsonProperty(PROP_REDISTRIBUTION_POLICY)
  public @Nullable String getRedistributionPolicy() {
    return _redistributionPolicy;
  }

  public void setRedistributionPolicy(@Nullable String redistributionPolicy) {
    _redistributionPolicy = redistributionPolicy;
  }

  /** Tie-breaking mode for two local routes of different origin mechanisms. */
  @JsonProperty(PROP_LOCAL_ORIGINATION_TYPE_TIE_BREAKER)
  public @Nonnull LocalOriginationTypeTieBreaker getLocalOriginationTypeTieBreaker() {
    return _localOriginationTypeTieBreaker;
  }

  /** Whether to prefer lowest or highest NHIP for network local routes. */
  @JsonProperty(PROP_NETWORK_NEXT_HOP_IP_TIE_BREAKER)
  public @Nonnull NextHopIpTieBreaker getNetworkNextHopIpTieBreaker() {
    return _networkNextHopIpTieBreaker;
  }

  /** Whether to prefer lowest or highest NHIP for redistribute local routes. */
  @JsonProperty(PROP_REDISTRIBUTE_NEXT_HOP_IP_TIE_BREAKER)
  public @Nonnull NextHopIpTieBreaker getRedistributeNextHopIpTieBreaker() {
    return _redistributeNextHopIpTieBreaker;
  }

  /** Names of tracks for which routes must be re-evaluated against policy upon a state change. */
  @JsonProperty(PROP_TRACKS)
  public @Nonnull Set<String> getTracks() {
    return _tracks;
  }

  @JsonProperty(PROP_TRACKS)
  public void setTracks(@Nonnull Set<String> tracks) {
    _tracks = tracks;
  }

  @JsonProperty(PROP_NEXT_HOP_IP_RESOLVER_RESTRICTION_POLICY)
  public @Nullable String getNextHopIpResolverRestrictionPolicy() {
    return _nextHopIpResolverRestrictionPolicy;
  }

  @JsonProperty(PROP_NEXT_HOP_IP_RESOLVER_RESTRICTION_POLICY)
  public void setNextHopIpResolverRestrictionPolicy(
      @Nullable String nextHopIpResolverRestrictionPolicy) {
    _nextHopIpResolverRestrictionPolicy = nextHopIpResolverRestrictionPolicy;
  }
}
