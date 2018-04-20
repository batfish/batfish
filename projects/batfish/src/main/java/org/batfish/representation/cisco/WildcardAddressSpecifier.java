package org.batfish.representation.cisco;

import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;

public class WildcardAddressSpecifier implements AccessListAddressSpecifier {

  /** */
  private static final long serialVersionUID = 1L;

  private final IpWildcard _ipWildcard;

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  public WildcardAddressSpecifier(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  @Override
  public IpSpace toIpSpace() {
    return _ipWildcard.toIpSpace();
  }
}
