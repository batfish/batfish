package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.JuniperConfiguration.computeConditionTrackName;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;

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
    // https://www.juniper.net/documentation/us/en/software/junos/bgp/topics/topic-map/basic-routing-policies.html#id-conditional-advertisement-and-import-policy-routing-table-with-certain-match-conditions
    // TODO: "Conditions in routing policies can be configured irrespective of whether they are a
    //       art of the export or import policies or both. The export policy supports these
    //       conditions inherited from the routing policy based on the existence of another route in
    //       the routing policy. However, the import policy doesn't support these conditions, and
    //       the conditions are not executed even if they are present."
    //       So this should be converted to either unconditional true or false for non-export
    //       context. For this, we need to:
    //       1. Determine effective value for non-BGP-export context
    //       2. Create a new BooleanExpr that matches on direction. With this, and match on
    //          (any) BGP session type, we can implement the appropriate guard.
    return new TrackSucceeded(computeConditionTrackName(_name));
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
