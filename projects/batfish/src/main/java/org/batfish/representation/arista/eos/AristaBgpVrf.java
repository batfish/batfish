package org.batfish.representation.arista.eos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** BGP configuration for a single VRF */
public final class AristaBgpVrf implements Serializable {

  private boolean _defaultIpv4Unicast;
  @Nullable private Boolean _advertiseInactive;
  @Nullable private Integer _allowAsIn;
  @Nullable private Boolean _alwaysCompareMed;
  @Nullable private Boolean _bestpathAsPathMultipathRelax;
  @Nullable private AristaBgpBestpathTieBreaker _bestpathTieBreaker;
  @Nullable private Ip _clusterId;
  @Nullable private Long _confederationIdentifier;
  @Nullable private LongSpace _confederationPeers;
  @Nullable private Long _defaultMetric;
  @Nullable private Integer _ebgpAdminDistance;
  @Nullable private Boolean _enforceFirstAs;
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
  @Nullable private Boolean _nextHopUnchanged;

  @Nonnull
  private final Map<AristaRedistributeType, AristaBgpRedistributionPolicy> _redistributionPolicies;

  @Nullable private RouteDistinguisher _routeDistinguisher;
  @Nullable private Ip _routerId;
  @Nullable private Boolean _shutdown;
  @Nonnull private final Map<Prefix, AristaBgpAggregateNetwork> _v4aggregates;
  @Nonnull private final Map<Prefix6, AristaBgpAggregateNetwork> _v6aggregates;
  @Nonnull private final Map<Ip, AristaBgpV4Neighbor> _v4neighbors;
  @Nonnull private final Map<Prefix, AristaBgpV4DynamicNeighbor> _v4DynamicNeighbors;

  @Nullable private AristaBgpVrfEvpnAddressFamily _evpnAf;

  @Nullable private AristaBgpVrfFlowSpecAddressFamily _flowSpecV4Af;
  @Nullable private AristaBgpVrfFlowSpecAddressFamily _flowSpecV6Af;

  // TODO: do these need to be different families, or 1 v4-specific but not unicast-specific?
  @Nullable private AristaBgpVrfIpv4UnicastAddressFamily _v4UnicastAf;
  @Nullable private AristaBgpVrfIpv4UnicastAddressFamily _v4LabeledUnicastAf;
  @Nullable private AristaBgpVrfIpv4UnicastAddressFamily _v4MulticastAf;
  @Nullable private AristaBgpVrfIpv4UnicastAddressFamily _v4SrTeAf;

  // TODO: do these need to be different families, or 1 v6-specific but not unicast-specific?
  @Nullable private AristaBgpVrfIpv6UnicastAddressFamily _v6LabeledUnicastAf;
  @Nullable private AristaBgpVrfIpv6UnicastAddressFamily _v6MulticastAf;
  @Nullable private AristaBgpVrfIpv6UnicastAddressFamily _v6UnicastAf;
  @Nullable private AristaBgpVrfIpv6UnicastAddressFamily _v6SrTeAf;

  @Nullable private AristaBgpVrfVpnAddressFamily _vpnV4Af;
  @Nullable private AristaBgpVrfVpnAddressFamily _vpnV6Af;

