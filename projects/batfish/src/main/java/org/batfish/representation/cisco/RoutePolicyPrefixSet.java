package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.Prefix6SetExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public abstract class RoutePolicyPrefixSet implements Serializable {

  private static final long serialVersionUID = 1L;

  public abstract Prefix6SetExpr toPrefix6SetExpr(
      CiscoConfiguration cc, Configuration c, Warnings w);

  public abstract PrefixSetExpr toPrefixSetExpr(CiscoConfiguration cc, Configuration c, Warnings w);
}
