package org.batfish.grammar.flatvyos;

import java.util.Collections;

import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.vyos.RouteMapMatch;

public class RouteMapMatchPrefixList implements RouteMapMatch {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _prefixList;

   public RouteMapMatchPrefixList(String prefixList) {
      _prefixList = prefixList;
   }

   @Override
   public void applyTo(Configuration c, PolicyMap policyMap,
         PolicyMapClause clause, Warnings w) {
      RouteFilterList routeFilterList = c.getRouteFilterLists()
            .get(_prefixList);
      if (routeFilterList == null) {
         w.redFlag("Reference to undefined prefix-list: \"" + _prefixList
               + "\"");
      }
      else {
         PolicyMapMatchRouteFilterListLine line = new PolicyMapMatchRouteFilterListLine(
               Collections.singleton(routeFilterList));
         clause.getMatchLines().add(line);
      }
   }

   public String getPrefixList() {
      return _prefixList;
   }

}
