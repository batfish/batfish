package org.batfish.representation.cisco_xr.eos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** BGP configuration for a single VRF */
public final class AristaBgpVrf implements Serializable {

  private boolean _defaultIpv4Unicast;
  @Nullable private Boolean _bestpathAsPathMultipathRelax;
  @Nullable private AristaBgpBestpathTieBreaker _bestpathTieBreaker;
  @Nullable private Long _defaultMetric;
  @Nullable private Integer _ebgpAdminDistance;
  @Nullable private ExtendedCommunity _exportRouteTarget;
  @Nullable private Integer _ibgpAdminDistance;
  @Nullable private ExtendedCommunity _importRouteTarget;
  @Nullable private Integer _holdTimer;
  @Nullable private Integer _keepAliveTimer;
  @Nullable private Integer _listenLimit;
  @Nullable private Integer _localAdminDistance;
  @Nullable private Long _localAs;
  @Nullable private Integer _maxPaths;
  @Nullable private Integer _maxPathsEcmp;
  @Nonnull private final String _name;

  @Nonnull
  private final Map<AristaRedistributeType, AristaBgpRedistributionPolicy> _redistributionPolicies;

  @Nullable private RouteDistinguisher _routeDistinguisher;
  @Nullable private Ip _routerId;
  @Nullable private Boolean _shutdown;
  @Nonnull private final Map<Prefix, AristaBgpAggregateNetwork> _v4aggregates;
  @Nonnull private final Map<Ip, AristaBgpV4Neighbor> _v4neighbors;
  @Nonnull private final Map<Prefix, AristaBgpV4DynamicNeighbor> _v4DynamicNeighbors;
  @Nullable private AristaBgpVrfIpv4UnicastAddressFamily _v4UnicastAf;
  @Nullable private AristaBgpVrfIpv6UnicastAddressFamily _v6UnicastAf;
  @Nullable private AristaBgpVrfEvpnAddressFamily _evpnAf;

  public AristaBgpVrf(String name) {
    _name = name;
    _defaultIpv4Unicast = true;
    _v4aggregates = new HashMap<>(0);
    _v4DynamicNeighbors = new HashMap<>(0);
    _v4neighbors = new HashMap<>(0);
    _redistributionPolicies = new HashMap<>(0);
  }

  public boolean getDefaultIpv4Unicast() {
    return _defaultIpv4Unicast;
  }

  public void setDefaultIpv4Unicast(boolean defaultIpv4Unicast) {
    _defaultIpv4Unicast = defaultIpv4Unicast;
  }

  @Nullable
  public Boolean getBestpathAsPathMultipathRelax() {
    return _bestpathAsPathMultipathRelax;
  }

  public void setBestpathAsPathMultipathRelax(@Nullable Boolean bestpathAsPathMultipathRelax) {
    _bestpathAsPathMultipathRelax = bestpathAsPathMultipathRelax;
  }

  @Nullable
  public AristaBgpBestpathTieBreaker getBestpathTieBreaker() {
    return _bestpathTieBreaker;
  }

  public void setBestpathTieBreaker(@Nullable AristaBgpBestpathTieBreaker bestpathTieBreaker) {
    _bestpathTieBreaker = bestpathTieBreaker;
  }

  @Nullable
  public Long getDefaultMetric() {
    return _defaultMetric;
  }

  public void setDefaultMetric(@Nullable Long defaultMetric) {
    _defaultMetric = defaultMetric;
  }

  @Nullable
  public Integer getEbgpAdminDistance() {
    return _ebgpAdminDistance;
  }

  public void setEbgpAdminDistance(@Nullable Integer ebgpAdminDistance) {
    _ebgpAdminDistance = ebgpAdminDistance;
  }

  @Nullable
  public Integer getIbgpAdminDistance() {
    return _ibgpAdminDistance;
  }

  public void setIbgpAdminDistance(@Nullable Integer ibgpAdminDistance) {
    _ibgpAdminDistance = ibgpAdminDistance;
  }

  @Nullable
  public Integer getLocalAdminDistance() {
    return _localAdminDistance;
  }

  public void setLocalAdminDistance(@Nullable Integer localAdminDistance) {
    _localAdminDistance = localAdminDistance;
  }

  @Nullable
  public ExtendedCommunity getExportRouteTarget() {
    return _exportRouteTarget;
  }

  public void setExportRouteTarget(@Nullable ExtendedCommunity exportRouteTarget) {
    _exportRouteTarget = exportRouteTarget;
  }

  @Nonnull
  public AristaBgpVrfEvpnAddressFamily getOrCreateEvpnAf() {
    if (_evpnAf == null) {
      _evpnAf = new AristaBgpVrfEvpnAddressFamily();
    }
    return _evpnAf;
  }

