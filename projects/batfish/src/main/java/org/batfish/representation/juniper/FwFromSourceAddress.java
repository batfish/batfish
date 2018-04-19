package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public final class FwFromSourceAddress extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  public FwFromSourceAddress(Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    IpWildcard wildcard = new IpWildcard(_prefix);
    headerSpaceBuilder.setSrcIps(
        AclIpSpace.union(headerSpaceBuilder.getSrcIps(), wildcard.toIpSpace()));
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}
