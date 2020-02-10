package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.representation.cisco_nxos.BgpVrfAddressFamilyConfiguration.Type;

/**
 * Represents the top-level configuration of a VRF in a BGP process for Cisco NX-OS.
 *
 * <p>Configuration commands that can be entered at the {@code config-router} (for default VRF) or
 * {@code config-router-vrf} levels are VRF-specific BGP configuration.
 */
public final class BgpVrfConfiguration implements Serializable {

  public BgpVrfConfiguration() {
    _addressFamilies = new HashMap<>(); // all address families disabled by default
    _clusterId = null; // route reflection is disabled by default.
    _logNeighborChanges = false; // disabled by default
    _maxAsLimit = null; // default no limit
    _neighbors = new HashMap<>(); // no neighbors by default.
    _neighbors6 = new HashMap<>(); // no neighbors by default.
    _passiveNeighbors = new HashMap<>(); // no neighbors by default.
    _passiveNeighbors6 = new HashMap<>(); // no neighbors by default.
    _routerId = null; // use device's default router id unless overridden.
  }

  @Nullable
  public BgpVrfIpv4AddressFamilyConfiguration getIpv4UnicastAddressFamily() {
    return (BgpVrfIpv4AddressFamilyConfiguration) _addressFamilies.get(Type.IPV4_UNICAST);
  }

  @Nullable
  public BgpVrfIpv6AddressFamilyConfiguration getIpv6UnicastAddressFamily() {
    return (BgpVrfIpv6AddressFamilyConfiguration) _addressFamilies.get(Type.IPV6_UNICAST);
  }

  @Nullable
  public BgpVrfL2VpnEvpnAddressFamilyConfiguration getL2VpnEvpnAddressFamily() {
    return (BgpVrfL2VpnEvpnAddressFamilyConfiguration) _addressFamilies.get(Type.L2VPN_EVPN);
  }

  public BgpVrfAddressFamilyConfiguration getOrCreateAddressFamily(
      BgpVrfAddressFamilyConfiguration.Type af) {
    switch (af) {
      case L2VPN_EVPN:
        return _addressFamilies.computeIfAbsent(
            af, a -> new BgpVrfL2VpnEvpnAddressFamilyConfiguration());
      case IPV4_MULTICAST:
      case IPV4_UNICAST:
        return _addressFamilies.computeIfAbsent(
            af, a -> new BgpVrfIpv4AddressFamilyConfiguration());
      case IPV6_MULTICAST:
      case IPV6_UNICAST:
        return _addressFamilies.computeIfAbsent(
            af, a -> new BgpVrfIpv6AddressFamilyConfiguration());
      default:
        // Dummy.
        return new BgpVrfAddressFamilyConfiguration() {};
    }
  }

  public BgpVrfNeighborConfiguration getOrCreateNeighbor(Ip address) {
    return _neighbors.computeIfAbsent(address, a -> new BgpVrfNeighborConfiguration());
  }

  public BgpVrfNeighborConfiguration getOrCreateNeighbor(Ip6 address) {
    return _neighbors6.computeIfAbsent(address, a -> new BgpVrfNeighborConfiguration());
  }

  public BgpVrfNeighborConfiguration getOrCreatePassiveNeighbor(Prefix prefix) {
    return _passiveNeighbors.computeIfAbsent(prefix, p -> new BgpVrfNeighborConfiguration());
  }

  public BgpVrfNeighborConfiguration getOrCreatePassiveNeighbor(Prefix6 prefix) {
    return _passiveNeighbors6.computeIfAbsent(prefix, p -> new BgpVrfNeighborConfiguration());
  }

  public Map<Ip, BgpVrfNeighborConfiguration> getNeighbors() {
    return Collections.unmodifiableMap(_neighbors);
  }

  public Map<Prefix, BgpVrfNeighborConfiguration> getPassiveNeighbors() {
    return Collections.unmodifiableMap(_passiveNeighbors);
  }

  public boolean getBestpathAlwaysCompareMed() {
    return _bestpathAlwaysCompareMed;
  }

  public void setBestpathAlwaysCompareMed(boolean bestpathAlwaysCompareMed) {
    _bestpathAlwaysCompareMed = bestpathAlwaysCompareMed;
  }

  public boolean getBestpathAsPathMultipathRelax() {
    return _bestpathAsPathMultipathRelax;
  }

  public void setBestpathAsPathMultipathRelax(boolean bestpathAsPathMultipathRelax) {
    _bestpathAsPathMultipathRelax = bestpathAsPathMultipathRelax;
  }

  public boolean getBestpathCompareRouterId() {
    return _bestpathCompareRouterId;
  }

  public void setBestpathCompareRouterId(boolean bestpathCompareRouterId) {
    _bestpathCompareRouterId = bestpathCompareRouterId;
  }

  public boolean getBestpathCostCommunityIgnore() {
    return _bestpathCostCommunityIgnore;
  }

  public void setBestpathCostCommunityIgnore(boolean bestpathCostCommunityIgnore) {
    _bestpathCostCommunityIgnore = bestpathCostCommunityIgnore;
  }

  public boolean getBestpathMedConfed() {
    return _bestpathMedConfed;
  }

  public void setBestpathMedConfed(boolean bestpathMedConfed) {
    _bestpathMedConfed = bestpathMedConfed;
  }

  public boolean getBestpathMedMissingAsWorst() {
    return _bestpathMedMissingAsWorst;
  }

  public void setBestpathMedMissingAsWorst(boolean bestpathMedMissingAsWorst) {
    _bestpathMedMissingAsWorst = bestpathMedMissingAsWorst;
  }

  public boolean getBestpathMedNonDeterministic() {
    return _bestpathMedNonDeterministic;
  }

  public void setBestpathMedNonDeterministic(boolean bestpathMedNonDeterministic) {
    _bestpathMedNonDeterministic = bestpathMedNonDeterministic;
  }

  @Nullable
  public Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(long localAs) {
    _localAs = localAs;
  }

  @Nullable
  public Ip getClusterId() {
    return _clusterId;
  }

  public void setClusterId(Ip clusterId) {
    _clusterId = clusterId;
  }

  public boolean getLogNeighborChanges() {
    return _logNeighborChanges;
  }

  public void setLogNeighborChanges(boolean log) {
    _logNeighborChanges = log;
  }

  @Nullable
  public Integer getMaxasLimit() {
    return _maxAsLimit;
  }

  public void setMaxasLimit(@Nullable Integer maxAsLimit) {
    _maxAsLimit = maxAsLimit;
  }

  @Nullable
  public Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  private final Map<Type, BgpVrfAddressFamilyConfiguration> _addressFamilies;
  private boolean _bestpathAlwaysCompareMed;
  private boolean _bestpathAsPathMultipathRelax;
  private boolean _bestpathCompareRouterId;
  private boolean _bestpathCostCommunityIgnore;
  private boolean _bestpathMedConfed;
  private boolean _bestpathMedMissingAsWorst;
  private boolean _bestpathMedNonDeterministic;
  @Nullable private Ip _clusterId;
  @Nullable private Long _localAs;
  private boolean _logNeighborChanges;
  @Nullable private Integer _maxAsLimit;
  private final Map<Ip, BgpVrfNeighborConfiguration> _neighbors;
  private final Map<Ip6, BgpVrfNeighborConfiguration> _neighbors6;
  private final Map<Prefix, BgpVrfNeighborConfiguration> _passiveNeighbors;
  private final Map<Prefix6, BgpVrfNeighborConfiguration> _passiveNeighbors6;
  @Nullable private Ip _routerId;
}
