package org.batfish.representation.cisco_nxos;

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

  @Nullable
  public BgpVrfNeighborAddressFamilyConfiguration getIpv4UnicastAddressFamily() {
    return _addressFamilies.get("ipv4-unicast");
  }

  @Nullable
  public BgpVrfNeighborAddressFamilyConfiguration getIpv6UnicastAddressFamily() {
    return _addressFamilies.get("ipv6-unicast");
  }

  @Nullable
  public BgpVrfNeighborAddressFamilyConfiguration getL2VpnEvpnAddressFamily() {
    return _addressFamilies.get("l2vpn-evpn");
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  @Nullable
  public Integer getEbgpMultihopTtl() {
    return _ebgpMultihopTtl;
  }

  public void setEbgpMultihopTtl(@Nullable Integer ttl) {
    _ebgpMultihopTtl = ttl;
  }

  @Nullable
  public String getInheritPeer() {
    return _inheritPeer;
  }

  public void setInheritPeer(@Nullable String peer) {
    _inheritPeer = peer;
  }

  public void setInheritPeerSession(@Nullable String peerSession) {
    _inheritPeerSession = peerSession;
  }

  @Nullable
  public Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  @Nullable
  public Long getRemoteAs() {
    return _remoteAs;
  }

  public void setRemoteAs(@Nullable Long remoteAs) {
    _remoteAs = remoteAs;
  }

  @Nullable
  public String getRemoteAsRouteMap() {
    return _remoteAsRouteMap;
  }

  public void setRemoteAsRouteMap(@Nullable String remoteAsRouteMap) {
    _remoteAsRouteMap = remoteAsRouteMap;
  }

  @Nullable
  public RemovePrivateAsMode getRemovePrivateAs() {
    return _removePrivateAs;
  }

  public void setRemovePrivateAs(@Nullable RemovePrivateAsMode mode) {
    _removePrivateAs = mode;
  }

  @Nullable
  public Boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(@Nullable Boolean shutdown) {
    _shutdown = shutdown;
  }

  @Nullable
  public String getUpdateSource() {
    return _updateSource;
  }

  public void setUpdateSource(@Nullable String updateSource) {
    _updateSource = updateSource;
  }

  private final Map<String, BgpVrfNeighborAddressFamilyConfiguration> _addressFamilies;
  @Nullable private String _description;
  @Nullable private Integer _ebgpMultihopTtl;
  @Nullable private String _inheritPeer;
  @Nullable private String _inheritPeerSession;
  @Nullable private Long _localAs;
  @Nullable private Long _remoteAs;
  @Nullable private String _remoteAsRouteMap;
  @Nullable private RemovePrivateAsMode _removePrivateAs;
  @Nullable private Boolean _shutdown;
  @Nullable private String _updateSource;

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
