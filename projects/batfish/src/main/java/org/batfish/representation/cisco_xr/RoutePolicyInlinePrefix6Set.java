package org.batfish.representation.cisco_xr;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix6Space;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.Prefix6SetExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public class RoutePolicyInlinePrefix6Set extends RoutePolicyPrefixSet {

  private Prefix6Space _prefix6Space;

  public RoutePolicyInlinePrefix6Set(Prefix6Space prefix6Space) {
    _prefix6Space = prefix6Space;
  }

  public Prefix6Space getPrefix6Space() {
    return _prefix6Space;
  }

  @Override
  public Prefix6SetExpr toPrefix6SetExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new ExplicitPrefix6Set(_prefix6Space);
  }

  @Nullable
  @Override
  public PrefixSetExpr toPrefixSetExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return null;
  }
}
