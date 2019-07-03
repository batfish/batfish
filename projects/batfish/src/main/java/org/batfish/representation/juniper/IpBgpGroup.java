package org.batfish.representation.juniper;

import org.batfish.datamodel.Prefix;

public class IpBgpGroup extends BgpGroup {

  private Prefix _remoteAddress;

  public IpBgpGroup(Prefix remoteAddress) {
    _remoteAddress = remoteAddress;
  }

  public Prefix getRemoteAddress() {
    return _remoteAddress;
  }
}
