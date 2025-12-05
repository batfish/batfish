package org.batfish.representation.juniper;

import static org.batfish.datamodel.AbstractRoute.MAX_ADMIN_DISTANCE;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.AdministrativeCostExpr;
import org.batfish.datamodel.routing_policy.expr.DecrementAdministrativeCost;
import org.batfish.datamodel.routing_policy.expr.IncrementAdministrativeCost;
import org.batfish.datamodel.routing_policy.expr.LiteralAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the action in Juniper's routing policy(policy statement) which sets the preference for
 * a matched route
 */
@ParametersAreNonnullByDefault
public final class PsThenPreference extends PsThen {

  public enum Operator {
    /** Increment the preference, clipping at 4,294,967,295. */
    ADD,
    /** Decrement the preference, clipping at 0. */
    SUBTRACT,
    /** Set the preference to the given value. */
    SET,
  }

  private final long _preference;
  private final @Nonnull Operator _op;

  public PsThenPreference(long preference) {
    this(preference, Operator.SET);
  }

  public PsThenPreference(long preference, Operator op) {
    _preference = preference;
    _op = op;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    AdministrativeCostExpr expr;
    if (_op == Operator.ADD) {
      expr = new IncrementAdministrativeCost(_preference, MAX_ADMIN_DISTANCE);
    } else if (_op == Operator.SUBTRACT) {
      expr = new DecrementAdministrativeCost(_preference, 0);
    } else {
      assert _op == Operator.SET;
      expr = new LiteralAdministrativeCost(_preference);
    }
    statements.add(new SetAdministrativeCost(expr));
  }

  public long getPreference() {
    return _preference;
  }

  public @Nonnull Operator getOp() {
    return _op;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenPreference)) {
      return false;
    }
    PsThenPreference that = (PsThenPreference) o;
    return _preference == that._preference && _op == that._op;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_preference, _op.ordinal());
  }
}
