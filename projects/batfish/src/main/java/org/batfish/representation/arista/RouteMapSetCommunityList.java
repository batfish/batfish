package org.batfish.representation.arista;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

public final class RouteMapSetCommunityList extends RouteMapSetLine {

  public RouteMapSetCommunityList(Iterable<String> communityLists, boolean additive) {
    _additive = additive;
    _communityLists = ImmutableList.copyOf(communityLists);
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    // warn or abort as necessary
    for (String communityListName : _communityLists) {
      if (!cc.getExpandedCommunityLists().containsKey(communityListName)
          && !cc.getStandardCommunityLists().containsKey(communityListName)) {
        // TODO: verify behavior of single undefined reference
        return;
      }
      StandardCommunityList scl = cc.getStandardCommunityLists().get(communityListName);
      if (scl != null) {
        if (scl.getLines().stream().anyMatch(line -> line.getAction() != LineAction.PERMIT)) {
          w.redFlagf(
              "route-map set community community-list ignores deny lines in standard"
                  + " community-list: '%s'",
              communityListName);
        }
      } else {
        w.redFlagf(
            "route-map set community community-list is ignored for expanded community list:"
                + " '%s'",
            communityListName);
      }
    }
    CommunitySetExpr retainedCommunities =
        _additive
            ? InputCommunities.instance()
            : new CommunitySetDifference(
                InputCommunities.instance(), AllStandardCommunities.instance());
    CommunitySetExpr communitiesToSet =
        CommunitySetUnion.of(
            _communityLists.stream()
                .map(CommunitySetReference::new)
                .collect(ImmutableList.toImmutableList()));
    statements.add(new SetCommunities(CommunitySetUnion.of(retainedCommunities, communitiesToSet)));
  }

  public boolean getAdditive() {
    return _additive;
  }

  public @Nonnull List<String> getCommunityLists() {
    return _communityLists;
  }

  private final boolean _additive;
  private final @Nonnull List<String> _communityLists;
}
