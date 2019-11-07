package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An element of a {@link XrCommunitySet}. */
@ParametersAreNonnullByDefault
public interface XrCommunitySetElem extends Serializable {

  <T, U> T accept(XrCommunitySetElemVisitor<T, U> visitor, U arg);
}
