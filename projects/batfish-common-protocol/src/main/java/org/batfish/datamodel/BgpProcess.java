package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.BgpConfederation;

/** Represents a bgp process on a router */
public class BgpProcess implements Serializable {

  public static class Builder {

    @Nullable private BgpConfederation _confederation;
    @Nullable private Integer _ebgpAdminCost;
    @Nullable private Integer _ibgpAdminCost;
    @Nullable private Ip _routerId;
    @Nullable private Vrf _vrf;

    public BgpProcess build() {
      checkArgument(_routerId != null, "Missing %s", PROP_ROUTER_ID);
      checkArgument(_ebgpAdminCost != null, "Missing %s", PROP_EBGP_ADMIN_COST);
      checkArgument(_ibgpAdminCost != null, "Missing %s", PROP_IBGP_ADMIN_COST);
      BgpProcess bgpProcess =
          new BgpProcess(_routerId, _ebgpAdminCost, _ibgpAdminCost, _confederation);
      if (_vrf != null) {
        _vrf.setBgpProcess(bgpProcess);
      }
      return bgpProcess;
    }

    @Nonnull
    public Builder setConfederation(@Nullable BgpConfederation confederation) {
      _confederation = confederation;
      return this;
    }

    /**
     * Sets {@link #setEbgpAdminCost(int) ebgpAdminCost} and {@link #setIbgpAdminCost(int)
     * ibgpAdminCost} to default BGP administrative costs for the given {@link ConfigurationFormat}.
     */
    @Nonnull
    public Builder setAdminCostsToVendorDefaults(@Nonnull ConfigurationFormat format) {
      return setEbgpAdminCost(RoutingProtocol.BGP.getDefaultAdministrativeCost(format))
          .setIbgpAdminCost(RoutingProtocol.IBGP.getDefaultAdministrativeCost(format));
    }

    @Nonnull
    public Builder setEbgpAdminCost(int ebgpAdminCost) {
      _ebgpAdminCost = ebgpAdminCost;
      return this;
    }

    @Nonnull
    public Builder setIbgpAdminCost(int ibgpAdminCost) {
      _ibgpAdminCost = ibgpAdminCost;
      return this;
    }

    @Nonnull
    public Builder setRouterId(Ip routerId) {
      _routerId = routerId;
      return this;
    }

    @Nonnull
    public Builder setVrf(Vrf vrf) {
      _vrf = vrf;
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

  private static final String PROP_CONFEDERATION = "confederation";
  private static final String PROP_EBGP_ADMIN_COST = "ebgpAdminCost";
  private static final String PROP_IBGP_ADMIN_COST = "ibgpAdminCost";
  private static final String PROP_INTERFACE_NEIGHBORS = "interfaceNeighbors";
  private static final String PROP_PASSIVE_NEIGHBORS = "dynamicNeighbors";
  private static final String PROP_MULTIPATH_EBGP = "multipathEbgp";
  private static final String PROP_MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE =
      "multipathEquivalentAsPathMatchMode";
  private static final String PROP_MULTIPATH_IBGP = "multipathIbgp";
  private static final String PROP_ACTIVE_NEIGHBORS = "neighbors";
  private static final String PROP_ROUTER_ID = "routerId";
  private static final String PROP_TIE_BREAKER = "tieBreaker";
  private static final String PROP_CLUSTER_LIST_AS_IGP_COST = "clusterListAsIgpCost";

  @Nullable private BgpConfederation _confederation;
  private final int _ebgpAdminCost;
  private final int _ibgpAdminCost;
  private Supplier<Set<Long>> _clusterIds;
  @Nonnull private SortedMap<String, BgpUnnumberedPeerConfig> _interfaceNeighbors;
  private boolean _multipathEbgp;
  private MultipathEquivalentAsPathMatchMode _multipathEquivalentAsPathMatchMode;
  private boolean _multipathIbgp;
  private boolean _clusterListAsIbgpCost;

  /**
   * A map of all non-dynamic bgp neighbors with which the router owning this process is configured
   * to peer, keyed by unique ID.
   */
  @Nonnull private SortedMap<Prefix, BgpActivePeerConfig> _activeNeighbors;

  /**
   * A map of all dynamic bgp neighbors with which the router owning this process is configured to
   * peer, keyed by unique ID.
   */
  @Nonnull private SortedMap<Prefix, BgpPassivePeerConfig> _passiveNeighbors;

  /** Space of prefixes to be advertised using explicit network statements */
  private PrefixSpace _originationSpace;

  @Nonnull private final Ip _routerId;

  private BgpTieBreaker _tieBreaker;

  /**
   * Constructs a BgpProcess with the default admin costs for the given {@link ConfigurationFormat},
   * for convenient creation of BGP processes in tests.
   */
  @VisibleForTesting
  public BgpProcess(@Nonnull Ip routerId, @Nonnull ConfigurationFormat configurationFormat) {
    this(
        routerId,
        RoutingProtocol.BGP.getDefaultAdministrativeCost(configurationFormat),
        RoutingProtocol.IBGP.getDefaultAdministrativeCost(configurationFormat));
  }

  /** Constructs a BgpProcess with the given router ID and admin costs */
  public BgpProcess(@Nonnull Ip routerId, int ebgpAdminCost, int ibgpAdminCost) {
    this(routerId, ebgpAdminCost, ibgpAdminCost, null);
  }

  private BgpProcess(
      @Nonnull Ip routerId,
      int ebgpAdminCost,
      int ibgpAdminCost,
      @Nullable BgpConfederation confederation) {
    _activeNeighbors = new TreeMap<>();
    _confederation = confederation;
    _ebgpAdminCost = ebgpAdminCost;
    _ibgpAdminCost = ibgpAdminCost;
    _interfaceNeighbors = new TreeMap<>();
    _tieBreaker = BgpTieBreaker.ARRIVAL_ORDER;
    _clusterIds = new ClusterIdsSupplier();
    _originationSpace = new PrefixSpace();
    _passiveNeighbors = new TreeMap<>();
    _routerId = routerId;
    _clusterListAsIbgpCost = false;
  }

  @JsonCreator
  private static BgpProcess create(
      @Nullable @JsonProperty(PROP_ROUTER_ID) Ip routerId,
      @Nullable @JsonProperty(PROP_CONFEDERATION) BgpConfederation confederation,
      @Nullable @JsonProperty(PROP_EBGP_ADMIN_COST) Integer ebgpAdminCost,
      @Nullable @JsonProperty(PROP_IBGP_ADMIN_COST) Integer ibgpAdminCost) {
    checkArgument(routerId != null, "Missing %s", routerId);
    // In the absence of provided values, default to Cisco IOS values
    return new BgpProcess(
        routerId,
        firstNonNull(
            ebgpAdminCost,
            RoutingProtocol.BGP.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS)),
        firstNonNull(
            ibgpAdminCost,
            RoutingProtocol.IBGP.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS)),
        confederation);
  }

