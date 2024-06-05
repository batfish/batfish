package org.batfish.representation.frr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.junit.Test;

public class RouteMapMatchIpAddressPrefixListTest {

  @Test
  public void testToBooleanExpr() {
    RouteMapMatchIpAddressPrefixList match =
        new RouteMapMatchIpAddressPrefixList(ImmutableList.of("N1", "N2"));

    FrrConfiguration config = new FrrConfiguration();
    config
        .getIpPrefixLists()
        .putAll(
            ImmutableMap.of(
                "N1", new IpPrefixList("N1"),
                "N2", new IpPrefixList("N2"),
                "N3", new IpPrefixList("N3")));

    assertThat(
        match.toBooleanExpr(null, config, null),
        equalTo(
            new Disjunction(
                new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet("N1")),
                new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet("N2")))));
  }
}
