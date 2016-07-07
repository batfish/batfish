package org.batfish.representation.cisco;

import java.util.List;
import java.util.Set;

import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchCommunityList;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;

public class RouteMapMatchCommunityListLine extends RouteMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<String> _listNames;

   public RouteMapMatchCommunityListLine(Set<String> names) {
      _listNames = names;
   }

   public Set<String> getListNames() {
      return _listNames;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.COMMUNITY_LIST;
   }

   @Override
   public BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc,
         Warnings w) {
      Disjunction d = new Disjunction();
      List<BooleanExpr> disjuncts = d.getDisjuncts();
      for (String listName : _listNames) {
         CommunityList list = c.getCommunityLists().get(listName);
         if (list != null) {
            String msg = "match community line";
            StandardCommunityList standardCommunityList = cc
                  .getStandardCommunityLists().get(listName);
            if (standardCommunityList != null) {
               standardCommunityList.getReferers().put(this, msg);
            }
            ExpandedCommunityList expandedCommunityList = cc
                  .getExpandedCommunityLists().get(listName);
            if (expandedCommunityList != null) {
               expandedCommunityList.getReferers().put(this, msg);
            }
            disjuncts.add(new MatchCommunityList(listName));
         }
         else {
            w.redFlag("Reference to undefined community-list: " + listName,
                  VendorConfiguration.UNDEFINED);
         }
      }
      return d.simplify();
   }

}
