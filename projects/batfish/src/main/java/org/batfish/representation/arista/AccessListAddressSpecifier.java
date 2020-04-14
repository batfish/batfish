package org.batfish.representation.arista;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;

public interface AccessListAddressSpecifier extends Serializable {

  @Nonnull
  IpSpace toIpSpace();
}
