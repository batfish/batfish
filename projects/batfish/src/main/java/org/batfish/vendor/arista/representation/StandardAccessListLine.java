package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface StandardAccessListLine extends Serializable {
  long getSeq();

  Optional<ExtendedAccessListLine> toExtendedAccessListLine();
}
