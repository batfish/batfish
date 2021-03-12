package org.batfish.representation.cisco_asa;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.datamodel.routing_policy.statement.DeleteCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetDeleteCommunityLine extends RouteMapSetLine {

  private final String _listName;

  public RouteMapSetDeleteCommunityLine(String listName) {
    _listName = listName;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AsaConfiguration cc, Configuration c, Warnings w) {
    CommunityList list = c.getCommunityLists().get(_listName);
    if (list != null) {
      statements.add(new DeleteCommunity(new NamedCommunitySet(_listName)));
    }
  }

  public String getListName() {
    return _listName;
  }
}
