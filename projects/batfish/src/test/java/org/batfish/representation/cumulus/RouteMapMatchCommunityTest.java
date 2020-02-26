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
}