  @Nullable
  public AristaBgpVrfEvpnAddressFamily getEvpnAf() {
    return _evpnAf;
  }

  public void setEvpnAf(@Nullable AristaBgpVrfEvpnAddressFamily evpnAf) {
    _evpnAf = evpnAf;
  }

  @Nullable
  public ExtendedCommunity getImportRouteTarget() {
    return _importRouteTarget;
  }

  public void setImportRouteTarget(@Nullable ExtendedCommunity importRouteTarget) {
    _importRouteTarget = importRouteTarget;
  }

  /** Hold timer, in seconds */
  @Nullable
  public Integer getHoldTimer() {
    return _holdTimer;
  }

  /** Set the Hold timer, in seconds */
  public void setHoldTimer(@Nullable Integer holdTimer) {
    _holdTimer = holdTimer;
  }

  /** Keepalive timer, in seconds */
  @Nullable
  public Integer getKeepAliveTimer() {
    return _keepAliveTimer;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  /** Set the Keepalive timer, in seconds */
  public void setKeepAliveTimer(@Nullable Integer keepAliveTimer) {
    _keepAliveTimer = keepAliveTimer;
  }

  /** Maximum number of dynamic/passive connections */
  @Nullable
  public Integer getListenLimit() {
    return _listenLimit;
  }

  public AristaBgpVrf setListenLimit(@Nullable Integer listenLimit) {
    _listenLimit = listenLimit;
    return this;
  }

  @Nullable
  public Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  @Nullable
  public Integer getMaxPaths() {
    return _maxPaths;
  }

  public void setMaxPaths(@Nullable Integer maxPaths) {
    _maxPaths = maxPaths;
  }

  @Nullable
  public Integer getMaxPathsEcmp() {
    return _maxPathsEcmp;
  }

  public void setMaxPathsEcmp(@Nullable Integer maxPathsEcmp) {
    _maxPathsEcmp = maxPathsEcmp;
  }

  @Nonnull
  public Map<AristaRedistributeType, AristaBgpRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public void addRedistributionPolicy(AristaRedistributeType type, @Nullable String routeMap) {
    _redistributionPolicies.put(type, new AristaBgpRedistributionPolicy(type, routeMap));
  }

  @Nullable
  public RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  public void setRouteDistinguisher(@Nullable RouteDistinguisher routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
  }

  @Nullable
  public Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  @Nullable
  public Boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(@Nullable Boolean shutdown) {
    _shutdown = shutdown;
  }

  @Nonnull
  public Map<Prefix, AristaBgpAggregateNetwork> getV4aggregates() {
    return _v4aggregates;
  }

  @Nonnull
  public Map<Prefix, AristaBgpV4DynamicNeighbor> getV4DynamicNeighbors() {
    return _v4DynamicNeighbors;
  }

  @Nonnull
  public AristaBgpV4DynamicNeighbor getOrCreateV4DynamicNeighbor(Prefix prefix) {
    return _v4DynamicNeighbors.computeIfAbsent(prefix, AristaBgpV4DynamicNeighbor::new);
  }

  @Nonnull
  public Map<Ip, AristaBgpV4Neighbor> getV4neighbors() {
    return _v4neighbors;
  }

  @Nonnull
  public AristaBgpV4Neighbor getOrCreateV4Neighbor(Ip address) {
    return _v4neighbors.computeIfAbsent(address, AristaBgpV4Neighbor::new);
  }

  @Nullable
  public AristaBgpVrfIpv4UnicastAddressFamily getV4UnicastAf() {
    return _v4UnicastAf;
  }

  @Nonnull
  public AristaBgpVrfIpv4UnicastAddressFamily getOrCreateV4UnicastAf() {
    if (_v4UnicastAf == null) {
      _v4UnicastAf = new AristaBgpVrfIpv4UnicastAddressFamily();
    }
    return _v4UnicastAf;
  }

  public void setV4UnicastAf(@Nullable AristaBgpVrfIpv4UnicastAddressFamily v4UnicastAf) {
    _v4UnicastAf = v4UnicastAf;
  }

  @Nullable
  public AristaBgpVrfIpv6UnicastAddressFamily getV6UnicastAf() {
    return _v6UnicastAf;
  }

  @Nonnull
  public AristaBgpVrfIpv6UnicastAddressFamily getOrCreateV6UnicastAf() {
    if (_v6UnicastAf == null) {
      _v6UnicastAf = new AristaBgpVrfIpv6UnicastAddressFamily();
    }
    return _v6UnicastAf;
  }
}
