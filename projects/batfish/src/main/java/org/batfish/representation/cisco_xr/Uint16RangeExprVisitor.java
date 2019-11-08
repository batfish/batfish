package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A visitor of {@link Uint16RangeExpr} that takes 1 generic argument and returns a generic value.
 */
@ParametersAreNonnullByDefault
public interface Uint16RangeExprVisitor<T, U> {

  T visitLiteralUint16(LiteralUint16 literalUint16, U arg);

  T visitLiteralUint16Range(LiteralUint16Range literalUint16Range, U arg);

  T visitUint16Reference(Uint16Reference uint16Reference, U arg);
}
