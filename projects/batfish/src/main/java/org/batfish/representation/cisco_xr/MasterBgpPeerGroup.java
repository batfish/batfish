package org.batfish.representation.cisco_xr;

/**
 * BgpPeerGroup representing per-neighbor default settings before peer groups/templates/individual
 * neighbor settings are applied
 */
public class MasterBgpPeerGroup extends BgpPeerGroup {

  private static final String MASTER_BGP_PEER_GROUP_NAME = "~MASTER_BGP_PEER_GROUP~";

  public MasterBgpPeerGroup() {
    _active = false;
    _additionalPathsReceive = false;
    _additionalPathsSelectAll = false;
    _additionalPathsSend = false;
    _advertiseInactive = false;
    _allowAsIn = false;
    _defaultOriginate = false;
    _ebgpMultihop = false;
    _routeReflectorClient = false;
    _sendCommunity = false;
    _shutdown = false;
  }

  @Override
  public String getName() {
    return MASTER_BGP_PEER_GROUP_NAME;
  }
}
