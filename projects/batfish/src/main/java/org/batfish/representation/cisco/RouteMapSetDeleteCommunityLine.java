package org.batfish.representation.cisco;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetDeleteCommunityLine extends RouteMapSetLine {

  private final String _listName;

  public RouteMapSetDeleteCommunityLine(String listName) {
    _listName = listName;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    if (!c.getCommunityMatchExprs().containsKey(_listName)) {
      return;
    }
    statements.add(
        new SetCommunities(
            new CommunitySetDifference(
                InputCommunities.instance(), new CommunityMatchExprReference(_listName))));
  }

  public String getListName() {
    return _listName;
  }
}
