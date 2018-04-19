package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;

public interface ExtendedAccessListAddressSpecifier {

  @Nonnull
  IpSpace toIpSpace();
}
