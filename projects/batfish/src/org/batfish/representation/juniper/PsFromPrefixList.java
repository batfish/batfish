package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.VendorConversionException;

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
   public void applyTo(PolicyMapClause clause, Configuration c) {
      RouteFilterList list = c.getRouteFilterLists().get(_name);
      if (list == null) {
         throw new VendorConversionException(
               "missing route filter list from prefix list: \"" + _name + "\"");
      }
      PolicyMapMatchRouteFilterListLine line = new PolicyMapMatchRouteFilterListLine(
            Collections.singleton(list));
      clause.getMatchLines().add(line);
   }

   public String getName() {
      return _name;
   }

}
