package org.batfish.representation.cumulus;

import org.batfish.datamodel.Ip;

public class BgpIpNeighbor extends BgpNeighbor {
  private Ip _ip;

  public BgpIpNeighbor(String name) {
    super(name);
  }

  public Ip getIp() {
    return _ip;
  }

  public BgpIpNeighbor setIp(Ip ip) {
    _ip = ip;
    return this;
  }
}
