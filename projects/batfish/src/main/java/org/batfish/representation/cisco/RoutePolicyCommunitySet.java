package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.common.Warnings;

public abstract class RoutePolicyCommunitySet implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract CommunitySetExpr toCommunitySetExpr(CiscoConfiguration cc,
         Configuration c, Warnings w);

}
