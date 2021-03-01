package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.JuniperConfiguration.computeConditionRoutingPolicyName;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;

/** Represents a "from condition" line in a {@link PsTerm} */
@ParametersAreNonnullByDefault
public final class PsFromCondition extends PsFrom {

  public PsFromCondition(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new CallExpr(computeConditionRoutingPolicyName(_name));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PsFromCondition that = (PsFromCondition) o;
    return _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  private final @Nonnull String _name;
}
