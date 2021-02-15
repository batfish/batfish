package org.batfish.representation.cumulus_nclu;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip6;

/** BGP neighbor identified by an IPv4 peer address */
public class BgpIpv6Neighbor extends BgpNeighbor {
  private final @Nonnull Ip6 _peerIp;

  public BgpIpv6Neighbor(String name, Ip6 ip6) {
    super(name);
    _peerIp = ip6;
  }

  public @Nonnull Ip6 getPeerIp() {
    return _peerIp;
  }
}
