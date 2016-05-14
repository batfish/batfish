package org.batfish.datamodel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class PolicyMapMatchRouteFilterListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<RouteFilterList> _lists;

   public PolicyMapMatchRouteFilterListLine(Set<RouteFilterList> lists) {
      _lists = lists;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Set<RouteFilterList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.ROUTE_FILTER_LIST;
   }

}
