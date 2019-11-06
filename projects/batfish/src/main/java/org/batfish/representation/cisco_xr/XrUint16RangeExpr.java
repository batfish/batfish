package org.batfish.representation.cisco_xr;

import java.io.Serializable;

/** An expression representing a range of 16-bit unsigned integers. */
public interface XrUint16RangeExpr extends Serializable {

  <T, U> T accept(XrUint16RangeExprVisitor<T, U> visitor, U arg);
}
