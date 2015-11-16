package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.RouteFilterList;

public final class PsFromPrefixList extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public PsFromPrefixList(String name) {
      _name = name;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c,
         Warnings warnings) {
      RouteFilterList list = c.getRouteFilterLists().get(_name);
      if (list == null) {
         warnings.redFlag("Reference to undefined route filter list: \""
               + _name + "\"");
      }
      else {
         PolicyMapMatchRouteFilterListLine line = new PolicyMapMatchRouteFilterListLine(
               Collections.singleton(list));
         clause.getMatchLines().add(line);
      }
   }

   public String getName() {
      return _name;
   }

}
