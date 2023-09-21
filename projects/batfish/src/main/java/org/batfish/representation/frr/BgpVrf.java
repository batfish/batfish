package org.batfish.representation.frr;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** BGP configuration for a particular VRF. */
public class BgpVrf implements Serializable {

  private boolean _defaultIpv4Unicast;
  private @Nullable Long _autonomousSystem;
  private @Nullable Boolean _asPathMultipathRelax;
  private final @Nonnull Map<String, BgpNeighbor> _neighbors;
  private @Nonnull Map<Prefix, BgpNetwork> _networks;
  private @Nullable BgpIpv4UnicastAddressFamily _ipv4Unicast;
  private @Nullable BgpL2vpnEvpnAddressFamily _l2VpnEvpn;
  private @Nullable Ip _routerId;
  private @Nullable Ip _clusterId;
  private final @Nonnull String _vrfName;
  private @Nullable Long _confederationId;
  private @Nullable Long _maxMedAdministrative;
  private final @Nonnull Map<FrrRoutingProtocol, BgpRedistributionPolicy> _redistributionPolicies;

  public BgpVrf(String vrfName) {
    // the default is true unless explicitly disabled (via "no bgp default ipv4-unicast")
    _defaultIpv4Unicast = true;
    _vrfName = vrfName;
    _neighbors = new HashMap<>();
    _networks = ImmutableMap.of();
    _redistributionPolicies = new TreeMap<>();
  }

  public boolean isIpv4UnicastActive() {
    return _defaultIpv4Unicast || (_ipv4Unicast != null);
  }

  public boolean getDefaultIpv4Unicast() {
    return _defaultIpv4Unicast;
  }

  public void setDefaultIpv4Unicast(boolean defaultIpv4Unicast) {
    _defaultIpv4Unicast = defaultIpv4Unicast;
  }

  public @Nullable Boolean getAsPathMultipathRelax() {
    return _asPathMultipathRelax;
  }

  public void setAsPathMultipathRelax(@Nullable Boolean asPathMultipathRelax) {
    _asPathMultipathRelax = asPathMultipathRelax;
  }

  public @Nullable Long getMaxMedAdministrative() {
    return _maxMedAdministrative;
  }

  public void setMaxMedAdministrative(@Nullable Long maxMedAdministrative) {
    _maxMedAdministrative = maxMedAdministrative;
  }

  public @Nullable Long getAutonomousSystem() {
    return _autonomousSystem;
  }

  public @Nonnull Map<String, BgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  public @Nullable BgpIpv4UnicastAddressFamily getIpv4Unicast() {
    return _ipv4Unicast;
  }

  public BgpIpv4UnicastAddressFamily getOrCreateIpv4Unicast() {
    if (_ipv4Unicast != null) {
      return _ipv4Unicast;
    }
    _ipv4Unicast = new BgpIpv4UnicastAddressFamily();
    return _ipv4Unicast;
  }

  public @Nullable BgpL2vpnEvpnAddressFamily getL2VpnEvpn() {
    return _l2VpnEvpn;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public @Nullable Ip getClusterId() {
    return _clusterId;
  }

  public @Nonnull String getVrfName() {
    return _vrfName;
  }

  public void setAutonomousSystem(@Nullable Long autonomousSystem) {
    _autonomousSystem = autonomousSystem;
  }

  public void setIpv4Unicast(@Nullable BgpIpv4UnicastAddressFamily ipv4Unicast) {
    _ipv4Unicast = ipv4Unicast;
  }

  public void setL2VpnEvpn(@Nullable BgpL2vpnEvpnAddressFamily l2VpnEvpn) {
    _l2VpnEvpn = l2VpnEvpn;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public void setClusterId(@Nullable Ip clusterId) {
    _clusterId = clusterId;
  }

  public @Nullable Long getConfederationId() {
    return _confederationId;
  }

  public void setConfederationId(@Nullable Long confederationId) {
    _confederationId = confederationId;
  }

  public @Nullable BgpNeighborIpv4UnicastAddressFamily getIpv4UnicastConfiguration(
      String neighbor) {
    return _ipv4Unicast == null ? null : _ipv4Unicast.getNeighbors().get(neighbor);
  }

  public @Nullable BgpNeighborL2vpnEvpnAddressFamily getL2EvpnConfiguration(String neighbor) {
    return _l2VpnEvpn == null ? null : _l2VpnEvpn.getNeighbors().get(neighbor);
  }

  public @Nonnull Map<Prefix, BgpNetwork> getNetworks() {
    return _networks;
  }

  public void addNetwork(BgpNetwork network) {
    _networks =
        ImmutableMap.<Prefix, BgpNetwork>builder()
            .putAll(_networks)
            .put(network.getNetwork(), network)
            .build();
  }

  public @Nonnull Map<FrrRoutingProtocol, BgpRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }
}
