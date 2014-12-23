package batfish.representation.juniper;

import java.util.Collections;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapClause;
import batfish.representation.PolicyMapMatchRouteFilterListLine;
import batfish.representation.RouteFilterList;
import batfish.representation.VendorConversionException;

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
