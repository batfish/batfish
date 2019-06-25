package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

public class Ipv6BgpPeerGroup extends LeafBgpPeerGroup {

  private Ip6 _ip6;

  public Ipv6BgpPeerGroup(Ip6 ip6) {
    _ip6 = ip6;
  }

  public Ip6 getIp6() {
    return _ip6;
  }

  @Override
  public String getName() {
    return _ip6.toString();
  }

  @Nullable
  @Override
  public Prefix getNeighborPrefix() {
    return null;
  }

  @Override
  public Prefix6 getNeighborPrefix6() {
    return new Prefix6(_ip6, Prefix6.MAX_PREFIX_LENGTH);
  }
}
