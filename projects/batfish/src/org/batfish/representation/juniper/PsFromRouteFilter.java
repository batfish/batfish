package org.batfish.representation.juniper;

import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.main.Warnings;

public final class PsFromRouteFilter extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _routeFilterName;

   public PsFromRouteFilter(String routeFilterName) {
      _routeFilterName = routeFilterName;
   }

   public String getRouteFilterName() {
      return _routeFilterName;
   }

   @Override
   public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c,
         Warnings warnings) {
      RouteFilterList rfl = c.getRouteFilterLists().get(_routeFilterName);
      if (rfl != null) {
         return new MatchPrefixSet(new DestinationNetwork(),
               new NamedPrefixSet(_routeFilterName));
      }
      else {
         throw new VendorConversionException(
               "missing route filter list: \"" + _routeFilterName + "\"");
      }
   }
}
