package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

public class IpBgpPeerGroup extends LeafBgpPeerGroup {

  private static final long serialVersionUID = 1L;

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
    return new Prefix(_ip, Prefix.MAX_PREFIX_LENGTH);
  }

  @Nullable
  @Override
  public Prefix6 getNeighborPrefix6() {
    return null;
  }
}
