package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/** Represents a "from validation-database" line in a {@link PsTerm} */
public final class PsFromValidationDatabase extends PsFrom {

  public enum State {
    VALID,
    INVALID,
    UNKNOWN,
  }

  private final @Nonnull State _state;

  public PsFromValidationDatabase(@Nonnull State state) {
    _state = state;
  }

  public @Nonnull State getState() {
    return _state;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    // Batfish does not model RPKI, so a default validation state must be chosen. "valid" is used
    // because this clause only appears in networks with RPKI deployed, where most routes are valid.
    // This makes common patterns like "from validation-database valid then local-preference add N"
    // produce expected behavior, whereas "unknown" would cause such policies to silently not match.
    return _state == State.VALID ? BooleanExprs.TRUE : BooleanExprs.FALSE;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PsFromValidationDatabase that)) {
      return false;
    }
    return _state == that._state;
  }

  @Override
  public int hashCode() {
    return _state.ordinal();
  }
}
