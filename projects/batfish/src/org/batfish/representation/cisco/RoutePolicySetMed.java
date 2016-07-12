package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public class RoutePolicySetMed extends RoutePolicySetStatement {

   private static final long serialVersionUID = 1L;

   private int _med;

   public RoutePolicySetMed(int med) {
      _med = med;
   }

   public int getMed() {
      return _med;
   }

   @Override
   protected Statement toSetStatement(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new SetMetric(_med);
   }

}