  public AristaBgpVrf(String name) {
    _name = name;
    _defaultIpv4Unicast = true;
    _v4aggregates = new HashMap<>(0);
    _v6aggregates = new HashMap<>(0);
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
  public Boolean getAdvertiseInactive() {
    return _advertiseInactive;
  }

  public void setAdvertiseInactive(@Nullable Boolean advertiseInactive) {
    _advertiseInactive = advertiseInactive;
  }

  @Nullable
  public Integer getAllowAsIn() {
    return _allowAsIn;
  }

  public void setAllowAsIn(@Nullable Integer allowAsIn) {
    _allowAsIn = allowAsIn;
  }

  @Nullable
  public Boolean getAlwaysCompareMed() {
    return _alwaysCompareMed;
  }

  public void setAlwaysCompareMed(@Nullable Boolean alwaysCompareMed) {
    _alwaysCompareMed = alwaysCompareMed;
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
  public Ip getClusterId() {
    return _clusterId;
  }

  public void setClusterId(@Nullable Ip clusterId) {
    _clusterId = clusterId;
  }

  @Nullable
  public Long getConfederationIdentifier() {
    return _confederationIdentifier;
  }

  public void setConfederationIdentifier(@Nullable Long confederationIdentifier) {
    _confederationIdentifier = confederationIdentifier;
  }

  @Nullable
  public LongSpace getConfederationPeers() {
    return _confederationPeers;
  }

  public void setConfederationPeers(@Nullable LongSpace confederationPeers) {
    _confederationPeers = confederationPeers;
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
  public Boolean getEnforceFirstAs() {
    return _enforceFirstAs;
  }

  public void setEnforceFirstAs(@Nullable Boolean enforceFirstAs) {
    _enforceFirstAs = enforceFirstAs;
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

  public @Nullable AristaBgpVrfFlowSpecAddressFamily getFlowSpecV4Af() {
    return _flowSpecV4Af;
  }

  public @Nonnull AristaBgpVrfFlowSpecAddressFamily getOrCreateFlowSpecV4Af() {
    if (_flowSpecV4Af == null) {
      _flowSpecV4Af = new AristaBgpVrfFlowSpecAddressFamily();
    }
    return _flowSpecV4Af;
  }

  public @Nullable AristaBgpVrfFlowSpecAddressFamily getFlowSpecV6Af() {
    return _flowSpecV6Af;
  }

  public @Nonnull AristaBgpVrfFlowSpecAddressFamily getOrCreateFlowSpecV6Af() {
    if (_flowSpecV6Af == null) {
      _flowSpecV6Af = new AristaBgpVrfFlowSpecAddressFamily();
    }
    return _flowSpecV6Af;
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

  @Nullable
  public Boolean getNextHopUnchanged() {
    return _nextHopUnchanged;
  }

  public void setNextHopUnchanged(@Nullable Boolean nextHopUnchanged) {
    _nextHopUnchanged = nextHopUnchanged;
  }

  @Nonnull
  public Map<AristaRedistributeType, AristaBgpRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public void addRedistributionPolicy(AristaRedistributeType type, @Nullable String routeMap) {
    _redistributionPolicies.put(type, new AristaBgpRedistributionPolicy(type, routeMap));
  }

  public void removeRedistributionPolicy(AristaRedistributeType type) {
    _redistributionPolicies.remove(type);
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
  public Map<Prefix6, AristaBgpAggregateNetwork> getV6aggregates() {
    return _v6aggregates;
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

  @Nonnull
  public AristaBgpVrfIpv4UnicastAddressFamily getOrCreateV4MulticastAf() {
    if (_v4MulticastAf == null) {
      _v4MulticastAf = new AristaBgpVrfIpv4UnicastAddressFamily();
    }
    return _v4MulticastAf;
  }

  @Nullable
  public AristaBgpVrfIpv4UnicastAddressFamily getV4MulticastAf() {
    return _v4MulticastAf;
  }

  @Nonnull
  public AristaBgpVrfIpv4UnicastAddressFamily getOrCreateV4SrTeAf() {
    if (_v4SrTeAf == null) {
      _v4SrTeAf = new AristaBgpVrfIpv4UnicastAddressFamily();
    }
    return _v4SrTeAf;
  }

  @Nullable
  public AristaBgpVrfIpv4UnicastAddressFamily getV4SrTeAf() {
    return _v4SrTeAf;
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

  @Nullable
  public AristaBgpVrfIpv4UnicastAddressFamily getV4LabeledUnicastAf() {
    return _v4LabeledUnicastAf;
  }

  @Nonnull
  public AristaBgpVrfIpv4UnicastAddressFamily getOrCreateV4LabeledUnicastAf() {
    if (_v4LabeledUnicastAf == null) {
      _v4LabeledUnicastAf = new AristaBgpVrfIpv4UnicastAddressFamily();
    }
    return _v4LabeledUnicastAf;
  }

  @Nonnull
  public AristaBgpVrfIpv6UnicastAddressFamily getOrCreateV6LabeledUnicastAf() {
    if (_v6LabeledUnicastAf == null) {
      _v6LabeledUnicastAf = new AristaBgpVrfIpv6UnicastAddressFamily();
    }
    return _v6LabeledUnicastAf;
  }

  @Nullable
  public AristaBgpVrfIpv6UnicastAddressFamily getV6LabeledUnicastAf() {
    return _v6LabeledUnicastAf;
  }

  @Nonnull
  public AristaBgpVrfIpv6UnicastAddressFamily getOrCreateV6MulticastAf() {
    if (_v6MulticastAf == null) {
      _v6MulticastAf = new AristaBgpVrfIpv6UnicastAddressFamily();
    }
    return _v6MulticastAf;
  }

  @Nullable
  public AristaBgpVrfIpv6UnicastAddressFamily getV6MulticastAf() {
    return _v6MulticastAf;
  }

  @Nonnull
  public AristaBgpVrfIpv6UnicastAddressFamily getOrCreateV6SrTeAf() {
    if (_v6SrTeAf == null) {
      _v6SrTeAf = new AristaBgpVrfIpv6UnicastAddressFamily();
    }
    return _v6SrTeAf;
  }

  @Nullable
  public AristaBgpVrfIpv6UnicastAddressFamily getV6SrTeAf() {
    return _v6SrTeAf;
  }

  @Nonnull
  public AristaBgpVrfIpv6UnicastAddressFamily getOrCreateV6UnicastAf() {
    if (_v6UnicastAf == null) {
      _v6UnicastAf = new AristaBgpVrfIpv6UnicastAddressFamily();
    }
    return _v6UnicastAf;
  }

  @Nullable
  public AristaBgpVrfIpv6UnicastAddressFamily getV6UnicastAf() {
    return _v6UnicastAf;
  }

  @Nullable
  public AristaBgpVrfVpnAddressFamily getVpnV4Af() {
    return _vpnV4Af;
  }

  @Nonnull
  public AristaBgpVrfVpnAddressFamily getOrCreateVpnV4Af() {
    if (_vpnV4Af == null) {
      _vpnV4Af = new AristaBgpVrfVpnAddressFamily();
    }
    return _vpnV4Af;
  }

  @Nullable
  public AristaBgpVrfVpnAddressFamily getVpnV6Af() {
    return _vpnV6Af;
  }

  @Nonnull
  public AristaBgpVrfVpnAddressFamily getOrCreateVpnV6Af() {
    if (_vpnV6Af == null) {
      _vpnV6Af = new AristaBgpVrfVpnAddressFamily();
    }
    return _vpnV6Af;
  }
}
