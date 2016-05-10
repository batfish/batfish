package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.common.util.SubRange;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.LineAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;

public final class PsFromPrefixListFilterOrLonger extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _prefixList;

   public PsFromPrefixListFilterOrLonger(String prefixList) {
      _prefixList = prefixList;
   }

   @Override
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
      PrefixList pl = jc.getPrefixLists().get(_prefixList);
      if (pl != null) {
         pl.getReferers().put(this, "from prefix-list-filter or-longer");
         if (pl.getIpv6()) {
            return;
         }
         RouteFilterList rf = c.getRouteFilterLists().get(_prefixList);
         String orLongerListName = "~" + _prefixList + "~ORLONGER~";
         RouteFilterList orLongerList = c.getRouteFilterLists().get(
               orLongerListName);
         if (orLongerList == null) {
            orLongerList = new RouteFilterList(orLongerListName);
            for (RouteFilterLine line : rf.getLines()) {
               Prefix prefix = line.getPrefix();
               LineAction action = line.getAction();
               SubRange orLongerLineRange = new SubRange(line.getLengthRange()
                     .getStart(), 32);
               RouteFilterLine orLongerLine = new RouteFilterLine(action,
                     prefix, orLongerLineRange);
               orLongerList.addLine(orLongerLine);
               c.getRouteFilterLists().put(orLongerListName, orLongerList);
            }
         }
         PolicyMapMatchRouteFilterListLine pmLine = new PolicyMapMatchRouteFilterListLine(
               Collections.singleton(orLongerList));
         clause.getMatchLines().add(pmLine);
      }
      else {
         warnings.redFlag("Reference to undefined prefix-list: \""
               + _prefixList + "\"");
      }
   }
}
