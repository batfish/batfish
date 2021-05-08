package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An element of an {@link AsPathSet}. */
@ParametersAreNonnullByDefault
public interface AsPathSetElem extends Serializable {

  <T, U> T accept(AsPathSetElemVisitor<T, U> visitor, U arg);
}
