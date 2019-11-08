package org.batfish.representation.cisco_xr;

import java.io.Serializable;

/** An expression representing a range of 32-bit unsigned integers. */
public interface Uint32RangeExpr extends Serializable {

  <T, U> T accept(Uint32RangeExprVisitor<T, U> visitor, U arg);
}
