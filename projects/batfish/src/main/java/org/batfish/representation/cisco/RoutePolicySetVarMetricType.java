package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.SetVarMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.common.Warnings;

public class RoutePolicySetVarMetricType extends RoutePolicyStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _var;

   public RoutePolicySetVarMetricType(String var) {
      _var = var;
   }

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      statements.add(new SetVarMetricType(_var));
   }

}
