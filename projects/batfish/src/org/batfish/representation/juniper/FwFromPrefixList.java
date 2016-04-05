package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.LineAction;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;

public final class FwFromPrefixList implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   public FwFromPrefixList(String name) {
      _name = name;
   }

   public void applyTo(IpAccessListLine line, List<IpAccessListLine> lines,
         JuniperVendorConfiguration jc, Configuration c, Warnings w) {
      PrefixList pl = jc.getPrefixLists().get(_name);
      if (pl != null) {
         pl.getReferers().put(this, "firewall from prefix-list");
         if (pl.getIpv6()) {
            return;
         }
         IpAccessListLine dstLine = line.copy();
         IpAccessListLine srcLine = line.copy();

         RouteFilterList sourcePrefixList = c.getRouteFilterLists().get(_name);
         for (RouteFilterLine rfLine : sourcePrefixList.getLines()) {
            if (rfLine.getAction() != LineAction.ACCEPT) {
               throw new BatfishException(
                     "Expected accept action for routerfilterlist from juniper");
            }
            else {
               srcLine.getSourceIpRanges().add(rfLine.getPrefix());
               dstLine.getDestinationIpRanges().add(rfLine.getPrefix());
            }
         }
         lines.add(srcLine);
         lines.add(dstLine);
      }
      else {
         w.redFlag("Reference to undefined prefix-list: \"" + _name + "\"");
      }
   }

}
