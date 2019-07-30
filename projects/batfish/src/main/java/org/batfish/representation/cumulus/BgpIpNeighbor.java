package org.batfish.representation.cumulus;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** BGP neighbor identified by an IPv4 peer address */
public class BgpIpNeighbor extends BgpNeighbor {
  private Ip _peerIp;

  public BgpIpNeighbor(String name) {
    super(name);
  }

  public Ip getPeerIp() {
    return _peerIp;
  }

  public void setPeerIp(@Nonnull Ip ip) {
    _peerIp = ip;
  }
}
