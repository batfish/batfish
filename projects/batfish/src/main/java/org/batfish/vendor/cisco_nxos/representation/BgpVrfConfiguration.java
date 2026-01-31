package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.vendor.cisco_nxos.representation.BgpVrfAddressFamilyConfiguration.Type;

/**
 * Represents the top-level configuration of a VRF in a BGP process for Cisco NX-OS.
 *
 * <p>Configuration commands that can be entered at the {@code config-router} (for default VRF) or
 * {@code config-router-vrf} levels are VRF-specific BGP configuration.
 */
public final class BgpVrfConfiguration implements Serializable {

  public BgpVrfConfiguration() {
    _addressFamilies = new EnumMap<>(Type.class); // all address families disabled by default
    _clusterId = null; // route reflection is disabled by default.
    _logNeighborChanges = false; // disabled by default
    _maxAsLimit = null; // default no limit
    _neighbors = new HashMap<>(); // no neighbors by default.
    _neighbors6 = new HashMap<>(); // no neighbors by default.
    _passiveNeighbors = new HashMap<>(); // no neighbors by default.
    _passiveNeighbors6 = new HashMap<>(); // no neighbors by default.
    _routerId = null; // use device's default router id unless overridden.
  }

  public @Nullable BgpVrfIpv4AddressFamilyConfiguration getIpv4UnicastAddressFamily() {
    return (BgpVrfIpv4AddressFamilyConfiguration) _addressFamilies.get(Type.IPV4_UNICAST);
  }

  public @Nullable BgpVrfIpv6AddressFamilyConfiguration getIpv6UnicastAddressFamily() {
    return (BgpVrfIpv6AddressFamilyConfiguration) _addressFamilies.get(Type.IPV6_UNICAST);
  }

  public @Nullable BgpVrfL2VpnEvpnAddressFamilyConfiguration getL2VpnEvpnAddressFamily() {
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

  public boolean removeNeighbor(Ip address) {
    return _neighbors.remove(address) != null;
  }

  public boolean removeNeighbor(Ip6 address) {
    return _neighbors6.remove(address) != null;
  }

  public boolean removePassiveNeighbor(Prefix prefix) {
    return _passiveNeighbors.remove(prefix) != null;
  }

  public boolean removePassiveNeighbor(Prefix6 prefix) {
    return _passiveNeighbors6.remove(prefix) != null;
  }

  public Map<Ip, BgpVrfNeighborConfiguration> getNeighbors() {
    return Collections.unmodifiableMap(_neighbors);
  }

  @VisibleForTesting // IPv6 neighbors not supported past extraction
  public Map<Ip6, BgpVrfNeighborConfiguration> getNeighbors6() {
    return _neighbors6;
  }

  public Map<Prefix, BgpVrfNeighborConfiguration> getPassiveNeighbors() {
    return Collections.unmodifiableMap(_passiveNeighbors);
  }

  @VisibleForTesting // IPv6 neighbors not supported past extraction
  public Map<Prefix6, BgpVrfNeighborConfiguration> getPassiveNeighbors6() {
    return _passiveNeighbors6;
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

  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(long localAs) {
    _localAs = localAs;
  }

  public @Nullable Ip getClusterId() {
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

  public @Nullable Integer getMaxasLimit() {
    return _maxAsLimit;
  }

  public void setMaxasLimit(@Nullable Integer maxAsLimit) {
    _maxAsLimit = maxAsLimit;
  }

  public @Nullable Ip getRouterId() {
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
  private @Nullable Ip _clusterId;
  private @Nullable Long _localAs;
  private boolean _logNeighborChanges;
  private @Nullable Integer _maxAsLimit;
  private final Map<Ip, BgpVrfNeighborConfiguration> _neighbors;
  private final Map<Ip6, BgpVrfNeighborConfiguration> _neighbors6;
  private final Map<Prefix, BgpVrfNeighborConfiguration> _passiveNeighbors;
  private final Map<Prefix6, BgpVrfNeighborConfiguration> _passiveNeighbors6;
  private @Nullable Ip _routerId;
}
