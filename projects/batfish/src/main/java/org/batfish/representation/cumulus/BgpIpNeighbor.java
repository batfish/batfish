package org.batfish.representation.cumulus;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** BGP neighbor identified by an IPv4 peer address */
public class BgpIpNeighbor extends BgpNeighbor {
  private final @Nonnull Ip _peerIp;

  public BgpIpNeighbor(String name, Ip ip) {
    super(name);
    _peerIp = ip;
  }

  public @Nonnull Ip getPeerIp() {
    return _peerIp;
  }
}
