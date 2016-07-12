package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;
import org.batfish.main.Warnings;

public abstract class RoutePolicyPrefixSet implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract PrefixSetExpr toPrefixSetExpr(CiscoConfiguration cc,
         Configuration c, Warnings w);

}
