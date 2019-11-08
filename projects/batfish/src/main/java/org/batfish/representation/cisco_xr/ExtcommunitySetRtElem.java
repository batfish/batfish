package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An element of a {@link ExtcommunitySetRt}. */
@ParametersAreNonnullByDefault
public interface ExtcommunitySetRtElem extends Serializable {

  <T, U> T accept(ExtcommunitySetRtElemVisitor<T, U> visitor, U arg);
}
