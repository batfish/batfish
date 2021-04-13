package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An element of an {@link RdSet}. */
@ParametersAreNonnullByDefault
public interface RdSetElem extends Serializable {

  <T, U> T accept(RdSetElemVisitor<T, U> visitor, U arg);
}
