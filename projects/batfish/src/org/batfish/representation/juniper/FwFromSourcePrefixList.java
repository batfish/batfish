package org.batfish.representation.juniper;

import org.batfish.common.BatfishException;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.LineAction;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;

public final class FwFromSourcePrefixList extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public FwFromSourcePrefixList(String name) {
      _name = name;
   }

   @Override
   public void applyTo(IpAccessListLine line, Warnings w, Configuration c) {
      RouteFilterList sourcePrefixList = c.getRouteFilterLists().get(_name);
      if (sourcePrefixList != null) {
         for (RouteFilterLine rfLine : sourcePrefixList.getLines()) {
            if (rfLine.getAction() != LineAction.ACCEPT) {
               throw new BatfishException(
                     "Expected accept action for routerfilterlist from juniper");
            }
            else {
               line.getSourceIpRanges().add(rfLine.getPrefix());
            }
         }
      }
      else {
         w.redFlag("Reference to undefined source prefix-list: \"" + _name
               + "\"");
      }
   }

}
