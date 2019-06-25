package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;

public abstract class RoutePolicyCommunitySet implements Serializable {

  public abstract CommunitySetExpr toCommunitySetExpr(
      CiscoConfiguration cc, Configuration c, Warnings w);
}
