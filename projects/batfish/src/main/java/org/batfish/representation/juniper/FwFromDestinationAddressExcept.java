package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;

public final class FwFromDestinationAddressExcept extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  @Nullable private final IpWildcard _ipWildcard;

  public FwFromDestinationAddressExcept(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setNotDstIps(
        AclIpSpace.union(headerSpaceBuilder.getNotDstIps(), _ipWildcard.toIpSpace()));
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }
}
