package org.batfish.datamodel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapMatchRouteFilterListLine extends PolicyMapMatchLine {

   private static final String LISTS_VAR = "lists";

   private static final long serialVersionUID = 1L;

   private Set<RouteFilterList> _lists;

   @JsonCreator
   public PolicyMapMatchRouteFilterListLine() {
   }

   public PolicyMapMatchRouteFilterListLine(Set<RouteFilterList> lists) {
      _lists = lists;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(LISTS_VAR)
   public Set<RouteFilterList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.ROUTE_FILTER_LIST;
   }

   @JsonProperty(LISTS_VAR)
   public void setLists(Set<RouteFilterList> lists) {
      _lists = lists;
   }

}
