package org.batfish.representation.cumulus;

import static org.hamcrest.Matchers.contains;
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
  public void testGetMatches_Interface() {
    RouteMapEntry entry = new RouteMapEntry(10, LineAction.DENY);
    entry.setMatchInterface(new RouteMapMatchInterface(ImmutableSet.of("interface")));

    ImmutableList<RouteMapMatch> matches =
        entry.getMatches().collect(ImmutableList.toImmutableList());

    assertThat(matches, contains(isA(RouteMapMatchInterface.class)));
  }

  @Test
  public void testGetMatches_Community() {
    RouteMapEntry entry = new RouteMapEntry(10, LineAction.DENY);
    entry.setMatchCommunity(new RouteMapMatchCommunity(ImmutableSet.of("community")));

    ImmutableList<RouteMapMatch> matches =
        entry.getMatches().collect(ImmutableList.toImmutableList());

    assertThat(matches, contains(isA(RouteMapMatchCommunity.class)));
  }

  @Test
  public void testGetMatches_PrefixList() {
    RouteMapEntry entry = new RouteMapEntry(10, LineAction.DENY);
    entry.setMatchIpAddressPrefixList(
        new RouteMapMatchIpAddressPrefixList(ImmutableSet.of("prefix-list")));

    ImmutableList<RouteMapMatch> matches =
        entry.getMatches().collect(ImmutableList.toImmutableList());

    assertThat(matches, contains(isA(RouteMapMatchIpAddressPrefixList.class)));
  }

  @Test
  public void testGetSets_Metric() {
    RouteMapEntry entry = new RouteMapEntry(10, LineAction.DENY);
    entry.setSetMetric(new RouteMapSetMetric(100));

    ImmutableList<RouteMapSet> sets = entry.getSets().collect(ImmutableList.toImmutableList());

    assertThat(sets, contains(isA(RouteMapSetMetric.class)));
  }

  @Test
  public void testGetSets_NextHop() {
    RouteMapEntry entry = new RouteMapEntry(10, LineAction.DENY);
    entry.setSetIpNextHop(new RouteMapSetIpNextHopLiteral(Ip.parse("10.0.0.1")));

    ImmutableList<RouteMapSet> sets = entry.getSets().collect(ImmutableList.toImmutableList());

    assertThat(sets, contains(isA(RouteMapSetIpNextHopLiteral.class)));
  }

  @Test
  public void testGetSets_Community() {
    RouteMapEntry entry = new RouteMapEntry(10, LineAction.DENY);
    entry.setSetCommunity(
        new RouteMapSetCommunity(ImmutableSet.of(StandardCommunity.of(60000, 10))));

    ImmutableList<RouteMapSet> sets = entry.getSets().collect(ImmutableList.toImmutableList());

    assertThat(sets, contains(isA(RouteMapSetCommunity.class)));
  }
}
