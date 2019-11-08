package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A visitor of {@link Uint32RangeExpr} that takes 1 generic argument and returns a generic value.
 */
@ParametersAreNonnullByDefault
public interface Uint32RangeExprVisitor<T, U> {

  T visitLiteralUint32(LiteralUint32 literalUint32, U arg);
}
