package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import java.util.EnumMap;
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
  private @Nullable Boolean _advertiseInactive;
  private @Nullable Integer _allowAsIn;
  private @Nullable Boolean _alwaysCompareMed;
  private @Nullable Boolean _bestpathAsPathMultipathRelax;
  private @Nullable AristaBgpBestpathTieBreaker _bestpathTieBreaker;

  private @Nullable Boolean _clientToClientReflection;
  private @Nullable Ip _clusterId;
  private @Nullable Long _confederationIdentifier;
  private @Nullable LongSpace _confederationPeers;
  private @Nullable Long _defaultMetric;
  private @Nullable Integer _ebgpAdminDistance;
  private @Nullable Boolean _enforceFirstAs;
  private @Nullable ExtendedCommunity _exportRouteTarget;
  private @Nullable Integer _ibgpAdminDistance;
  private @Nullable ExtendedCommunity _importRouteTarget;
  private @Nullable Integer _holdTimer;
  private @Nullable Integer _keepAliveTimer;
  private @Nullable Integer _listenLimit;
  private @Nullable Integer _localAdminDistance;
  private @Nullable Long _localAs;
  private @Nullable Integer _maxPaths;
  private @Nullable Integer _maxPathsEcmp;
  private final @Nonnull String _name;
  private @Nullable Boolean _nextHopUnchanged;

  private final @Nonnull Map<AristaRedistributeType, AristaBgpRedistributionPolicy>
      _redistributionPolicies;

  private @Nullable RouteDistinguisher _routeDistinguisher;
  private @Nullable Ip _routerId;
  private @Nullable Boolean _shutdown;
  private final @Nonnull Map<Prefix, AristaBgpAggregateNetwork> _v4aggregates;
  private final @Nonnull Map<Prefix6, AristaBgpAggregateNetwork> _v6aggregates;
  private final @Nonnull Map<Ip, AristaBgpV4Neighbor> _v4neighbors;
  private final @Nonnull Map<Prefix, AristaBgpV4DynamicNeighbor> _v4DynamicNeighbors;

  private @Nullable AristaBgpVrfEvpnAddressFamily _evpnAf;

  private @Nullable AristaBgpVrfFlowSpecAddressFamily _flowSpecV4Af;
  private @Nullable AristaBgpVrfFlowSpecAddressFamily _flowSpecV6Af;

  // TODO: do these need to be different families, or 1 v4-specific but not unicast-specific?
  private @Nullable AristaBgpVrfIpv4UnicastAddressFamily _v4UnicastAf;
  private @Nullable AristaBgpVrfIpv4UnicastAddressFamily _v4LabeledUnicastAf;
  private @Nullable AristaBgpVrfIpv4UnicastAddressFamily _v4MulticastAf;
  private @Nullable AristaBgpVrfIpv4UnicastAddressFamily _v4SrTeAf;

  // TODO: do these need to be different families, or 1 v6-specific but not unicast-specific?
  private @Nullable AristaBgpVrfIpv6UnicastAddressFamily _v6LabeledUnicastAf;
  private @Nullable AristaBgpVrfIpv6UnicastAddressFamily _v6MulticastAf;
  private @Nullable AristaBgpVrfIpv6UnicastAddressFamily _v6UnicastAf;
  private @Nullable AristaBgpVrfIpv6UnicastAddressFamily _v6SrTeAf;

  private @Nullable AristaBgpVrfVpnAddressFamily _vpnV4Af;
  private @Nullable AristaBgpVrfVpnAddressFamily _vpnV6Af;

  public AristaBgpVrf(String name) {
    _name = name;
    _defaultIpv4Unicast = true;
    _v4aggregates = new HashMap<>(0);
    _v6aggregates = new HashMap<>(0);
    _v4DynamicNeighbors = new HashMap<>(0);
    _v4neighbors = new HashMap<>(0);
    _redistributionPolicies = new EnumMap<>(AristaRedistributeType.class);
  }

  public boolean getDefaultIpv4Unicast() {
    return _defaultIpv4Unicast;
  }

  public void setDefaultIpv4Unicast(boolean defaultIpv4Unicast) {
    _defaultIpv4Unicast = defaultIpv4Unicast;
  }

  public @Nullable Boolean getAdvertiseInactive() {
    return _advertiseInactive;
  }

  public void setAdvertiseInactive(@Nullable Boolean advertiseInactive) {
    _advertiseInactive = advertiseInactive;
  }

  public @Nullable Integer getAllowAsIn() {
    return _allowAsIn;
  }

  public void setAllowAsIn(@Nullable Integer allowAsIn) {
    _allowAsIn = allowAsIn;
  }

  public @Nullable Boolean getAlwaysCompareMed() {
    return _alwaysCompareMed;
  }

  public void setAlwaysCompareMed(@Nullable Boolean alwaysCompareMed) {
    _alwaysCompareMed = alwaysCompareMed;
  }

  public @Nullable Boolean getBestpathAsPathMultipathRelax() {
    return _bestpathAsPathMultipathRelax;
  }

  public void setBestpathAsPathMultipathRelax(@Nullable Boolean bestpathAsPathMultipathRelax) {
    _bestpathAsPathMultipathRelax = bestpathAsPathMultipathRelax;
  }

  public @Nullable AristaBgpBestpathTieBreaker getBestpathTieBreaker() {
    return _bestpathTieBreaker;
  }

  public void setBestpathTieBreaker(@Nullable AristaBgpBestpathTieBreaker bestpathTieBreaker) {
    _bestpathTieBreaker = bestpathTieBreaker;
  }

  public @Nullable Boolean getClientToClientReflection() {
    return _clientToClientReflection;
  }

  public void setClientToClientReflection(@Nullable Boolean clientToClientReflection) {
    _clientToClientReflection = clientToClientReflection;
  }

  public @Nullable Ip getClusterId() {
    return _clusterId;
  }

  public void setClusterId(@Nullable Ip clusterId) {
    _clusterId = clusterId;
  }

  public @Nullable Long getConfederationIdentifier() {
    return _confederationIdentifier;
  }

  public void setConfederationIdentifier(@Nullable Long confederationIdentifier) {
    _confederationIdentifier = confederationIdentifier;
  }

  public @Nullable LongSpace getConfederationPeers() {
    return _confederationPeers;
  }

  public void setConfederationPeers(@Nullable LongSpace confederationPeers) {
    _confederationPeers = confederationPeers;
  }

  public @Nullable Long getDefaultMetric() {
    return _defaultMetric;
  }

  public void setDefaultMetric(@Nullable Long defaultMetric) {
    _defaultMetric = defaultMetric;
  }

  public @Nullable Integer getEbgpAdminDistance() {
    return _ebgpAdminDistance;
  }

  public void setEbgpAdminDistance(@Nullable Integer ebgpAdminDistance) {
    _ebgpAdminDistance = ebgpAdminDistance;
  }

  public @Nullable Boolean getEnforceFirstAs() {
    return _enforceFirstAs;
  }

  public void setEnforceFirstAs(@Nullable Boolean enforceFirstAs) {
    _enforceFirstAs = enforceFirstAs;
  }

  public @Nullable Integer getIbgpAdminDistance() {
    return _ibgpAdminDistance;
  }

  public void setIbgpAdminDistance(@Nullable Integer ibgpAdminDistance) {
    _ibgpAdminDistance = ibgpAdminDistance;
  }

  public @Nullable Integer getLocalAdminDistance() {
    return _localAdminDistance;
  }

  public void setLocalAdminDistance(@Nullable Integer localAdminDistance) {
    _localAdminDistance = localAdminDistance;
  }

  public @Nullable ExtendedCommunity getExportRouteTarget() {
    return _exportRouteTarget;
  }

  public void setExportRouteTarget(@Nullable ExtendedCommunity exportRouteTarget) {
    _exportRouteTarget = exportRouteTarget;
  }

  public @Nonnull AristaBgpVrfEvpnAddressFamily getOrCreateEvpnAf() {
    if (_evpnAf == null) {
      _evpnAf = new AristaBgpVrfEvpnAddressFamily();
    }
    return _evpnAf;
  }

  public @Nullable AristaBgpVrfEvpnAddressFamily getEvpnAf() {
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

  public @Nullable ExtendedCommunity getImportRouteTarget() {
    return _importRouteTarget;
  }

  public void setImportRouteTarget(@Nullable ExtendedCommunity importRouteTarget) {
    _importRouteTarget = importRouteTarget;
  }

  /** Hold timer, in seconds */
  public @Nullable Integer getHoldTimer() {
    return _holdTimer;
  }

  /** Set the Hold timer, in seconds */
  public void setHoldTimer(@Nullable Integer holdTimer) {
    _holdTimer = holdTimer;
  }

  /** Keepalive timer, in seconds */
  public @Nullable Integer getKeepAliveTimer() {
    return _keepAliveTimer;
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** Set the Keepalive timer, in seconds */
  public void setKeepAliveTimer(@Nullable Integer keepAliveTimer) {
    _keepAliveTimer = keepAliveTimer;
  }

  /** Maximum number of dynamic/passive connections */
  public @Nullable Integer getListenLimit() {
    return _listenLimit;
  }

  public AristaBgpVrf setListenLimit(@Nullable Integer listenLimit) {
    _listenLimit = listenLimit;
    return this;
  }

  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  public @Nullable Integer getMaxPaths() {
    return _maxPaths;
  }

  public void setMaxPaths(@Nullable Integer maxPaths) {
    _maxPaths = maxPaths;
  }

  public @Nullable Integer getMaxPathsEcmp() {
    return _maxPathsEcmp;
  }

  public void setMaxPathsEcmp(@Nullable Integer maxPathsEcmp) {
    _maxPathsEcmp = maxPathsEcmp;
  }

  public @Nullable Boolean getNextHopUnchanged() {
    return _nextHopUnchanged;
  }

  public void setNextHopUnchanged(@Nullable Boolean nextHopUnchanged) {
    _nextHopUnchanged = nextHopUnchanged;
  }

  public @Nonnull Map<AristaRedistributeType, AristaBgpRedistributionPolicy>
      getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public void addRedistributionPolicy(AristaRedistributeType type, @Nullable String routeMap) {
    _redistributionPolicies.put(type, new AristaBgpRedistributionPolicy(type, routeMap));
  }

  public void removeRedistributionPolicy(AristaRedistributeType type) {
    _redistributionPolicies.remove(type);
  }

  public @Nullable RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  public void setRouteDistinguisher(@Nullable RouteDistinguisher routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public @Nullable Boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(@Nullable Boolean shutdown) {
    _shutdown = shutdown;
  }

  public @Nonnull Map<Prefix, AristaBgpAggregateNetwork> getV4aggregates() {
    return _v4aggregates;
  }

  public @Nonnull Map<Prefix6, AristaBgpAggregateNetwork> getV6aggregates() {
    return _v6aggregates;
  }

  public @Nonnull Map<Prefix, AristaBgpV4DynamicNeighbor> getV4DynamicNeighbors() {
    return _v4DynamicNeighbors;
  }

  public @Nonnull AristaBgpV4DynamicNeighbor getOrCreateV4DynamicNeighbor(Prefix prefix) {
    return _v4DynamicNeighbors.computeIfAbsent(prefix, AristaBgpV4DynamicNeighbor::new);
  }

  public @Nonnull Map<Ip, AristaBgpV4Neighbor> getV4neighbors() {
    return _v4neighbors;
  }

  public @Nonnull AristaBgpV4Neighbor getOrCreateV4Neighbor(Ip address) {
    return _v4neighbors.computeIfAbsent(address, AristaBgpV4Neighbor::new);
  }

  public @Nonnull AristaBgpVrfIpv4UnicastAddressFamily getOrCreateV4MulticastAf() {
    if (_v4MulticastAf == null) {
      _v4MulticastAf = new AristaBgpVrfIpv4UnicastAddressFamily();
    }
    return _v4MulticastAf;
  }

  public @Nullable AristaBgpVrfIpv4UnicastAddressFamily getV4MulticastAf() {
    return _v4MulticastAf;
  }

  public @Nonnull AristaBgpVrfIpv4UnicastAddressFamily getOrCreateV4SrTeAf() {
    if (_v4SrTeAf == null) {
      _v4SrTeAf = new AristaBgpVrfIpv4UnicastAddressFamily();
    }
    return _v4SrTeAf;
  }

  public @Nullable AristaBgpVrfIpv4UnicastAddressFamily getV4SrTeAf() {
    return _v4SrTeAf;
  }

  public @Nullable AristaBgpVrfIpv4UnicastAddressFamily getV4UnicastAf() {
    return _v4UnicastAf;
  }

  public @Nonnull AristaBgpVrfIpv4UnicastAddressFamily getOrCreateV4UnicastAf() {
    if (_v4UnicastAf == null) {
      _v4UnicastAf = new AristaBgpVrfIpv4UnicastAddressFamily();
    }
    return _v4UnicastAf;
  }

  public @Nullable AristaBgpVrfIpv4UnicastAddressFamily getV4LabeledUnicastAf() {
    return _v4LabeledUnicastAf;
  }

  public @Nonnull AristaBgpVrfIpv4UnicastAddressFamily getOrCreateV4LabeledUnicastAf() {
    if (_v4LabeledUnicastAf == null) {
      _v4LabeledUnicastAf = new AristaBgpVrfIpv4UnicastAddressFamily();
    }
    return _v4LabeledUnicastAf;
  }

  public @Nonnull AristaBgpVrfIpv6UnicastAddressFamily getOrCreateV6LabeledUnicastAf() {
    if (_v6LabeledUnicastAf == null) {
      _v6LabeledUnicastAf = new AristaBgpVrfIpv6UnicastAddressFamily();
    }
    return _v6LabeledUnicastAf;
  }

  public @Nullable AristaBgpVrfIpv6UnicastAddressFamily getV6LabeledUnicastAf() {
    return _v6LabeledUnicastAf;
  }

  public @Nonnull AristaBgpVrfIpv6UnicastAddressFamily getOrCreateV6MulticastAf() {
    if (_v6MulticastAf == null) {
      _v6MulticastAf = new AristaBgpVrfIpv6UnicastAddressFamily();
    }
    return _v6MulticastAf;
  }

  public @Nullable AristaBgpVrfIpv6UnicastAddressFamily getV6MulticastAf() {
    return _v6MulticastAf;
  }

  public @Nonnull AristaBgpVrfIpv6UnicastAddressFamily getOrCreateV6SrTeAf() {
    if (_v6SrTeAf == null) {
      _v6SrTeAf = new AristaBgpVrfIpv6UnicastAddressFamily();
    }
    return _v6SrTeAf;
  }

  public @Nullable AristaBgpVrfIpv6UnicastAddressFamily getV6SrTeAf() {
    return _v6SrTeAf;
  }

  public @Nonnull AristaBgpVrfIpv6UnicastAddressFamily getOrCreateV6UnicastAf() {
    if (_v6UnicastAf == null) {
      _v6UnicastAf = new AristaBgpVrfIpv6UnicastAddressFamily();
    }
    return _v6UnicastAf;
  }

  public @Nullable AristaBgpVrfIpv6UnicastAddressFamily getV6UnicastAf() {
    return _v6UnicastAf;
  }

  public @Nullable AristaBgpVrfVpnAddressFamily getVpnV4Af() {
    return _vpnV4Af;
  }

  public @Nonnull AristaBgpVrfVpnAddressFamily getOrCreateVpnV4Af() {
    if (_vpnV4Af == null) {
      _vpnV4Af = new AristaBgpVrfVpnAddressFamily();
    }
    return _vpnV4Af;
  }

  public @Nullable AristaBgpVrfVpnAddressFamily getVpnV6Af() {
    return _vpnV6Af;
  }

  public @Nonnull AristaBgpVrfVpnAddressFamily getOrCreateVpnV6Af() {
    if (_vpnV6Af == null) {
      _vpnV6Af = new AristaBgpVrfVpnAddressFamily();
    }
    return _vpnV6Af;
  }
}
