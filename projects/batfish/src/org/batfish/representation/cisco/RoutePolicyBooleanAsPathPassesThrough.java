package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.PassesThroughAsPath;
import org.batfish.main.Warnings;

public class RoutePolicyBooleanAsPathPassesThrough extends RoutePolicyBoolean {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private boolean _exact;

   private List<AsExpr> _list;

   public RoutePolicyBooleanAsPathPassesThrough(List<AsExpr> list,
         boolean exact) {
      _list = list;
      _exact = exact;
   }

   @Override
   public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new PassesThroughAsPath(_list, _exact);
   }

}
