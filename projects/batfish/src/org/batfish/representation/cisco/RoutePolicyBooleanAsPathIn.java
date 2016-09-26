package org.batfish.representation.cisco;

import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.main.Warnings;

public class RoutePolicyBooleanAsPathIn extends RoutePolicyBoolean {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public RoutePolicyBooleanAsPathIn(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   @Override
   public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      AsPathAccessList acl = c.getAsPathAccessLists().get(_name);
      if (acl != null) {
         MatchAsPath match = new MatchAsPath(new NamedAsPathSet(_name));
         return match;
      }
      else {
         cc.undefined("Reference to undefined ip as-path access-list: " + _name,
               CiscoVendorConfiguration.AS_PATH_ACCESS_LIST, _name);
         return BooleanExprs.False.toStaticBooleanExpr();
      }
   }

}
