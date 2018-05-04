package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public final class FwFromSourceAddress extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  @Nullable private final IpWildcard _ipWildcard;

  @Nullable private final Prefix _prefix;

  public FwFromSourceAddress(Prefix prefix) {
    _prefix = prefix;
    _ipWildcard = null;
  }

  public FwFromSourceAddress(IpWildcard ipWildcard) {
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
    headerSpaceBuilder.setSrcIps(
        AclIpSpace.union(headerSpaceBuilder.getSrcIps(), wildcard.toIpSpace()));
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }
}
