package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.main.Warnings;

public class RoutePolicyBooleanTagIs extends RoutePolicyBoolean {

   /**
   *
   */
   private static final long serialVersionUID = 1L;

   private final int _value;

   public RoutePolicyBooleanTagIs(int value) {
      _value = value;
   }

   public int getValue() {
      return _value;
   }

   @Override
   public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {

         return new MatchTag(_value);
   }

}
