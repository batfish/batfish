package org.batfish.representation.cisco.nx;

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
public final class CiscoNxBgpGlobalConfiguration implements Serializable {
  private static final long serialVersionUID = 1L;

  public CiscoNxBgpGlobalConfiguration() {
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

  public CiscoNxBgpVrfNeighborConfiguration getOrCreateTemplatePeer(String name) {
    return _templatePeers.computeIfAbsent(name, n -> new CiscoNxBgpVrfNeighborConfiguration());
  }

  @Nullable
  CiscoNxBgpVrfNeighborConfiguration getTemplatePeer(String name) {
    return _templatePeers.get(name);
  }

  public CiscoNxBgpVrfNeighborAddressFamilyConfiguration getOrCreateTemplatePeerPolicy(
      String name) {
    return _templatePeerPolicies.computeIfAbsent(
        name, n -> new CiscoNxBgpVrfNeighborAddressFamilyConfiguration());
  }

  @Nullable
  CiscoNxBgpVrfNeighborAddressFamilyConfiguration getTemplatePeerPolicy(String name) {
    return _templatePeerPolicies.get(name);
  }

  public CiscoNxBgpVrfNeighborConfiguration getOrCreateTemplatePeerSession(String name) {
    return _templatePeerSessions.computeIfAbsent(
        name, n -> new CiscoNxBgpVrfNeighborConfiguration());
  }

  @Nullable
  CiscoNxBgpVrfNeighborConfiguration getTemplatePeerSession(String name) {
    return _templatePeerSessions.get(name);
  }

  public void doInherit(Warnings warnings) {
    _templatePeers.values().forEach(peer -> peer.doInherit(this, warnings));
    _templatePeerPolicies.values().forEach(policy -> policy.doInherit(this, warnings));
    _templatePeerSessions.values().forEach(session -> session.doInherit(this, warnings));
  }

  private boolean _enforceFirstAs;
  private long _localAs;
  private final Map<String, CiscoNxBgpVrfNeighborConfiguration> _templatePeers;
  private final Map<String, CiscoNxBgpVrfNeighborAddressFamilyConfiguration> _templatePeerPolicies;
  private final Map<String, CiscoNxBgpVrfNeighborConfiguration> _templatePeerSessions;
}
