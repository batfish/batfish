package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;

/**
 * Represents the global BGP configuration for Cisco NX-OS.
 *
 * <p>Configuration commands entered at the {@code config-router} level that cannot also be run in a
 * {@code config-router-vrf} level are global to the BGP configuration at the device level.
 */
public final class BgpGlobalConfiguration implements Serializable {

  public BgpGlobalConfiguration() {
    _enforceFirstAs = false; // disabled by default
    _templatePeers = new HashMap<>();
    _templatePeerPolicies = new HashMap<>();
    _templatePeerSessions = new HashMap<>();
    _vrfs = new HashMap<>();
  }

  public boolean getEnforceFirstAs() {
    return _enforceFirstAs;
  }

  public void setEnforceFirstAs(boolean enforceFirstAs) {
    _enforceFirstAs = enforceFirstAs;
  }

  public long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(long localAs) {
    _localAs = localAs;
  }

  /** A read-only map containing the per-VRF BGP configuration. */
  public Map<String, BgpVrfConfiguration> getVrfs() {
    return Collections.unmodifiableMap(_vrfs);
  }

  public @Nonnull BgpVrfConfiguration getOrCreateVrf(String vrfName) {
    return _vrfs.computeIfAbsent(vrfName, name -> new BgpVrfConfiguration());
  }

  public BgpVrfNeighborConfiguration getOrCreateTemplatePeer(String name) {
    return _templatePeers.computeIfAbsent(name, n -> new BgpVrfNeighborConfiguration());
  }

  @Nullable
  BgpVrfNeighborConfiguration getTemplatePeer(String name) {
    return _templatePeers.get(name);
  }

  public @Nonnull Map<String, BgpVrfNeighborConfiguration> getTemplatePeers() {
    return Collections.unmodifiableMap(_templatePeers);
  }

  public BgpVrfNeighborAddressFamilyConfiguration getOrCreateTemplatePeerPolicy(String name) {
    return _templatePeerPolicies.computeIfAbsent(
        name, n -> new BgpVrfNeighborAddressFamilyConfiguration());
  }

  @Nullable
  BgpVrfNeighborAddressFamilyConfiguration getTemplatePeerPolicy(String name) {
    return _templatePeerPolicies.get(name);
  }

  public BgpVrfNeighborConfiguration getOrCreateTemplatePeerSession(String name) {
    return _templatePeerSessions.computeIfAbsent(name, n -> new BgpVrfNeighborConfiguration());
  }

  @Nullable
  BgpVrfNeighborConfiguration getTemplatePeerSession(String name) {
    return _templatePeerSessions.get(name);
  }

  public void doInherit(Warnings warnings) {
    _templatePeers.forEach((name, config) -> config.doInherit(this, warnings));
    _templatePeerPolicies.forEach((name, config) -> config.doInherit(this, warnings));
    _templatePeerSessions.forEach((name, config) -> config.doInherit(this, warnings));
  }

  private boolean _enforceFirstAs;
  private long _localAs;
  private final Map<String, BgpVrfNeighborConfiguration> _templatePeers;
  private final Map<String, BgpVrfNeighborAddressFamilyConfiguration> _templatePeerPolicies;
  private final Map<String, BgpVrfNeighborConfiguration> _templatePeerSessions;
  private final Map<String, BgpVrfConfiguration> _vrfs;
}
