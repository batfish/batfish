package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.Prefix6SetExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public abstract class RoutePolicyPrefixSet implements Serializable {

  public abstract @Nullable Prefix6SetExpr toPrefix6SetExpr(
      CiscoXrConfiguration cc, Configuration c, Warnings w);

  public abstract @Nullable PrefixSetExpr toPrefixSetExpr(
      CiscoXrConfiguration cc, Configuration c, Warnings w);
}
