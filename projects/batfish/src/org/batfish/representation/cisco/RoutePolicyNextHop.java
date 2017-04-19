package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;
import org.batfish.common.Warnings;

public abstract class RoutePolicyNextHop implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract NextHopExpr toNextHopExpr(CiscoConfiguration cc,
         Configuration c, Warnings w);

}
