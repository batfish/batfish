package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An expression representing an {@link ExtcommunitySetRt}. */
@ParametersAreNonnullByDefault
public interface ExtcommunitySetRtExpr extends Serializable {

  <T, U> T accept(ExtcommunitySetRtExprVisitor<T, U> visitor, U arg);
}
