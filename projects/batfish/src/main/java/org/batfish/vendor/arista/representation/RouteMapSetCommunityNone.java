package org.batfish.vendor.arista.representation;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetCommunityNone extends RouteMapSetLine {

  public static @Nonnull RouteMapSetCommunityNone instance() {
    return INSTANCE;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    statements.add(
        new SetCommunities(
            new CommunitySetDifference(
                InputCommunities.instance(), AllStandardCommunities.instance())));
  }

  private static RouteMapSetCommunityNone INSTANCE = new RouteMapSetCommunityNone();

  private RouteMapSetCommunityNone() {}
}
