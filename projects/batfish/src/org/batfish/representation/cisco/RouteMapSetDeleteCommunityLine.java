package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.datamodel.routing_policy.statement.DeleteCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public class RouteMapSetDeleteCommunityLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private final String _listName;

   private final int _statementLine;

   public RouteMapSetDeleteCommunityLine(String listName, int statementLine) {
      _listName = listName;
      _statementLine = statementLine;
   }

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      CommunityList list = c.getCommunityLists().get(_listName);
      if (list != null) {
         String msg = "match community line";
         StandardCommunityList standardCommunityList = cc
               .getStandardCommunityLists().get(_listName);
         if (standardCommunityList != null) {
            standardCommunityList.getReferers().put(this, msg);
         }
         ExpandedCommunityList expandedCommunityList = cc
               .getExpandedCommunityLists().get(_listName);
         if (expandedCommunityList != null) {
            expandedCommunityList.getReferers().put(this, msg);
         }
         statements.add(new DeleteCommunity(new NamedCommunitySet(_listName)));
      }
      else {
         cc.undefined(CiscoStructureType.COMMUNITY_LIST, _listName,
               CiscoStructureUsage.ROUTE_MAP_DELETE_COMMUNITY, _statementLine);
      }
   }

   public String getListName() {
      return _listName;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.DELETE_COMMUNITY;
   }

}
