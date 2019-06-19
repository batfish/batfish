package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;

public final class FwFromDestinationAddress extends FwFrom {

  private static final long serialVersionUID = 1L;

  @Nullable private final IpWildcard _ipWildcard;

  public FwFromDestinationAddress(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setDstIps(
        AclIpSpace.union(headerSpaceBuilder.getDstIps(), _ipWildcard.toIpSpace()));
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }
}
