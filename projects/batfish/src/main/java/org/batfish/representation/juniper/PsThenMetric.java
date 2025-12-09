package org.batfish.representation.juniper;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the action in Juniper's routing policy(policy statement) which sets the metric for a
 * matched route
 */
@ParametersAreNonnullByDefault
public final class PsThenMetric extends PsThen {

  public enum Operator {
    /** Increment the metric, clipping at 4,294,967,295. */
    ADD,
    /** Decrement the metric, clipping at 0. */
    SUBTRACT,
    /** Set the metric to the given value. */
    SET,
  }

  private final long _metric;
  private final @Nonnull Operator _op;

  public PsThenMetric(long metric, Operator op) {
    _metric = metric;
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
      expr = new IncrementMetric(_metric);
    } else if (_op == Operator.SUBTRACT) {
      expr = new DecrementMetric(_metric);
    } else {
      assert _op == Operator.SET;
      expr = new LiteralLong(_metric);
    }
    statements.add(new SetMetric(expr));
  }

  public long getMetric() {
    return _metric;
  }

  public @Nonnull Operator getOp() {
    return _op;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenMetric)) {
      return false;
    }
    PsThenMetric that = (PsThenMetric) o;
    return _metric == that._metric && _op == that._op;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_metric, _op.ordinal());
  }
}
