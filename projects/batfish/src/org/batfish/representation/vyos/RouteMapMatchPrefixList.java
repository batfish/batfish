package org.batfish.representation.vyos;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.main.Warnings;

public class RouteMapMatchPrefixList implements RouteMapMatch {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _prefixList;

   public RouteMapMatchPrefixList(String prefixList) {
      _prefixList = prefixList;
   }

   public String getPrefixList() {
      return _prefixList;
   }

   @Override
   public BooleanExpr toBooleanExpr(VyosConfiguration vc, Configuration c,
         Warnings w) {
      PrefixList pl = vc.getPrefixLists().get(_prefixList);
      if (pl != null) {
         pl.getReferers().put(vc, "used in route-map match prefix-list");
         return new MatchPrefixSet(new NamedPrefixSet(_prefixList));
      }
      else {
         vc.undefined(
               "Reference to undefined prefix-list: '" + _prefixList + "'",
               VyosVendorConfiguration.PREFIX_LIST, _prefixList);
         // TODO: see if vyos treats as true, false, or disallows
         return BooleanExprs.True.toStaticBooleanExpr();
      }
   }

}