  public static Builder builder() {
    return new Builder();
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

  /**
   * Returns set of all cluster IDs for all neighbors. The result is memoized, so this should only
   * be called after the neighbors are finalized.
   */
  @JsonIgnore
  public Set<Long> getClusterIds() {
    return _clusterIds.get();
  }

  /** Neighbor relationships configured for this BGP process. */
  @JsonProperty(PROP_ACTIVE_NEIGHBORS)
  @Nonnull
  public SortedMap<Prefix, BgpActivePeerConfig> getActiveNeighbors() {
    return _activeNeighbors;
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

  /** Return the global confederation config */
  @Nullable
  @JsonProperty(PROP_CONFEDERATION)
  public BgpConfederation getConfederation() {
    return _confederation;
  }

  public void setConfederation(@Nullable BgpConfederation confederation) {
    _confederation = confederation;
  }

  /** Returns the admin cost for eBGP routes in this process */
  @JsonProperty(PROP_EBGP_ADMIN_COST)
  private int getEbgpAdminCost() {
    return _ebgpAdminCost;
  }

  /** Returns the admin cost for iBGP routes in this process */
  @JsonProperty(PROP_IBGP_ADMIN_COST)
  private int getIbgpAdminCost() {
    return _ibgpAdminCost;
  }

  /** Returns BGP unnumbered peer configurations keyed by peer-interface */
  @JsonProperty(PROP_INTERFACE_NEIGHBORS)
  @Nonnull
  public SortedMap<String, BgpUnnumberedPeerConfig> getInterfaceNeighbors() {
    return _interfaceNeighbors;
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
  @JsonProperty(PROP_PASSIVE_NEIGHBORS)
  @Nonnull
  public SortedMap<Prefix, BgpPassivePeerConfig> getPassiveNeighbors() {
    return _passiveNeighbors;
  }

  @JsonIgnore
  public PrefixSpace getOriginationSpace() {
    return _originationSpace;
  }

  /**
   * The configured router ID for this BGP process. Note that it can be overridden for individual
   * neighbors.
   */
  @Nonnull
  @JsonProperty(PROP_ROUTER_ID)
  public Ip getRouterId() {
    return _routerId;
  }

  @JsonProperty(PROP_TIE_BREAKER)
  public BgpTieBreaker getTieBreaker() {
    return _tieBreaker;
  }

  @JsonProperty(PROP_CLUSTER_LIST_AS_IGP_COST)
  public boolean getClusterListAsIgpCost() {
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
      @Nonnull SortedMap<String, BgpUnnumberedPeerConfig> interfaceNeighbors) {
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
  public void setNeighbors(SortedMap<Prefix, BgpActivePeerConfig> neighbors) {
    _activeNeighbors = firstNonNull(neighbors, new TreeMap<>());
  }

  public void setOriginationSpace(PrefixSpace originationSpace) {
    _originationSpace = originationSpace;
  }

  @JsonProperty(PROP_PASSIVE_NEIGHBORS)
  public void setPassiveNeighbors(@Nullable SortedMap<Prefix, BgpPassivePeerConfig> neighbors) {
    _passiveNeighbors = firstNonNull(neighbors, new TreeMap<>());
  }

  @JsonProperty(PROP_TIE_BREAKER)
  public void setTieBreaker(BgpTieBreaker tieBreaker) {
    _tieBreaker = tieBreaker;
  }

  @JsonProperty(PROP_CLUSTER_LIST_AS_IGP_COST)
  public void setClusterListAsIgpCost(boolean clusterListAsIbgpCost) {
    _clusterListAsIbgpCost = clusterListAsIbgpCost;
  }
}
