package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.common.ip.IpSpace;

public interface AccessListAddressSpecifier extends Serializable {

  @Nonnull
  IpSpace toIpSpace();
}
