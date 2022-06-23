package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public class RoutePolicyInlinePrefixSet extends RoutePolicyPrefixSet {

  private final PrefixSpace _prefixSpace;

  public RoutePolicyInlinePrefixSet(PrefixSpace prefixSpace) {
    _prefixSpace = prefixSpace;
  }

  public PrefixSpace getPrefixSpace() {
    return _prefixSpace;
  }

  @Override
  public PrefixSetExpr toPrefixSetExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new ExplicitPrefixSet(_prefixSpace);
  }
}
