package org.batfish.representation.cisco;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

public final class RouteMapSetAdditiveCommunityListLine extends RouteMapSetLine {

  private final Set<String> _communityLists;

  public RouteMapSetAdditiveCommunityListLine(Set<String> communityLists) {
    _communityLists = communityLists;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    SortedSet<Long> communities = new TreeSet<>();
    for (String communityListName : _communityLists) {
      CommunityList communityList = c.getCommunityLists().get(communityListName);
      if (communityList != null) {
        StandardCommunityList scl = cc.getStandardCommunityLists().get(communityListName);
        if (scl != null) {
          for (StandardCommunityListLine line : scl.getLines()) {
            if (line.getAction() == LineAction.PERMIT) {
              communities.addAll(line.getCommunities());
            } else {
              w.redFlag(
                  "Expected only permit lines in standard community-list referred to by route-map "
                      + "set community community-list line: \""
                      + communityListName
                      + "\"");
            }
          }
        } else {
          w.redFlag(
              "Expected standard community list in route-map set community community-list line "
                  + "but got expanded instead: \""
                  + communityListName
                  + "\"");
        }
      }
    }
    _communityLists.forEach(
        communityListName ->
            statements.add(
                new AddCommunity(
                    new org.batfish.datamodel.routing_policy.expr.NamedCommunitySet(
                        communityListName))));
  }
}
