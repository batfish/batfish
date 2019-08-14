package org.batfish.representation.cumulus;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

public class RouteMapEntryTest {

  @Test
  public void testGetMatches() {
    RouteMapEntry entry = new RouteMapEntry(10, LineAction.DENY);
    entry.setMatchInterface(new RouteMapMatchInterface(ImmutableSet.of("interface")));
    entry.setMatchCommunity(new RouteMapMatchCommunity(ImmutableSet.of("community")));
    entry.setMatchIpAddressPrefixList(
        new RouteMapMatchIpAddressPrefixList(ImmutableSet.of("prefix-list")));

    ImmutableList<RouteMapMatch> matches =
        entry.getMatches().collect(ImmutableList.toImmutableList());
    assertThat(matches.size(), equalTo(3));

    assertThat(matches.get(0), isA(RouteMapMatchInterface.class));
    assertThat(
        ((RouteMapMatchInterface) matches.get(0)).getInterfaces(),
        equalTo(ImmutableList.of("interface")));

    assertThat(matches.get(1), isA(RouteMapMatchCommunity.class));
    assertThat(
        ((RouteMapMatchCommunity) matches.get(1)).getNames(),
        equalTo(ImmutableList.of("community")));

    assertThat(matches.get(2), isA(RouteMapMatchIpAddressPrefixList.class));
    assertThat(
        ((RouteMapMatchIpAddressPrefixList) matches.get(2)).getNames(),
        equalTo(ImmutableList.of("prefix-list")));
  }

  @Test
  public void testGetSets() {
    RouteMapEntry entry = new RouteMapEntry(10, LineAction.DENY);
    entry.setSetMetric(new RouteMapSetMetric(10));
    entry.setSetIpNextHop(new RouteMapSetIpNextHopLiteral(Ip.parse("10.0.0.1")));
    entry.setSetCommunity(
        new RouteMapSetCommunity(ImmutableSet.of(StandardCommunity.of(60000, 10))));

    ImmutableList<RouteMapSet> sets = entry.getSets().collect(ImmutableList.toImmutableList());
    assertThat(sets.size(), equalTo(3));

    assertThat(sets.get(0), isA(RouteMapSetMetric.class));
    assertThat(((RouteMapSetMetric) sets.get(0)).getMetric(), equalTo(10L));

    assertThat(sets.get(1), isA(RouteMapSetIpNextHopLiteral.class));
    assertThat(
        ((RouteMapSetIpNextHopLiteral) sets.get(1)).getNextHop(), equalTo(Ip.parse("10.0.0.1")));

    assertThat(sets.get(2), isA(RouteMapSetCommunity.class));
    assertThat(
        ((RouteMapSetCommunity) sets.get(2)).getCommunities(),
        equalTo(ImmutableList.of(StandardCommunity.of(60000, 10))));
  }
}
