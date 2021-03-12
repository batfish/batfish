package org.batfish.representation.cisco_asa;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetCommunityLine extends RouteMapSetLine {

  private List<Long> _communities;

  public RouteMapSetCommunityLine(List<Long> communities) {
    _communities = communities;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AsaConfiguration cc, Configuration c, Warnings w) {
    statements.add(
        new SetCommunity(
            new LiteralCommunitySet(
                _communities.stream()
                    .map(StandardCommunity::of)
                    .collect(ImmutableSet.toImmutableSet()))));
  }

  public List<Long> getCommunities() {
    return _communities;
  }
}
