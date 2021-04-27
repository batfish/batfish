package org.batfish.representation.cisco_xr;

import java.util.Objects;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

public final class RoutePolicyBooleanValidationStateIs extends RoutePolicyBoolean {

  private final boolean _valid;

  public RoutePolicyBooleanValidationStateIs(Boolean valid) {
    _valid = valid;
  }

  public boolean getValid() {
    return _valid;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    // Since we don't model RPKI, assume all announcements are valid.
    return _valid ? BooleanExprs.TRUE : BooleanExprs.FALSE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof RoutePolicyBooleanValidationStateIs)) {
      return false;
    }
    RoutePolicyBooleanValidationStateIs that = (RoutePolicyBooleanValidationStateIs) o;
    return _valid == that._valid;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_valid);
  }
}
