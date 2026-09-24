package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a "to policy" line in a {@link PsTerm}. */
@ParametersAreNonnullByDefault
public final class PsToPolicyStatement extends PsTo {

  public PsToPolicyStatement(String policyStatement) {
    _policyStatement = policyStatement;
  }

  public @Nonnull String getPolicyStatement() {
    return _policyStatement;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof PsToPolicyStatement
        && _policyStatement.equals(((PsToPolicyStatement) o)._policyStatement);
  }

  @Override
  public int hashCode() {
    return _policyStatement.hashCode();
  }

  private final @Nonnull String _policyStatement;
}
