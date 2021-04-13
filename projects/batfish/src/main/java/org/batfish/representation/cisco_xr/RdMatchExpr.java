package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An expression matching the route distinguisher of an NLRI. */
@ParametersAreNonnullByDefault
public interface RdMatchExpr extends Serializable {

  <T, U> T accept(RdMatchExprVisitor<T, U> visitor, U arg);
}
