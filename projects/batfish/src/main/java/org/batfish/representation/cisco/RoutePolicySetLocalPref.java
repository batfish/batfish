package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statement;

@ParametersAreNonnullByDefault
public final class RoutePolicySetLocalPref extends RoutePolicySetStatement {

  private static final long serialVersionUID = 1L;

  private LongExpr _pref;

  public RoutePolicySetLocalPref(LongExpr intExpr) {
    _pref = intExpr;
  }

  public @Nonnull LongExpr getLocalPref() {
    return _pref;
  }

  @Override
  public Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new SetLocalPreference(_pref);
  }
}
