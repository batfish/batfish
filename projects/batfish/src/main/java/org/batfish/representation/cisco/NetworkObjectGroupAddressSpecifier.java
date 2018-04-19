package org.batfish.representation.cisco;

import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;

public class NetworkObjectGroupAddressSpecifier implements ExtendedAccessListAddressSpecifier {

  private final String _name;

  public NetworkObjectGroupAddressSpecifier(String name) {
    _name = name;
  }

  @Override
  public IpSpace toIpSpace() {
    return new IpSpaceReference(_name);
  }
}
