package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;

/**
 * Represents the BGP configuration for a single address family in a single BGP neighbor.
 *
 * <p>Configuration commands entered at the CLI {@code config-router-neighbor-af} or {@code
 * config-router-vrf-neighbor-af} levels.
 */
public final class BgpVrfNeighborAddressFamilyConfiguration implements Serializable {

  public BgpVrfNeighborAddressFamilyConfiguration() {
    _inheritPeerPolicies = new TreeMap<>();
  }

  public @Nullable Integer getAllowAsIn() {
    return _allowAsIn;
  }

  public void setAllowAsIn(@Nullable Integer allowAsIn) {
    _allowAsIn = allowAsIn;
  }

  public @Nullable Boolean getAsOverride() {
    return _asOverride;
  }

  public void setAsOverride(@Nullable Boolean asOverride) {
    _asOverride = asOverride;
  }

  public @Nullable Boolean getDefaultOriginate() {
    return _defaultOriginate;
  }

  public void setDefaultOriginate(@Nullable Boolean defaultOriginate) {
    _defaultOriginate = defaultOriginate;
  }

  public @Nullable String getDefaultOriginateMap() {
    return _defaultOriginateMap;
  }

  public void setDefaultOriginateMap(@Nullable String defaultOriginateMap) {
    _defaultOriginateMap = defaultOriginateMap;
  }

  public @Nullable Boolean getDisablePeerAsCheck() {
    return _disablePeerAsCheck;
  }

  public void setDisablePeerAsCheck(@Nullable Boolean disablePeerAsCheck) {
    _disablePeerAsCheck = disablePeerAsCheck;
  }

  public @Nullable String getInboundPrefixList() {
    return _inboundPrefixList;
  }

  public void setInboundPrefixList(@Nullable String inboundPrefixList) {
    _inboundPrefixList = inboundPrefixList;
  }

  public @Nullable String getInboundRouteMap() {
    return _inboundRouteMap;
  }

  public void setInboundRouteMap(@Nullable String inboundRouteMap) {
    _inboundRouteMap = inboundRouteMap;
  }

  public void setInheritPeerPolicy(int seq, @Nullable String policy) {
    _inheritPeerPolicies.put(seq, policy);
  }

  public @Nullable Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(@Nullable Boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }

  public @Nullable Boolean getNextHopThirdParty() {
    return _nextHopThirdParty;
  }

  public void setNextHopThirdParty(@Nullable Boolean nextHopThirdParty) {
    _nextHopThirdParty = nextHopThirdParty;
  }

  public @Nullable String getOutboundPrefixList() {
    return _outboundPrefixList;
  }

  public void setOutboundPrefixList(@Nullable String outboundPrefixList) {
    _outboundPrefixList = outboundPrefixList;
  }

  public @Nullable String getOutboundRouteMap() {
    return _outboundRouteMap;
  }

  public void setOutboundRouteMap(@Nullable String outboundRouteMap) {
    _outboundRouteMap = outboundRouteMap;
  }

  public @Nullable Boolean getRouteReflectorClient() {
    return _routeReflectorClient;
  }

  public void setRouteReflectorClient(@Nullable Boolean routeReflectorClient) {
    _routeReflectorClient = routeReflectorClient;
  }

  public @Nullable Boolean getSendCommunityExtended() {
    return _sendCommunityExtended;
  }

  public void setSendCommunityExtended(@Nullable Boolean sendCommunityExtended) {
    _sendCommunityExtended = sendCommunityExtended;
  }

  public @Nullable Boolean getSendCommunityStandard() {
    return _sendCommunityStandard;
  }

  public void setSendCommunityStandard(@Nullable Boolean sendCommunityStandard) {
    _sendCommunityStandard = sendCommunityStandard;
  }

  public @Nullable Boolean getSuppressInactive() {
    return _suppressInactive;
  }

  public void setSuppressInactive(@Nullable Boolean suppressInactive) {
    _suppressInactive = suppressInactive;
  }

  private @Nullable Integer _allowAsIn;
  private @Nullable Boolean _asOverride;
  private @Nullable Boolean _defaultOriginate;
  private @Nullable String _defaultOriginateMap;
  private @Nullable Boolean _disablePeerAsCheck;
  private boolean _doneInheriting;
  private @Nullable String _inboundPrefixList;
  private @Nullable String _inboundRouteMap;
  private final SortedMap<Integer, String> _inheritPeerPolicies;
  private @Nullable Boolean _nextHopSelf;
  private @Nullable Boolean _nextHopThirdParty;
  private @Nullable String _outboundPrefixList;
  private @Nullable String _outboundRouteMap;
  private @Nullable Boolean _routeReflectorClient;
  private @Nullable Boolean _sendCommunityExtended;
  private @Nullable Boolean _sendCommunityStandard;
  private @Nullable Boolean _suppressInactive;

  void doInherit(BgpGlobalConfiguration nxBgpGlobal, Warnings warnings) {
    if (_doneInheriting) {
      return;
    }
    // Mark inherited first, for loop prevention.
    _doneInheriting = true;

    for (String policyName : _inheritPeerPolicies.values()) {
      BgpVrfNeighborAddressFamilyConfiguration policy =
          nxBgpGlobal.getTemplatePeerPolicy(policyName);
      if (policy != null) {
        inheritFrom(nxBgpGlobal, warnings, policy);
      }
    }
  }

  void inheritFrom(
      BgpGlobalConfiguration nxBgpGlobal,
      Warnings warnings,
      BgpVrfNeighborAddressFamilyConfiguration policy) {
    policy.doInherit(nxBgpGlobal, warnings);
    if (_allowAsIn == null) {
      _allowAsIn = policy._allowAsIn;
    }
    if (_asOverride == null) {
      _asOverride = policy._asOverride;
    }
    if (_defaultOriginate == null) {
      _defaultOriginate = policy._defaultOriginate;
    }
    if (_defaultOriginateMap == null) {
      _defaultOriginateMap = policy._defaultOriginateMap;
    }
    if (_disablePeerAsCheck == null) {
      _disablePeerAsCheck = policy._disablePeerAsCheck;
    }
    if (_inboundPrefixList == null) {
      _inboundPrefixList = policy._inboundPrefixList;
    }
    if (_inboundRouteMap == null) {
      _inboundRouteMap = policy._inboundRouteMap;
    }
    if (_nextHopSelf == null) {
      _nextHopSelf = policy._nextHopSelf;
    }
    if (_nextHopThirdParty == null) {
      _nextHopThirdParty = policy._nextHopThirdParty;
    }
    if (_outboundPrefixList == null) {
      _outboundPrefixList = policy._outboundPrefixList;
    }
    if (_outboundRouteMap == null) {
      _outboundRouteMap = policy._outboundRouteMap;
    }
    if (_routeReflectorClient == null) {
      _routeReflectorClient = policy._routeReflectorClient;
    }
    if (_sendCommunityExtended == null) {
      _sendCommunityExtended = policy._sendCommunityExtended;
    }
    if (_sendCommunityStandard == null) {
      _sendCommunityStandard = policy._sendCommunityStandard;
    }
    if (_suppressInactive == null) {
      _suppressInactive = policy._suppressInactive;
    }
  }
}
