package org.batfish.vendor.arista.representation;

import com.google.common.collect.ImmutableList;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;

/**
 * Handles the "route-map match community-list" command, which matches when at least one of the
 * named lists matches at least one community in the advertisement.
 */
public class RouteMapMatchCommunity extends RouteMapMatchLine {

  private final Set<String> _listNames;

  public RouteMapMatchCommunity(Set<String> names) {
    _listNames = names;
  }

  public Set<String> getListNames() {
    return _listNames;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, AristaConfiguration cc, Warnings w) {
    return new Disjunction(
        _listNames.stream()
            .filter(c.getCommunitySetMatchExprs()::containsKey)
            .map(
                name ->
                    new MatchCommunities(
                        InputCommunities.instance(), new CommunitySetMatchExprReference(name)))
            .collect(ImmutableList.toImmutableList()));
  }
}
