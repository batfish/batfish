package org.batfish.representation.cumulus;

import org.batfish.datamodel.Ip;

public class BgpIpNeighbor extends BgpNeighbor {
  private Ip _peerIp;

  public BgpIpNeighbor(String name) {
    super(name);
  }

  public Ip getPeerIp() {
    return _peerIp;
  }

  public BgpIpNeighbor setPeerIp(Ip ip) {
    _peerIp = ip;
    return this;
  }
}
