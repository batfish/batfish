package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Base class for all Arista BGP neighbors */
public abstract class AristaBgpNeighbor implements Serializable {
  public static final long SYSTEM_DEFAULT_LOCALPREF = 100;

  public enum RemovePrivateAsMode {
    NONE,
    BASIC,
    ALL,
    REPLACE_AS,
  }

  private @Nullable Integer _allowAsIn;
  private @Nullable Boolean _autoLocalAddr;
  private @Nullable AristaBgpDefaultOriginate _defaultOriginate;
  private @Nullable String _description;
  private @Nullable Boolean _dontCapabilityNegotiate;
  private @Nullable Integer _ebgpMultihop;
  private @Nullable Boolean _enforceFirstAs;
  private @Nullable Long _exportLocalPref;
  private @Nullable Long _localAs;
  private @Nullable Boolean _nextHopPeer;
  private @Nullable Boolean _nextHopSelf;
  private @Nullable Boolean _nextHopUnchanged;
  // Whether this neighbor will initiate a BGP connection. If false, will listen only.
  private @Nullable Boolean _passive;
  private @Nullable Long _remoteAs;
  private @Nullable RemovePrivateAsMode _removePrivateAsMode;
  private @Nullable Boolean _routeReflectorClient;
  private @Nullable Boolean _sendCommunity;
  private @Nullable Boolean _sendExtendedCommunity;
  private @Nullable Boolean _shutdown;
  private @Nullable String _updateSource;
  private @Nonnull AristaBgpNeighborAddressFamily _genericAddressFamily;

  public @Nullable Integer getAllowAsIn() {
    return _allowAsIn;
  }

  public void setAllowAsIn(@Nullable Integer allowAsIn) {
    _allowAsIn = allowAsIn;
  }

  public @Nullable Boolean getAutoLocalAddr() {
    return _autoLocalAddr;
  }

  public void setAutoLocalAddr(@Nullable Boolean autoLocalAddr) {
    _autoLocalAddr = autoLocalAddr;
  }

  public @Nullable AristaBgpDefaultOriginate getDefaultOriginate() {
    return _defaultOriginate;
  }

  public void setDefaultOriginate(@Nullable AristaBgpDefaultOriginate defaultOriginate) {
    _defaultOriginate = defaultOriginate;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nullable Boolean getDontCapabilityNegotiate() {
    return _dontCapabilityNegotiate;
  }

  public void setDontCapabilityNegotiate(@Nullable Boolean dontCapabilityNegotiate) {
    _dontCapabilityNegotiate = dontCapabilityNegotiate;
  }

  public @Nullable Integer getEbgpMultihop() {
    return _ebgpMultihop;
  }

  public void setEbgpMultihop(@Nullable Integer ebgpMultihop) {
    _ebgpMultihop = ebgpMultihop;
  }

  public @Nullable Boolean getEnforceFirstAs() {
    return _enforceFirstAs;
  }

  public void setEnforceFirstAs(@Nullable Boolean enforceFirstAs) {
    _enforceFirstAs = enforceFirstAs;
  }

  public @Nullable Long getExportLocalPref() {
    return _exportLocalPref;
  }

  public void setExportLocalPref(@Nullable Long exportLocalPref) {
    _exportLocalPref = exportLocalPref;
  }

  public @Nonnull AristaBgpNeighborAddressFamily getGenericAddressFamily() {
    return _genericAddressFamily;
  }

  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  public @Nullable Boolean getNextHopPeer() {
    return _nextHopPeer;
  }

  public void setNextHopPeer(@Nullable Boolean nextHopPeer) {
    _nextHopPeer = nextHopPeer;
  }

  public @Nullable Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(@Nullable Boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }

  public @Nullable Boolean getNextHopUnchanged() {
    return _nextHopUnchanged;
  }

  public void setNextHopUnchanged(@Nullable Boolean nextHopUnchanged) {
    _nextHopUnchanged = nextHopUnchanged;
  }

  public @Nullable Boolean getPassive() {
    return _passive;
  }

  public void setPassive(@Nullable Boolean passive) {
    _passive = passive;
  }

  public @Nullable Long getRemoteAs() {
    return _remoteAs;
  }

  public void setRemoteAs(@Nullable Long remoteAs) {
    _remoteAs = remoteAs;
  }

  public @Nullable RemovePrivateAsMode getRemovePrivateAsMode() {
    return _removePrivateAsMode;
  }

  public void setRemovePrivateAsMode(@Nullable RemovePrivateAsMode removePrivateAsMode) {
    _removePrivateAsMode = removePrivateAsMode;
  }

  public @Nullable Boolean getRouteReflectorClient() {
    return _routeReflectorClient;
  }

  public void setRouteReflectorClient(@Nullable Boolean routeReflectorClient) {
    _routeReflectorClient = routeReflectorClient;
  }

  public @Nullable Boolean getSendCommunity() {
    return _sendCommunity;
  }

  public void setSendCommunity(@Nullable Boolean sendCommunity) {
    _sendCommunity = sendCommunity;
  }

  public @Nullable Boolean getSendExtendedCommunity() {
    return _sendExtendedCommunity;
  }

  public void setSendExtendedCommunity(@Nullable Boolean sendExtendedCommunity) {
    _sendExtendedCommunity = sendExtendedCommunity;
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

  protected AristaBgpNeighbor() {
    _genericAddressFamily = new AristaBgpNeighborAddressFamily();
  }

  protected void inheritFrom(AristaBgpNeighbor other) {
    if (_allowAsIn == null) {
      _allowAsIn = other._allowAsIn;
    }
    if (_autoLocalAddr == null) {
      _autoLocalAddr = other._autoLocalAddr;
    }
    // do not inherit description
    if (_defaultOriginate == null) {
      _defaultOriginate = other._defaultOriginate;
    }
    if (_dontCapabilityNegotiate == null) {
      _dontCapabilityNegotiate = other._dontCapabilityNegotiate;
    }
    if (_ebgpMultihop == null) {
      _ebgpMultihop = other._ebgpMultihop;
    }
    if (_enforceFirstAs == null) {
      _enforceFirstAs = other._enforceFirstAs;
    }
    if (_exportLocalPref == null) {
      _exportLocalPref = other._exportLocalPref;
    }
    if (_localAs == null) {
      _localAs = other._localAs;
    }
    if (_nextHopPeer == null) {
      _nextHopPeer = other._nextHopPeer;
    }
    if (_nextHopSelf == null) {
      _nextHopSelf = other._nextHopSelf;
    }
    if (_nextHopUnchanged == null) {
      _nextHopUnchanged = other._nextHopUnchanged;
    }
    if (_passive == null) {
      _passive = other._passive;
    }
    if (_remoteAs == null) {
      _remoteAs = other._remoteAs;
    }
    if (_removePrivateAsMode == null) {
      _removePrivateAsMode = other._removePrivateAsMode;
    }
    if (_routeReflectorClient == null) {
      _routeReflectorClient = other._routeReflectorClient;
    }
    if (_sendCommunity == null) {
      _sendCommunity = other._sendCommunity;
    }
    if (_sendExtendedCommunity == null) {
      _sendExtendedCommunity = other._sendExtendedCommunity;
    }
    if (_shutdown == null) {
      _shutdown = other._shutdown;
    }
    if (_updateSource == null) {
      _updateSource = other._updateSource;
    }
    // DO NOT inherit the generic address family - that needs to be done elsewhere to ensure
    // ordering is correct.
  }
}
