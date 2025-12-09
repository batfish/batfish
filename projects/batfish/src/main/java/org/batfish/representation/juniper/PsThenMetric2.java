package org.batfish.representation.juniper;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the action in Juniper's routing policy(policy statement) which sets the metric2 for a
 * matched route
 */
@ParametersAreNonnullByDefault
public final class PsThenMetric2 extends PsThen {

  public enum Operator {
    /** Increment the metric2, clipping at 4,294,967,295. */
    ADD,
    /** Decrement the metric2, clipping at 0. */
    SUBTRACT,
    /** Set the metric2 to the given value. */
    SET,
  }

  private final long _metric2;
  private final @Nonnull Operator _op;

  public PsThenMetric2(long metric2, Operator op) {
    _metric2 = metric2;
    _op = op;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // TODO: implement metric2 in VI model
    // Metric2 is the IGP metric for BGP routes when next hop loops through another router
    // VI model only supports single metric field, needs additional modeling
  }

  public long getMetric2() {
    return _metric2;
  }

  public @Nonnull Operator getOp() {
    return _op;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenMetric2)) {
      return false;
    }
    PsThenMetric2 that = (PsThenMetric2) o;
    return _metric2 == that._metric2 && _op == that._op;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_metric2, _op.ordinal());
  }
}
