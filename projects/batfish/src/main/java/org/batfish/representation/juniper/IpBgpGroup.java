package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

public class IpBgpGroup extends BgpGroup {

  private @Nullable String _dynamicNeighborName;
  private Prefix _remoteAddress;

  public IpBgpGroup(Prefix remoteAddress) {
    _remoteAddress = remoteAddress;
  }

  public Prefix getRemoteAddress() {
    return _remoteAddress;
  }

  public @Nullable String getDynamicNeighborName() {
    return _dynamicNeighborName;
  }

  public void setDynamicNeighborName(@Nullable String dynamicNeighborName) {
    _dynamicNeighborName = dynamicNeighborName;
  }
}
