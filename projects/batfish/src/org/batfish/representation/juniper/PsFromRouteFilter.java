package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchRouteFilterListLine;
import org.batfish.datamodel.RouteFilterList;
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

   @Override
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
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
