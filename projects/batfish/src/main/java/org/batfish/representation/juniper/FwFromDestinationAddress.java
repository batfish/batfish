package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public final class FwFromDestinationAddress extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  @Nullable private final IpWildcard _ipWildcard;

  @Nullable private final Prefix _prefix;

  public FwFromDestinationAddress(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
    _prefix = null;
  }

  public FwFromDestinationAddress(Prefix prefix) {
    _ipWildcard = null;
    _prefix = prefix;
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
    headerSpaceBuilder.setDstIps(
        AclIpSpace.union(headerSpaceBuilder.getDstIps(), wildcard.toIpSpace()));
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }
}
