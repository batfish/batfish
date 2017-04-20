package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.common.Warnings;

public class RoutePolicySetMed extends RoutePolicySetStatement {

   private static final long serialVersionUID = 1L;

   private IntExpr _med;

   public RoutePolicySetMed(IntExpr intExpr) {
      _med = intExpr;
   }

   public IntExpr getMed() {
      return _med;
   }

   @Override
   protected Statement toSetStatement(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new SetMetric(_med);
   }

}
