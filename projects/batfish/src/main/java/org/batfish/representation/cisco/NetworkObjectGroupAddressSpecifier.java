package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;

public class NetworkObjectGroupAddressSpecifier implements AccessListAddressSpecifier {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public NetworkObjectGroupAddressSpecifier(String name) {
    _name = name;
  }

  @Override
  @Nonnull
  public IpSpace toIpSpace() {
    return new IpSpaceReference(_name, String.format("Match network object-group: '%s'", _name));
  }
}
