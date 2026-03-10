package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;

/**
 * Represents the BGP configuration for a single neighbor at the VRF level.
 *
 * <p>Configuration commands entered at the CLI {@code config-router-neighbor} or {@code
 * config-router-vrf-neighbor} levels.
 */
public class BgpVrfNeighborConfiguration implements Serializable {

  /**
   * Determines whether to remote private AS numbers from AS paths ({@link #ALL}) or replace them
   * with the local AS number ({@link #REPLACE_AS}).
   */
  public enum RemovePrivateAsMode {
    ALL,
    REPLACE_AS
  }

  public BgpVrfNeighborConfiguration() {
    _addressFamilies = new HashMap<>();
  }

  public BgpVrfNeighborAddressFamilyConfiguration getOrCreateAddressFamily(String af) {
    return _addressFamilies.computeIfAbsent(
        af, a -> new BgpVrfNeighborAddressFamilyConfiguration());
  }

  public @Nullable BgpVrfNeighborAddressFamilyConfiguration getIpv4UnicastAddressFamily() {
    return _addressFamilies.get("ipv4-unicast");
  }

  public @Nullable BgpVrfNeighborAddressFamilyConfiguration getIpv6UnicastAddressFamily() {
    return _addressFamilies.get("ipv6-unicast");
  }

  public @Nullable BgpVrfNeighborAddressFamilyConfiguration getL2VpnEvpnAddressFamily() {
    return _addressFamilies.get("l2vpn-evpn");
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nullable Integer getEbgpMultihopTtl() {
    return _ebgpMultihopTtl;
  }

  public void setEbgpMultihopTtl(@Nullable Integer ttl) {
    _ebgpMultihopTtl = ttl;
  }

  public @Nullable String getInheritPeer() {
    return _inheritPeer;
  }

  public void setInheritPeer(@Nullable String peer) {
    _inheritPeer = peer;
  }

  public void setInheritPeerSession(@Nullable String peerSession) {
    _inheritPeerSession = peerSession;
  }

  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  public @Nullable Long getRemoteAs() {
    return _remoteAs;
  }

  public void setRemoteAs(@Nullable Long remoteAs) {
    _remoteAs = remoteAs;
  }

  public @Nullable String getRemoteAsRouteMap() {
    return _remoteAsRouteMap;
  }

  public void setRemoteAsRouteMap(@Nullable String remoteAsRouteMap) {
    _remoteAsRouteMap = remoteAsRouteMap;
  }

  public @Nullable RemovePrivateAsMode getRemovePrivateAs() {
    return _removePrivateAs;
  }

  public void setRemovePrivateAs(@Nullable RemovePrivateAsMode mode) {
    _removePrivateAs = mode;
  }

  public @Nullable Boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(@Nullable Boolean shutdown) {
    _shutdown = shutdown;
  }

  public @Nullable String getUpdateSource() {
    return _updateSource;
  }

  public void setUpdateSource(@Nullable String updateSource) {
    _updateSource = updateSource;
  }

  private final Map<String, BgpVrfNeighborAddressFamilyConfiguration> _addressFamilies;
  private @Nullable String _description;
  private @Nullable Integer _ebgpMultihopTtl;
  private @Nullable String _inheritPeer;
  private @Nullable String _inheritPeerSession;
  private @Nullable Long _localAs;
  private @Nullable Long _remoteAs;
  private @Nullable String _remoteAsRouteMap;
  private @Nullable RemovePrivateAsMode _removePrivateAs;
  private @Nullable Boolean _shutdown;
  private @Nullable String _updateSource;

  private void inheritFrom(
      BgpGlobalConfiguration nxBgpGlobal, Warnings warnings, BgpVrfNeighborConfiguration peer) {
    peer.doInherit(nxBgpGlobal, warnings);
    if (_description == null) {
      _description = peer._description;
    }
    if (_ebgpMultihopTtl == null) {
      _ebgpMultihopTtl = peer._ebgpMultihopTtl;
    }
    if (_localAs == null) {
      _localAs = peer._localAs;
    }
    if (_remoteAs == null) {
      _remoteAs = peer._remoteAs;
    }
    if (_remoteAsRouteMap == null) {
      _remoteAsRouteMap = peer._remoteAsRouteMap;
    }
    if (_removePrivateAs == null) {
      _removePrivateAs = peer._removePrivateAs;
    }
    if (_shutdown == null) {
      _shutdown = peer._shutdown;
    }
    if (_updateSource == null) {
      _updateSource = peer._updateSource;
    }
  }

  public void doInherit(BgpGlobalConfiguration nxBgpGlobal, Warnings warnings) {
    if (_doneInheriting) {
      return;
    }
    // Mark inherited first, for loop prevention.
    _doneInheriting = true;

    BgpVrfNeighborConfiguration peerSession =
        _inheritPeerSession != null
            ? nxBgpGlobal.getTemplatePeerSession(_inheritPeerSession)
            : null;
    if (peerSession != null) {
      inheritFrom(nxBgpGlobal, warnings, peerSession);
    }

    BgpVrfNeighborConfiguration peer =
        _inheritPeer != null ? nxBgpGlobal.getTemplatePeer(_inheritPeer) : null;
    if (peer != null) {
      inheritFrom(nxBgpGlobal, warnings, peer);

      for (Map.Entry<String, BgpVrfNeighborAddressFamilyConfiguration> af :
          peer._addressFamilies.entrySet()) {
        getOrCreateAddressFamily(af.getKey()).inheritFrom(nxBgpGlobal, warnings, af.getValue());
      }
    }

    for (BgpVrfNeighborAddressFamilyConfiguration af : _addressFamilies.values()) {
      af.doInherit(nxBgpGlobal, warnings);
    }
  }

  private boolean _doneInheriting;
}
