package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/** A visitor of {@link RdMatchExpr} that takes 1 generic argument and returns a generic value. */
@ParametersAreNonnullByDefault
public interface RdMatchExprVisitor<T, U> {

  T visitRdSetReference(RdSetReference rdSetReference);

  T visitRdSetParameterReference(RdSetParameterReference rdSetParameterReference);
}
