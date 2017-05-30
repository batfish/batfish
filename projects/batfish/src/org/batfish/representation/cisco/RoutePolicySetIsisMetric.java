package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.common.Warnings;

public class RoutePolicySetIsisMetric extends RoutePolicySetStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntExpr _metric;

   public RoutePolicySetIsisMetric(IntExpr metric) {
      _metric = metric;
   }

   @Override
   protected Statement toSetStatement(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new SetMetric(_metric);
   }

}
