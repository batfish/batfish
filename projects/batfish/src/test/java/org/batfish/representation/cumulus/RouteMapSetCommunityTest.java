package org.batfish.representation.cumulus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.junit.Test;

public class RouteMapSetCommunityTest {

  @Test
  public void toStatements() {
    List<StandardCommunity> communities =
        ImmutableList.of(StandardCommunity.parse("65000:1"), StandardCommunity.parse("65000:2"));
    RouteMapSetCommunity set = new RouteMapSetCommunity(communities, false);

    assertThat(
        set.toStatements(null, null, null).collect(ImmutableList.toImmutableList()),
        equalTo(
            ImmutableList.of(
                new SetCommunities(
                    CommunitySetUnion.of(
                        new CommunitySetExpr[] {
                          new CommunitySetDifference(
                              InputCommunities.instance(), AllStandardCommunities.instance()),
                          new LiteralCommunitySet(CommunitySet.of(communities))
                        })))));
  }
}
