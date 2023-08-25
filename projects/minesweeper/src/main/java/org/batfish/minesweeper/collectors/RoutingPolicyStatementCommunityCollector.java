package org.batfish.minesweeper.collectors;

import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.aspath.RoutePolicyStatementMatchCollector;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all community-list names in a route-policy {@link Statement}. */
@ParametersAreNonnullByDefault
public class RoutingPolicyStatementCommunityCollector
    extends RoutePolicyStatementMatchCollector<String> {
  public RoutingPolicyStatementCommunityCollector() {
    super(new CommunityBooleanExprCollector());
  }

  @Override
  public Set<String> visitSetCommunities(
      SetCommunities setCommunities, Tuple<Set<String>, Configuration> arg) {
    return setCommunities.getCommunitySetExpr().accept(new CommunitySetExprCollector(), arg);
  }
}
