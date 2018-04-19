package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;

public interface ExtendedAccessListAddressSpecifier extends Serializable {

  @Nonnull
  IpSpace toIpSpace();
}
