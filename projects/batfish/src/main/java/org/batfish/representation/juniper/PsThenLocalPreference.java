package org.batfish.representation.juniper;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.DecrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.IncrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statement;

@ParametersAreNonnullByDefault
public final class PsThenLocalPreference extends PsThen {

  public enum Operator {
    /** Increment the local-preference, clipping at 4,294,967,295. */
    ADD,
    /** Decrement the local-preference, clipping at 0. */
    SUBTRACT,
    /** Set the local-preference to the given value. */
    SET,
  }

  private final long _localPreference;
  private final @Nonnull Operator _op;

  public PsThenLocalPreference(long localPreference, Operator op) {
    _localPreference = localPreference;
    _op = op;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    LongExpr expr;
    if (_op == Operator.ADD) {
      expr = new IncrementLocalPreference(_localPreference);
    } else if (_op == Operator.SUBTRACT) {
      expr = new DecrementLocalPreference(_localPreference);
    } else {
      assert _op == Operator.SET;
      expr = new LiteralLong(_localPreference);
    }
    statements.add(new SetLocalPreference(expr));
  }

  public long getLocalPreference() {
    return _localPreference;
  }

  public @Nonnull Operator getOp() {
    return _op;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenLocalPreference)) {
      return false;
    }
    PsThenLocalPreference that = (PsThenLocalPreference) o;
    return _localPreference == that._localPreference && _op == that._op;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_localPreference, _op.ordinal());
  }
}
