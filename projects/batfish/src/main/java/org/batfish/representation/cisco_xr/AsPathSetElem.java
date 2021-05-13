package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An element of an {@link AsPathSet}. */
@ParametersAreNonnullByDefault
public interface AsPathSetElem extends Serializable {

  <T> T accept(AsPathSetElemVisitor<T> visitor);
}
