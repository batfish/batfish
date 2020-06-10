package org.batfish.representation.arista.eos;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Base class for all Arista BGP neighbors */
public abstract class AristaBgpNeighbor implements Serializable {
  public enum RemovePrivateAsMode {
    NONE,
    BASIC,
    ALL,
    REPLACE_AS,
  }

  @Nullable private Integer _allowAsIn;
  @Nullable private Boolean _autoLocalAddr;
  @Nullable private AristaBgpDefaultOriginate _defaultOriginate;
  @Nullable private String _description;
  @Nullable private Boolean _dontCapabilityNegotiate;
  @Nullable private Integer _ebgpMultihop;
  @Nullable private Boolean _enforceFirstAs;
  @Nullable private Long _localAs;
  @Nullable private Boolean _nextHopPeer;
  @Nullable private Boolean _nextHopSelf;
  @Nullable private Boolean _nextHopUnchanged;
  // Whether this neighbor will initiate a BGP connection. If false, will listen only.
  @Nullable private Boolean _passive;
  @Nullable private Long _remoteAs;
  @Nullable private RemovePrivateAsMode _removePrivateAsMode;
  @Nullable private Boolean _routeReflectorClient;
  @Nullable private Boolean _sendCommunity;
  @Nullable private Boolean _sendExtendedCommunity;
  @Nullable private Boolean _shutdown;
  @Nullable private String _updateSource;
  @Nonnull private AristaBgpNeighborAddressFamily _genericAddressFamily;

  @Nullable
  public Integer getAllowAsIn() {
    return _allowAsIn;
  }

  public void setAllowAsIn(@Nullable Integer allowAsIn) {
    _allowAsIn = allowAsIn;
  }

  @Nullable
  public Boolean getAutoLocalAddr() {
    return _autoLocalAddr;
  }

  public void setAutoLocalAddr(@Nullable Boolean autoLocalAddr) {
    _autoLocalAddr = autoLocalAddr;
  }

  @Nullable
  public AristaBgpDefaultOriginate getDefaultOriginate() {
    return _defaultOriginate;
  }

  public void setDefaultOriginate(@Nullable AristaBgpDefaultOriginate defaultOriginate) {
    _defaultOriginate = defaultOriginate;
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  @Nullable
  public Boolean getDontCapabilityNegotiate() {
    return _dontCapabilityNegotiate;
  }

  public void setDontCapabilityNegotiate(@Nullable Boolean dontCapabilityNegotiate) {
    _dontCapabilityNegotiate = dontCapabilityNegotiate;
  }

  @Nullable
  public Integer getEbgpMultihop() {
    return _ebgpMultihop;
  }

  public void setEbgpMultihop(@Nullable Integer ebgpMultihop) {
    _ebgpMultihop = ebgpMultihop;
  }

  @Nullable
  public Boolean getEnforceFirstAs() {
    return _enforceFirstAs;
  }

  public void setEnforceFirstAs(@Nullable Boolean enforceFirstAs) {
    _enforceFirstAs = enforceFirstAs;
  }

  @Nonnull
  public AristaBgpNeighborAddressFamily getGenericAddressFamily() {
    return _genericAddressFamily;
  }

  @Nullable
  public Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  @Nullable
  public Boolean getNextHopPeer() {
    return _nextHopPeer;
  }

  public void setNextHopPeer(@Nullable Boolean nextHopPeer) {
    _nextHopPeer = nextHopPeer;
  }

  @Nullable
  public Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(@Nullable Boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }

  @Nullable
  public Boolean getNextHopUnchanged() {
    return _nextHopUnchanged;
  }

  public void setNextHopUnchanged(@Nullable Boolean nextHopUnchanged) {
    _nextHopUnchanged = nextHopUnchanged;
  }

  @Nullable
  public Boolean getPassive() {
    return _passive;
  }

  public void setPassive(@Nullable Boolean passive) {
    _passive = passive;
  }

  @Nullable
  public Long getRemoteAs() {
    return _remoteAs;
  }

  public void setRemoteAs(@Nullable Long remoteAs) {
    _remoteAs = remoteAs;
  }

  @Nullable
  public RemovePrivateAsMode getRemovePrivateAsMode() {
    return _removePrivateAsMode;
  }

  public void setRemovePrivateAsMode(@Nullable RemovePrivateAsMode removePrivateAsMode) {
    _removePrivateAsMode = removePrivateAsMode;
  }

  @Nullable
  public Boolean getRouteReflectorClient() {
    return _routeReflectorClient;
  }

  public void setRouteReflectorClient(@Nullable Boolean routeReflectorClient) {
    _routeReflectorClient = routeReflectorClient;
  }

  @Nullable
  public Boolean getSendCommunity() {
    return _sendCommunity;
  }

  public void setSendCommunity(@Nullable Boolean sendCommunity) {
    _sendCommunity = sendCommunity;
  }

  @Nullable
  public Boolean getSendExtendedCommunity() {
    return _sendExtendedCommunity;
  }

  public void setSendExtendedCommunity(@Nullable Boolean sendExtendedCommunity) {
    _sendExtendedCommunity = sendExtendedCommunity;
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
