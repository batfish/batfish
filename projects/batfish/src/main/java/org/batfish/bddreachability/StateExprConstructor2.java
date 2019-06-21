package org.batfish.bddreachability;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.symbolic.state.StateExpr;

/** A function of two {@link String}s that returns a {@link StateExpr}. */
@FunctionalInterface
@ParametersAreNonnullByDefault
public interface StateExprConstructor2 {

  @Nonnull
  StateExpr apply(String arg1, String arg2);
}
