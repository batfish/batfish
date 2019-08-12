package org.batfish.representation.cumulus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchEntireCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.junit.Test;

public class RouteMapMatchCommunityTest {

  @Test
  public void testGetNames() {
    RouteMapMatchCommunity match = new RouteMapMatchCommunity(ImmutableList.of("M1", "M2", "M3"));
    assertThat(match.getNames(), equalTo(ImmutableList.of("M1", "M2", "M3")));
  }

  @Test
  public void testToBooleanExpr() {
    RouteMapMatchCommunity match = new RouteMapMatchCommunity(ImmutableList.of("M1", "M2", "M3"));
    CumulusNcluConfiguration cumulusNcluConfiguration = new CumulusNcluConfiguration();

    cumulusNcluConfiguration
        .getIpCommunityLists()
        .putAll(
            ImmutableMap.of(
                "M1", new IpCommunityListExpanded("M1", LineAction.PERMIT, ImmutableList.of()),
                "M2", new IpCommunityListExpanded("M2", LineAction.PERMIT, ImmutableList.of())));
    BooleanExpr result = match.toBooleanExpr(null, cumulusNcluConfiguration, null);

    assertThat(
        result,
        equalTo(
            new Disjunction(
                ImmutableList.of(
                    new MatchEntireCommunitySet(new NamedCommunitySet("M1")),
                    new MatchEntireCommunitySet(new NamedCommunitySet("M2"))))));
  }
}
