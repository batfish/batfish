package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchRouteFilterListLine;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchRouteFilter;
import org.batfish.main.Warnings;

public final class PsFromPrefixListFilterLonger extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _prefixList;

   public PsFromPrefixListFilterLonger(String prefixList) {
      _prefixList = prefixList;
   }

   @Override
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
      PrefixList pl = jc.getPrefixLists().get(_prefixList);
      if (pl != null) {
         pl.getReferers().put(this, "from prefix-list-filter longer");
         if (pl.getIpv6()) {
            return;
         }
         RouteFilterList rf = c.getRouteFilterLists().get(_prefixList);
         String longerListName = "~" + _prefixList + "~LONGER~";
         RouteFilterList longerList = c.getRouteFilterLists().get(
               longerListName);
         if (longerList == null) {
            longerList = new RouteFilterList(longerListName);
            for (RouteFilterLine line : rf.getLines()) {
               Prefix prefix = line.getPrefix();
               LineAction action = line.getAction();
               SubRange longerLineRange = new SubRange(line.getLengthRange()
                     .getStart() + 1, 32);
               if (longerLineRange.getStart() > 32) {
                  warnings.redFlag("'prefix-list-filter " + _prefixList
                        + " longer' cannot match more specific prefix than "
                        + prefix.toString());
                  continue;
               }
               RouteFilterLine orLongerLine = new RouteFilterLine(action,
                     prefix, longerLineRange);
               longerList.addLine(orLongerLine);
               c.getRouteFilterLists().put(longerListName, longerList);
            }
         }
         PolicyMapMatchRouteFilterListLine pmLine = new PolicyMapMatchRouteFilterListLine(
               Collections.singleton(longerList));
         clause.getMatchLines().add(pmLine);
      }
      else {
         warnings.redFlag("Reference to undefined prefix-list: \""
               + _prefixList + "\"");
      }
   }

   @Override
   public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c,
         Warnings warnings) {
      PrefixList pl = jc.getPrefixLists().get(_prefixList);
      if (pl != null) {
         pl.getReferers().put(this, "from prefix-list-filter longer");
         if (pl.getIpv6()) {
            return BooleanExprs.False.toStaticBooleanExpr();
         }
         RouteFilterList rf = c.getRouteFilterLists().get(_prefixList);
         String longerListName = "~" + _prefixList + "~LONGER~";
         RouteFilterList longerList = c.getRouteFilterLists().get(
               longerListName);
         if (longerList == null) {
            longerList = new RouteFilterList(longerListName);
            for (RouteFilterLine line : rf.getLines()) {
               Prefix prefix = line.getPrefix();
               LineAction action = line.getAction();
               SubRange longerLineRange = new SubRange(line.getLengthRange()
                     .getStart() + 1, 32);
               if (longerLineRange.getStart() > 32) {
                  warnings.redFlag("'prefix-list-filter " + _prefixList
                        + " longer' cannot match more specific prefix than "
                        + prefix.toString());
                  continue;
               }
               RouteFilterLine orLongerLine = new RouteFilterLine(action,
                     prefix, longerLineRange);
               longerList.addLine(orLongerLine);
               c.getRouteFilterLists().put(longerListName, longerList);
            }
         }
         return new MatchRouteFilter(longerListName);
      }
      else {
         warnings.redFlag("Reference to undefined prefix-list: \""
               + _prefixList + "\"");
         return BooleanExprs.False.toStaticBooleanExpr();
      }
   }
}
