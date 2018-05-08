package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;

public final class FwFromSourceAddressExcept extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final IpWildcard _ipWildcard;

  public FwFromSourceAddressExcept(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setNotSrcIps(
        AclIpSpace.union(headerSpaceBuilder.getNotSrcIps(), _ipWildcard.toIpSpace()));
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }
}
