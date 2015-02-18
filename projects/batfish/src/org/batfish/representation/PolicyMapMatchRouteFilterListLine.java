package org.batfish.representation;

import java.util.Set;

public class PolicyMapMatchRouteFilterListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<RouteFilterList> _lists;

   public PolicyMapMatchRouteFilterListLine(Set<RouteFilterList> lists) {
      _lists = lists;
   }

   public Set<RouteFilterList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.ROUTE_FILTER_LIST;
   }

}
