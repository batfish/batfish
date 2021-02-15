package org.batfish.representation.cumulus_nclu;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** Clause of set comm-list delete in route map. */
public class RouteMapSetCommListDelete implements RouteMapSet {

  @Nonnull private final String _name;

  public RouteMapSetCommListDelete(@Nonnull String name) {
    _name = name;
  }

  @Nonnull
  @Override
  public Stream<Statement> toStatements(Configuration c, CumulusNcluConfiguration vc, Warnings w) {
    return Stream.of(
        new SetCommunities(
            new CommunitySetDifference(
                InputCommunities.instance(), new CommunityMatchExprReference(_name))));
  }

  public @Nonnull String getName() {
    return _name;
  }
}
