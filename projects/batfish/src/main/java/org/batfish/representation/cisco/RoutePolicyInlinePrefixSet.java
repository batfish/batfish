package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Prefix6SetExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public class RoutePolicyInlinePrefixSet extends RoutePolicyPrefixSet {

  /** */
  private static final long serialVersionUID = 1L;

  private PrefixSpace _prefixSpace;

  public RoutePolicyInlinePrefixSet(PrefixSpace prefixSpace) {
    _prefixSpace = prefixSpace;
  }

  public PrefixSpace getPrefixSpace() {
    return _prefixSpace;
  }

  @Nullable
  @Override
  public Prefix6SetExpr toPrefix6SetExpr(CiscoConfiguration cc, Configuration c, Warnings w) {
    return null;
  }

  @Override
  public PrefixSetExpr toPrefixSetExpr(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new ExplicitPrefixSet(_prefixSpace);
  }
}
