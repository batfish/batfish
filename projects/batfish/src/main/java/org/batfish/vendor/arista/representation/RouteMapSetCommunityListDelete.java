package org.batfish.vendor.arista.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetCommunityListDelete extends RouteMapSetLine {

  private final List<String> _communityLists;

  public RouteMapSetCommunityListDelete(Iterable<String> communityLists) {
    _communityLists = ImmutableList.copyOf(communityLists);
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    // abort as necessary
    for (String communityListName : _communityLists) {
      if (!cc.getExpandedCommunityLists().containsKey(communityListName)
          && !cc.getStandardCommunityLists().containsKey(communityListName)) {
        // TODO: verify behavior of single undefined reference
        return;
      }
    }
    CommunityMatchExpr shouldDelete =
        CommunityMatchAny.matchAny(
            _communityLists.stream()
                .map(CommunityMatchExprReference::new)
                .collect(ImmutableList.toImmutableList()));
    statements.add(
        new SetCommunities(new CommunitySetDifference(InputCommunities.instance(), shouldDelete)));
  }

  public List<String> getCommunityLists() {
    return _communityLists;
  }
}
