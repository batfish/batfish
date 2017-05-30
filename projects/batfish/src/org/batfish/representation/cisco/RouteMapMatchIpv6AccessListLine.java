package org.batfish.representation.cisco;

import java.util.List;
import java.util.Set;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork6;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchIp6AccessList;
import org.batfish.datamodel.routing_policy.expr.MatchPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.NamedPrefix6Set;
import org.batfish.common.Warnings;

public class RouteMapMatchIpv6AccessListLine extends RouteMapMatchLine {

   private static final long serialVersionUID = 1L;

   private final Set<String> _listNames;

   private boolean _routing;

   private final int _statementLine;

   public RouteMapMatchIpv6AccessListLine(Set<String> names,
         int statementLine) {
      _listNames = names;
      _statementLine = statementLine;
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
         Ip6AccessList ipAccessList = null;
         Route6FilterList routeFilterList = null;
         if (_routing) {
            routeFilterList = c.getRoute6FilterLists().get(listName);
            list = routeFilterList;
         }
         else {
            ipAccessList = c.getIp6AccessLists().get(listName);
            list = ipAccessList;
         }
         if (list == null) {
            cc.undefined(CiscoStructureType.IPV6_ACCESS_LIST, listName,
                  CiscoStructureUsage.ROUTE_MAP_MATCH_IPV6_ACCESS_LIST,
                  _statementLine);
         }
         else {
            String msg = "route-map match ipv6 access-list line";
            ExtendedIpv6AccessList extendedAccessList = cc.getExtendedIpv6Acls()
                  .get(listName);
            if (extendedAccessList != null) {
               extendedAccessList.getReferers().put(this, msg);
            }
            StandardIpv6AccessList standardAccessList = cc.getStandardIpv6Acls()
                  .get(listName);
            if (standardAccessList != null) {
               standardAccessList.getReferers().put(this, msg);
            }
            if (_routing) {
               disjuncts.add(new MatchPrefix6Set(new DestinationNetwork6(),
                     new NamedPrefix6Set(listName)));
            }
            else {
               disjuncts.add(new MatchIp6AccessList(listName));
            }
         }
      }
      return d.simplify();
   }

}
