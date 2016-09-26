package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.main.Warnings;

public class RoutePolicyBooleanASPathIn extends RoutePolicyBoolean {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final String _name;

   public RoutePolicyBooleanASPathIn(String text) {
      _name = text;
   }

   @Override
   public RoutePolicyBooleanType getType() {
      return RoutePolicyBooleanType.ASPATHIN;
   }

   @Override
   public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated method stub
   }

   public String getName() {
      return _name;
   }

}
