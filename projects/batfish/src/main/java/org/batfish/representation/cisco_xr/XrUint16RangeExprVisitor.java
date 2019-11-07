package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A visitor of {@link XrUint16RangeExpr} that takes 1 generic argument and returns a generic value.
 */
@ParametersAreNonnullByDefault
public interface XrUint16RangeExprVisitor<T, U> {

  T visitLiteralUint16(XrLiteralUint16 literalUint16, U arg);

  T visitLiteralUint16Range(XrLiteralUint16Range literalUint16Range, U arg);

  T visitUint16Reference(XrUint16Reference uint16Reference, U arg);
}
