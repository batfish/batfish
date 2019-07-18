package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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
  }

  public boolean getEnforceFirstAs() {
    return _enforceFirstAs;
  }

  public void setEnforceFirstAs(boolean enforceFirstAs) {
    this._enforceFirstAs = enforceFirstAs;
  }

  public long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(long localAs) {
    this._localAs = localAs;
  }

  public BgpVrfNeighborConfiguration getOrCreateTemplatePeer(String name) {
    return _templatePeers.computeIfAbsent(name, n -> new BgpVrfNeighborConfiguration());
  }

  @Nullable
  BgpVrfNeighborConfiguration getTemplatePeer(String name) {
    return _templatePeers.get(name);
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
    _templatePeers.values().forEach(peer -> peer.doInherit(this, warnings));
    _templatePeerPolicies.values().forEach(policy -> policy.doInherit(this, warnings));
    _templatePeerSessions.values().forEach(session -> session.doInherit(this, warnings));
  }

  private boolean _enforceFirstAs;
  private long _localAs;
  private final Map<String, BgpVrfNeighborConfiguration> _templatePeers;
  private final Map<String, BgpVrfNeighborAddressFamilyConfiguration> _templatePeerPolicies;
  private final Map<String, BgpVrfNeighborConfiguration> _templatePeerSessions;
}
