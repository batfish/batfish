package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.VendorConversionException;

public final class PsFromRouteFilter extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _routeFilterName;

   public PsFromRouteFilter(String routeFilterName) {
      _routeFilterName = routeFilterName;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      RouteFilterList rfl = c.getRouteFilterLists().get(_routeFilterName);
      if (rfl == null) {
         throw new VendorConversionException("missing route filter list: \""
               + _routeFilterName + "\"");
      }
      clause.getMatchLines().add(
            new PolicyMapMatchRouteFilterListLine(Collections.singleton(rfl)));
   }

   public String getRouteFilterName() {
      return _routeFilterName;
   }

}
