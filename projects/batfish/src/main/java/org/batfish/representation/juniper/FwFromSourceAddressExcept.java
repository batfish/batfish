package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public final class FwFromSourceAddressExcept extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final IpWildcard _ipWildcard;

  private final Prefix _prefix;

  public FwFromSourceAddressExcept(Prefix prefix) {
    _prefix = prefix;
    _ipWildcard = null;
  }

  public FwFromSourceAddressExcept(IpWildcard ipWildcard) {
    _prefix = null;
    _ipWildcard = ipWildcard;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    IpWildcard wildcard;
    if (_ipWildcard != null) {
      wildcard = _ipWildcard;
    } else {
      wildcard = new IpWildcard(_prefix);
    }
    headerSpaceBuilder.setNotSrcIps(
        AclIpSpace.union(headerSpaceBuilder.getNotSrcIps(), wildcard.toIpSpace()));
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }
}
