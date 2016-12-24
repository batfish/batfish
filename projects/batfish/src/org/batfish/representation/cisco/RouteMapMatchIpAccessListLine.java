package org.batfish.representation.cisco;

import java.util.List;
import java.util.Set;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchIpAccessList;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.main.Warnings;

public class RouteMapMatchIpAccessListLine extends RouteMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<String> _listNames;

   private boolean _routing;

   public RouteMapMatchIpAccessListLine(Set<String> names) {
      _listNames = names;
   }

   public Set<String> getListNames() {
      return _listNames;
   }

   public boolean getRouting() {
      return _routing;
   }

   public void setRouting(boolean routing) {
      _routing = routing;
   }

   @Override
   public BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc,
         Warnings w) {
      Disjunction d = new Disjunction();
      List<BooleanExpr> disjuncts = d.getDisjuncts();
      for (String listName : _listNames) {
         Object list;
         IpAccessList ipAccessList = null;
         RouteFilterList routeFilterList = null;
         if (_routing) {
            routeFilterList = c.getRouteFilterLists().get(listName);
            list = routeFilterList;
         }
         else {
            ipAccessList = c.getIpAccessLists().get(listName);
            list = ipAccessList;
         }
         if (list == null) {
            cc.undefined("Reference to undefined ip access-list: " + listName,
                  CiscoConfiguration.IP_ACCESS_LIST, listName);
         }
         else {
            String msg = "route-map match ip access-list line";
            ExtendedAccessList extendedAccessList = cc.getExtendedAcls()
                  .get(listName);
            if (extendedAccessList != null) {
               extendedAccessList.getReferers().put(this, msg);
            }
            StandardAccessList standardAccessList = cc.getStandardAcls()
                  .get(listName);
            if (standardAccessList != null) {
               standardAccessList.getReferers().put(this, msg);
            }
            if (_routing) {
               disjuncts.add(new MatchPrefixSet(new DestinationNetwork(),
                     new NamedPrefixSet(listName)));
            }
            else {
               disjuncts.add(new MatchIpAccessList(listName));
            }
         }
      }
      return d.simplify();
   }

}
