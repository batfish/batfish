package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

public class IpBgpPeerGroup extends LeafBgpPeerGroup {

  private Ip _ip;

  public IpBgpPeerGroup(Ip ip) {
    _ip = ip;
  }

  public Ip getIp() {
    return _ip;
  }

  @Override
  public String getName() {
    return _ip.toString();
  }

  @Override
  public Prefix getNeighborPrefix() {
    return _ip.toPrefix();
  }

  @Nullable
  @Override
  public Prefix6 getNeighborPrefix6() {
    return null;
  }
}
