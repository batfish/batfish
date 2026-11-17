package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.junit.jupiter.api.Test;

/** Tests for {@link PsThens} last-wins and cumulative semantics per the Junos commit-time model. */
class PsThensTest {

  // ==========================================================================
  // §1: Scalar last-wins
  // ==========================================================================

  @Test
  void testOriginDedup() {
    PsThens ps = new PsThens();
    PsThenOrigin igp = new PsThenOrigin(OriginType.IGP);
    assertThat(ps.addPsThen(igp), empty());
    assertThat(ps.addPsThen(igp), contains("origin (dedup)"));
    assertEquals(ImmutableList.of(igp), ps.getAllThens());
  }

  @Test
  void testOriginLastWins() {
    PsThens ps = new PsThens();
    PsThenOrigin igp = new PsThenOrigin(OriginType.IGP);
    PsThenOrigin egp = new PsThenOrigin(OriginType.EGP);
    assertThat(ps.addPsThen(igp), empty());
    assertThat(ps.addPsThen(egp), contains("origin"));
    assertEquals(ImmutableList.of(egp), ps.getAllThens());
  }

  @Test
  void testPreferenceDedup() {
    PsThens ps = new PsThens();
    PsThenPreference pref = new PsThenPreference(100, PsThenPreference.Operator.SET);
    assertThat(ps.addPsThen(pref), empty());
    assertThat(ps.addPsThen(pref), contains("preference (dedup)"));
    assertEquals(ImmutableList.of(pref), ps.getAllThens());
  }

  @Test
  void testPreferenceLastWins() {
    PsThens ps = new PsThens();
    PsThenPreference p100 = new PsThenPreference(100, PsThenPreference.Operator.SET);
    PsThenPreference p200 = new PsThenPreference(200, PsThenPreference.Operator.SET);
    assertThat(ps.addPsThen(p100), empty());
    assertThat(ps.addPsThen(p200), contains("preference"));
    assertEquals(ImmutableList.of(p200), ps.getAllThens());
  }

  @Test
  void testTagDedup() {
    PsThens ps = new PsThens();
    PsThenTag tag = new PsThenTag(100);
    assertThat(ps.addPsThen(tag), empty());
    assertThat(ps.addPsThen(tag), contains("tag (dedup)"));
    assertEquals(ImmutableList.of(tag), ps.getAllThens());
  }

  @Test
  void testTagLastWins() {
    PsThens ps = new PsThens();
    PsThenTag t100 = new PsThenTag(100);
    PsThenTag t200 = new PsThenTag(200);
    assertThat(ps.addPsThen(t100), empty());
    assertThat(ps.addPsThen(t200), contains("tag"));
    assertEquals(ImmutableList.of(t200), ps.getAllThens());
  }

  @Test
  void testNextHopIpDedup() {
    PsThens ps = new PsThens();
    PsThenNextHopIp nh = new PsThenNextHopIp(Ip.parse("192.0.2.1"));
    assertThat(ps.addPsThen(nh), empty());
    // PsThenNextHopIp doesn't implement equals, so this won't be a dedup — it's the same instance
    assertThat(ps.addPsThen(nh), contains("next-hop (dedup)"));
    assertEquals(ImmutableList.of(nh), ps.getAllThens());
  }

  @Test
  void testNextHopIpLastWins() {
    PsThens ps = new PsThens();
    PsThenNextHopIp nh1 = new PsThenNextHopIp(Ip.parse("192.0.2.1"));
    PsThenNextHopIp nh2 = new PsThenNextHopIp(Ip.parse("192.0.2.2"));
    assertThat(ps.addPsThen(nh1), empty());
    assertThat(ps.addPsThen(nh2), contains("next-hop"));
    assertEquals(ImmutableList.of(nh2), ps.getAllThens());
  }

  @Test
  void testNextHopSelfVsIp() {
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenNextHopSelf.INSTANCE), empty());
    PsThenNextHopIp nhIp = new PsThenNextHopIp(Ip.parse("192.0.2.1"));
    assertThat(ps.addPsThen(nhIp), contains("next-hop"));
    assertEquals(ImmutableList.of(nhIp), ps.getAllThens());
  }

  @Test
  void testNextHopSelfVsPeer() {
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenNextHopSelf.INSTANCE), empty());
    assertThat(ps.addPsThen(PsThenNextHopPeerAddress.INSTANCE), contains("next-hop"));
    assertEquals(ImmutableList.of(PsThenNextHopPeerAddress.INSTANCE), ps.getAllThens());
  }

  @Test
  void testNextHopRejectVsDiscard() {
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenNextHopReject.INSTANCE), empty());
    assertThat(ps.addPsThen(PsThenNextHopDiscard.INSTANCE), contains("next-hop"));
    assertEquals(ImmutableList.of(PsThenNextHopDiscard.INSTANCE), ps.getAllThens());
  }

  @Test
  void testExternalDedup() {
    PsThens ps = new PsThens();
    PsThenExternal e1 = new PsThenExternal(OspfMetricType.E1);
    assertThat(ps.addPsThen(e1), empty());
    assertThat(ps.addPsThen(e1), contains("external (dedup)"));
    assertEquals(ImmutableList.of(e1), ps.getAllThens());
  }

  @Test
  void testExternalLastWins() {
    PsThens ps = new PsThens();
    PsThenExternal e1 = new PsThenExternal(OspfMetricType.E1);
    PsThenExternal e2 = new PsThenExternal(OspfMetricType.E2);
    assertThat(ps.addPsThen(e1), empty());
    assertThat(ps.addPsThen(e2), contains("external"));
    assertEquals(ImmutableList.of(e2), ps.getAllThens());
  }

  @Test
  void testLoadBalanceLastWins() {
    PsThens ps = new PsThens();
    PsThenLoadBalance perPacket =
        new PsThenLoadBalance(PsThenLoadBalance.LoadBalanceMethod.PER_PACKET);
    PsThenLoadBalance consistentHash =
        new PsThenLoadBalance(PsThenLoadBalance.LoadBalanceMethod.CONSISTENT_HASH);
    assertThat(ps.addPsThen(perPacket), empty());
    assertThat(ps.addPsThen(consistentHash), contains("load-balance"));
    assertEquals(ImmutableList.of(consistentHash), ps.getAllThens());
  }

  // ==========================================================================
  // §2: Numeric last-wins (local-preference, metric)
  // ==========================================================================

  @Test
  void testLocalPreferenceSetDiff() {
    PsThens ps = new PsThens();
    PsThenLocalPreference lp200 =
        new PsThenLocalPreference(200, PsThenLocalPreference.Operator.SET);
    PsThenLocalPreference lp50 = new PsThenLocalPreference(50, PsThenLocalPreference.Operator.SET);
    assertThat(ps.addPsThen(lp200), empty());
    assertThat(ps.addPsThen(lp50), contains("local-preference"));
    assertEquals(ImmutableList.of(lp50), ps.getAllThens());
  }

  @Test
  void testLocalPreferenceSetSame() {
    PsThens ps = new PsThens();
    PsThenLocalPreference lp = new PsThenLocalPreference(200, PsThenLocalPreference.Operator.SET);
    assertThat(ps.addPsThen(lp), empty());
    assertThat(ps.addPsThen(lp), contains("local-preference (dedup)"));
    assertEquals(ImmutableList.of(lp), ps.getAllThens());
  }

  @Test
  void testLocalPreferenceSetThenAdd() {
    PsThens ps = new PsThens();
    PsThenLocalPreference lpSet =
        new PsThenLocalPreference(200, PsThenLocalPreference.Operator.SET);
    PsThenLocalPreference lpAdd = new PsThenLocalPreference(50, PsThenLocalPreference.Operator.ADD);
    assertThat(ps.addPsThen(lpSet), empty());
    assertThat(ps.addPsThen(lpAdd), contains("local-preference"));
    assertEquals(ImmutableList.of(lpAdd), ps.getAllThens());
  }

  @Test
  void testLocalPreferenceAddThenSet() {
    PsThens ps = new PsThens();
    PsThenLocalPreference lpAdd = new PsThenLocalPreference(50, PsThenLocalPreference.Operator.ADD);
    PsThenLocalPreference lpSet =
        new PsThenLocalPreference(200, PsThenLocalPreference.Operator.SET);
    assertThat(ps.addPsThen(lpAdd), empty());
    assertThat(ps.addPsThen(lpSet), contains("local-preference"));
    assertEquals(ImmutableList.of(lpSet), ps.getAllThens());
  }

  @Test
  void testLocalPreferenceSetThenSubtract() {
    PsThens ps = new PsThens();
    PsThenLocalPreference lpSet =
        new PsThenLocalPreference(200, PsThenLocalPreference.Operator.SET);
    PsThenLocalPreference lpSub =
        new PsThenLocalPreference(50, PsThenLocalPreference.Operator.SUBTRACT);
    assertThat(ps.addPsThen(lpSet), empty());
    assertThat(ps.addPsThen(lpSub), contains("local-preference"));
    assertEquals(ImmutableList.of(lpSub), ps.getAllThens());
  }

  @Test
  void testLocalPreferenceAddDiff() {
    PsThens ps = new PsThens();
    PsThenLocalPreference lpAdd200 =
        new PsThenLocalPreference(200, PsThenLocalPreference.Operator.ADD);
    PsThenLocalPreference lpAdd50 =
        new PsThenLocalPreference(50, PsThenLocalPreference.Operator.ADD);
    assertThat(ps.addPsThen(lpAdd200), empty());
    assertThat(ps.addPsThen(lpAdd50), contains("local-preference"));
    assertEquals(ImmutableList.of(lpAdd50), ps.getAllThens());
  }

  @Test
  void testLocalPreferenceAddThenSubtract() {
    PsThens ps = new PsThens();
    PsThenLocalPreference lpAdd =
        new PsThenLocalPreference(200, PsThenLocalPreference.Operator.ADD);
    PsThenLocalPreference lpSub =
        new PsThenLocalPreference(50, PsThenLocalPreference.Operator.SUBTRACT);
    assertThat(ps.addPsThen(lpAdd), empty());
    assertThat(ps.addPsThen(lpSub), contains("local-preference"));
    assertEquals(ImmutableList.of(lpSub), ps.getAllThens());
  }

  @Test
  void testMetricSetDiff() {
    PsThens ps = new PsThens();
    PsThenMetric m200 = new PsThenMetric(200, PsThenMetric.Operator.SET);
    PsThenMetric m50 = new PsThenMetric(50, PsThenMetric.Operator.SET);
    assertThat(ps.addPsThen(m200), empty());
    assertThat(ps.addPsThen(m50), contains("metric"));
    assertEquals(ImmutableList.of(m50), ps.getAllThens());
  }

  @Test
  void testMetricSetSame() {
    PsThens ps = new PsThens();
    PsThenMetric m = new PsThenMetric(200, PsThenMetric.Operator.SET);
    assertThat(ps.addPsThen(m), empty());
    assertThat(ps.addPsThen(m), contains("metric (dedup)"));
    assertEquals(ImmutableList.of(m), ps.getAllThens());
  }

  @Test
  void testMetricSetThenAdd() {
    PsThens ps = new PsThens();
    PsThenMetric mSet = new PsThenMetric(200, PsThenMetric.Operator.SET);
    PsThenMetric mAdd = new PsThenMetric(50, PsThenMetric.Operator.ADD);
    assertThat(ps.addPsThen(mSet), empty());
    assertThat(ps.addPsThen(mAdd), contains("metric"));
    assertEquals(ImmutableList.of(mAdd), ps.getAllThens());
  }

  @Test
  void testMetricAddThenSet() {
    PsThens ps = new PsThens();
    PsThenMetric mAdd = new PsThenMetric(50, PsThenMetric.Operator.ADD);
    PsThenMetric mSet = new PsThenMetric(200, PsThenMetric.Operator.SET);
    assertThat(ps.addPsThen(mAdd), empty());
    assertThat(ps.addPsThen(mSet), contains("metric"));
    assertEquals(ImmutableList.of(mSet), ps.getAllThens());
  }

  @Test
  void testMetricAddDiff() {
    PsThens ps = new PsThens();
    PsThenMetric mAdd200 = new PsThenMetric(200, PsThenMetric.Operator.ADD);
    PsThenMetric mAdd50 = new PsThenMetric(50, PsThenMetric.Operator.ADD);
    assertThat(ps.addPsThen(mAdd200), empty());
    assertThat(ps.addPsThen(mAdd50), contains("metric"));
    assertEquals(ImmutableList.of(mAdd50), ps.getAllThens());
  }

  @Test
  void testMetricAddThenSubtract() {
    PsThens ps = new PsThens();
    PsThenMetric mAdd = new PsThenMetric(200, PsThenMetric.Operator.ADD);
    PsThenMetric mSub = new PsThenMetric(50, PsThenMetric.Operator.SUBTRACT);
    assertThat(ps.addPsThen(mAdd), empty());
    assertThat(ps.addPsThen(mSub), contains("metric"));
    assertEquals(ImmutableList.of(mSub), ps.getAllThens());
  }

  // ==========================================================================
  // §3: AS-Path
  // ==========================================================================

  @Test
  void testPrependLastWins() {
    PsThens ps = new PsThens();
    PsThenAsPathPrepend p1 = new PsThenAsPathPrepend(ImmutableList.of(65010L));
    PsThenAsPathPrepend p2 = new PsThenAsPathPrepend(ImmutableList.of(65020L));
    assertThat(ps.addPsThen(p1), empty());
    assertThat(ps.addPsThen(p2), contains("as-path-prepend"));
    assertEquals(ImmutableList.of(p2), ps.getAllThens());
  }

  @Test
  void testPrependDedup() {
    PsThens ps = new PsThens();
    PsThenAsPathPrepend p = new PsThenAsPathPrepend(ImmutableList.of(65010L));
    assertThat(ps.addPsThen(p), empty());
    assertThat(
        ps.addPsThen(new PsThenAsPathPrepend(ImmutableList.of(65010L))),
        contains("as-path-prepend (dedup)"));
  }

  @Test
  void testPrependMultiVsSingle() {
    PsThens ps = new PsThens();
    PsThenAsPathPrepend multi = new PsThenAsPathPrepend(ImmutableList.of(65010L, 65020L));
    PsThenAsPathPrepend single = new PsThenAsPathPrepend(ImmutableList.of(65030L));
    assertThat(ps.addPsThen(multi), empty());
    assertThat(ps.addPsThen(single), contains("as-path-prepend"));
    assertEquals(ImmutableList.of(single), ps.getAllThens());
  }

  @Test
  void testPrependAndExpandBothRetained() {
    PsThens ps = new PsThens();
    PsThenAsPathPrepend prepend = new PsThenAsPathPrepend(ImmutableList.of(65010L));
    PsThenAsPathExpandAsList expand = new PsThenAsPathExpandAsList(ImmutableList.of(65020L));
    assertThat(ps.addPsThen(prepend), empty());
    assertThat(ps.addPsThen(expand), empty());
    assertEquals(ImmutableList.of(prepend, expand), ps.getAllThens());
  }

  @Test
  void testExpandAndPrependBothRetained() {
    PsThens ps = new PsThens();
    PsThenAsPathExpandAsList expand = new PsThenAsPathExpandAsList(ImmutableList.of(65020L));
    PsThenAsPathPrepend prepend = new PsThenAsPathPrepend(ImmutableList.of(65010L));
    assertThat(ps.addPsThen(expand), empty());
    assertThat(ps.addPsThen(prepend), empty());
    // Canonical order: prepend before expand (Junos applies prepend first)
    assertEquals(ImmutableList.of(prepend, expand), ps.getAllThens());
  }

  @Test
  void testExpandLastWins() {
    PsThens ps = new PsThens();
    PsThenAsPathExpandAsList e1 = new PsThenAsPathExpandAsList(ImmutableList.of(65010L));
    PsThenAsPathExpandAsList e2 = new PsThenAsPathExpandAsList(ImmutableList.of(65020L));
    assertThat(ps.addPsThen(e1), empty());
    assertThat(ps.addPsThen(e2), contains("as-path-expand"));
    assertEquals(ImmutableList.of(e2), ps.getAllThens());
  }

  @Test
  void testExpandLastAsLastWins() {
    PsThens ps = new PsThens();
    PsThenAsPathExpandLastAs e2 = new PsThenAsPathExpandLastAs(2);
    PsThenAsPathExpandLastAs e3 = new PsThenAsPathExpandLastAs(3);
    assertThat(ps.addPsThen(e2), empty());
    assertThat(ps.addPsThen(e3), contains("as-path-expand"));
    assertEquals(ImmutableList.of(e3), ps.getAllThens());
  }

  @Test
  void testExpandLastAsPlusPrependBothRetained() {
    PsThens ps = new PsThens();
    PsThenAsPathExpandLastAs expand = new PsThenAsPathExpandLastAs(2);
    PsThenAsPathPrepend prepend = new PsThenAsPathPrepend(ImmutableList.of(65010L));
    assertThat(ps.addPsThen(expand), empty());
    assertThat(ps.addPsThen(prepend), empty());
    // Canonical order: prepend before expand
    assertEquals(ImmutableList.of(prepend, expand), ps.getAllThens());
  }

  // ==========================================================================
  // §4: Communities — single ordered list with set-as-barrier and add/delete conflict detection
  // ==========================================================================

  @Test
  void testCommunityAddDifferent() {
    // add RED; add BLUE — both retained, no conflict
    PsThens ps = new PsThens();
    PsThenCommunityAdd red = new PsThenCommunityAdd("RED");
    PsThenCommunityAdd blue = new PsThenCommunityAdd("BLUE");
    assertThat(ps.addPsThen(red), empty());
    assertThat(ps.addPsThen(blue), empty());
    assertThat(ps.getAllThens(), contains(red, blue));
  }

  @Test
  void testCommunityAddDedup() {
    // add RED; add RED — second is exact duplicate of the immediately preceding action
    PsThens ps = new PsThens();
    PsThenCommunityAdd red = new PsThenCommunityAdd("RED");
    assertThat(ps.addPsThen(red), empty());
    assertThat(ps.addPsThen(new PsThenCommunityAdd("RED")), contains("community add RED (dedup)"));
    assertEquals(ImmutableList.of(red), ps.getAllThens());
  }

  @Test
  void testCommunityAddDeleteAddNoDedup() {
    // add RED; delete RED; add RED — the second add is NOT a dedup: the intervening delete
    // changes the runtime effect, so all three must be retained (final route has RED).
    PsThens ps = new PsThens();
    PsThenCommunityAdd addRed1 = new PsThenCommunityAdd("RED");
    PsThenCommunityDelete delRed = new PsThenCommunityDelete("RED");
    PsThenCommunityAdd addRed2 = new PsThenCommunityAdd("RED");
    assertThat(ps.addPsThen(addRed1), empty());
    assertThat(ps.addPsThen(delRed), contains("community add RED (conflict)"));
    assertThat(ps.addPsThen(addRed2), contains("community delete RED (conflict)"));
    assertThat(ps.getAllThens(), contains(addRed1, delRed, addRed2));
  }

  @Test
  void testCommunityDeleteDifferent() {
    // delete RED; delete BLUE — both retained
    PsThens ps = new PsThens();
    PsThenCommunityDelete delRed = new PsThenCommunityDelete("RED");
    PsThenCommunityDelete delBlue = new PsThenCommunityDelete("BLUE");
    assertThat(ps.addPsThen(delRed), empty());
    assertThat(ps.addPsThen(delBlue), empty());
    assertThat(ps.getAllThens(), contains(delRed, delBlue));
  }

  @Test
  void testCommunityDeleteDedup() {
    PsThens ps = new PsThens();
    PsThenCommunityDelete delRed = new PsThenCommunityDelete("RED");
    assertThat(ps.addPsThen(delRed), empty());
    assertThat(
        ps.addPsThen(new PsThenCommunityDelete("RED")), contains("community delete RED (dedup)"));
    assertEquals(ImmutableList.of(delRed), ps.getAllThens());
  }

  @Test
  void testCommunitySetThenAdd() {
    // set RED; add BLUE — both retained (set replaces, then add unions)
    PsThens ps = new PsThens();
    PsThenCommunitySet setRed = new PsThenCommunitySet("RED");
    PsThenCommunityAdd addBlue = new PsThenCommunityAdd("BLUE");
    assertThat(ps.addPsThen(setRed), empty());
    assertThat(ps.addPsThen(addBlue), empty());
    assertThat(ps.getAllThens(), contains(setRed, addBlue));
  }

  @Test
  void testCommunityAddThenSetWipesAdd() {
    // add RED; set BLUE — set wipes prior add (now a no-op); only set retained, warning
    PsThens ps = new PsThens();
    PsThenCommunityAdd addRed = new PsThenCommunityAdd("RED");
    PsThenCommunitySet setBlue = new PsThenCommunitySet("BLUE");
    assertThat(ps.addPsThen(addRed), empty());
    assertThat(ps.addPsThen(setBlue), contains("community add RED"));
    assertEquals(ImmutableList.of(setBlue), ps.getAllThens());
  }

  @Test
  void testCommunitySetThenSetWipesPriorSet() {
    // set RED; set BLUE — second set wipes first (last-wins); warning
    PsThens ps = new PsThens();
    PsThenCommunitySet setRed = new PsThenCommunitySet("RED");
    PsThenCommunitySet setBlue = new PsThenCommunitySet("BLUE");
    assertThat(ps.addPsThen(setRed), empty());
    assertThat(ps.addPsThen(setBlue), contains("community set RED"));
    assertEquals(ImmutableList.of(setBlue), ps.getAllThens());
  }

  @Test
  void testCommunitySetThenSetDedup() {
    // set RED; set RED — exact duplicate
    PsThens ps = new PsThens();
    PsThenCommunitySet setRed = new PsThenCommunitySet("RED");
    assertThat(ps.addPsThen(setRed), empty());
    assertThat(ps.addPsThen(new PsThenCommunitySet("RED")), contains("community set RED (dedup)"));
    assertEquals(ImmutableList.of(setRed), ps.getAllThens());
  }

  @Test
  void testCommunityAddThenDeleteSameNameConflicts() {
    // add RED; delete RED — both retained (order matters for runtime), warn about conflict
    PsThens ps = new PsThens();
    PsThenCommunityAdd addRed = new PsThenCommunityAdd("RED");
    PsThenCommunityDelete delRed = new PsThenCommunityDelete("RED");
    assertThat(ps.addPsThen(addRed), empty());
    assertThat(ps.addPsThen(delRed), contains("community add RED (conflict)"));
    assertThat(ps.getAllThens(), contains(addRed, delRed));
  }

  @Test
  void testCommunityDeleteThenAddSameNameConflicts() {
    // delete RED; add RED — both retained (order matters), warn about conflict
    PsThens ps = new PsThens();
    PsThenCommunityDelete delRed = new PsThenCommunityDelete("RED");
    PsThenCommunityAdd addRed = new PsThenCommunityAdd("RED");
    assertThat(ps.addPsThen(delRed), empty());
    assertThat(ps.addPsThen(addRed), contains("community delete RED (conflict)"));
    assertThat(ps.getAllThens(), contains(delRed, addRed));
  }

  @Test
  void testCommunityAddThenDeleteDifferentNamesNoConflict() {
    // add RED; delete BLUE — different names, both retained
    PsThens ps = new PsThens();
    PsThenCommunityAdd addRed = new PsThenCommunityAdd("RED");
    PsThenCommunityDelete delBlue = new PsThenCommunityDelete("BLUE");
    assertThat(ps.addPsThen(addRed), empty());
    assertThat(ps.addPsThen(delBlue), empty());
    assertThat(ps.getAllThens(), contains(addRed, delBlue));
  }

  @Test
  void testCommunitySetThenDeleteBothRetained() {
    // set RED; delete RED — set replaces, then delete removes RED → both retained
    PsThens ps = new PsThens();
    PsThenCommunitySet setRed = new PsThenCommunitySet("RED");
    PsThenCommunityDelete delRed = new PsThenCommunityDelete("RED");
    assertThat(ps.addPsThen(setRed), empty());
    assertThat(ps.addPsThen(delRed), empty());
    assertThat(ps.getAllThens(), contains(setRed, delRed));
  }

  @Test
  void testCommunityDeclaredOrderPreserved() {
    // delete RED; add BLUE; delete GREEN — all retained, in declared order
    PsThens ps = new PsThens();
    PsThenCommunityDelete delRed = new PsThenCommunityDelete("RED");
    PsThenCommunityAdd addBlue = new PsThenCommunityAdd("BLUE");
    PsThenCommunityDelete delGreen = new PsThenCommunityDelete("GREEN");
    assertThat(ps.addPsThen(delRed), empty());
    assertThat(ps.addPsThen(addBlue), empty());
    assertThat(ps.addPsThen(delGreen), empty());
    assertThat(ps.getAllThens(), contains(delRed, addBlue, delGreen));
  }

  @Test
  void testCommunitySetWipesAllPrior() {
    // add RED; delete BLUE; set GREEN — set wipes both prior actions; warnings for each
    PsThens ps = new PsThens();
    PsThenCommunityAdd addRed = new PsThenCommunityAdd("RED");
    PsThenCommunityDelete delBlue = new PsThenCommunityDelete("BLUE");
    PsThenCommunitySet setGreen = new PsThenCommunitySet("GREEN");
    assertThat(ps.addPsThen(addRed), empty());
    assertThat(ps.addPsThen(delBlue), empty());
    assertThat(ps.addPsThen(setGreen), contains("community add RED", "community delete BLUE"));
    assertEquals(ImmutableList.of(setGreen), ps.getAllThens());
  }

  // ==========================================================================
  // §6: Forwarding
  // ==========================================================================

  @Test
  void testLoadBalanceDedup() {
    PsThens ps = new PsThens();
    PsThenLoadBalance lb = new PsThenLoadBalance(PsThenLoadBalance.LoadBalanceMethod.PER_PACKET);
    assertThat(ps.addPsThen(lb), empty());
    assertThat(ps.addPsThen(lb), contains("load-balance (dedup)"));
    assertEquals(ImmutableList.of(lb), ps.getAllThens());
  }

  // ==========================================================================
  // §7: Cross-attribute — different families coexist
  // ==========================================================================

  @Test
  void testCrossAttributeAllRetained() {
    PsThens ps = new PsThens();
    PsThenLocalPreference lp = new PsThenLocalPreference(200, PsThenLocalPreference.Operator.SET);
    PsThenMetric met = new PsThenMetric(100, PsThenMetric.Operator.SET);
    PsThenCommunityAdd add = new PsThenCommunityAdd("RED");
    assertThat(ps.addPsThen(lp), empty());
    assertThat(ps.addPsThen(met), empty());
    assertThat(ps.addPsThen(add), empty());
    assertThat(ps.getAllThens(), containsInAnyOrder(lp, met, add));
  }

  // ==========================================================================
  // Tunnel-attribute set — last-wins (only one tunnel attribute applies on a BGP route)
  // ==========================================================================

  @Test
  void testTunnelAttributeSetDedup() {
    PsThens ps = new PsThens();
    PsThenTunnelAttributeSet ta1 = new PsThenTunnelAttributeSet("ta1");
    assertThat(ps.addPsThen(ta1), empty());
    assertThat(
        ps.addPsThen(new PsThenTunnelAttributeSet("ta1")),
        contains("tunnel-attribute set (dedup)"));
    assertEquals(ImmutableList.of(ta1), ps.getAllThens());
  }

  @Test
  void testTunnelAttributeSetLastWins() {
    PsThens ps = new PsThens();
    PsThenTunnelAttributeSet ta1 = new PsThenTunnelAttributeSet("ta1");
    PsThenTunnelAttributeSet ta2 = new PsThenTunnelAttributeSet("ta2");
    assertThat(ps.addPsThen(ta1), empty());
    assertThat(ps.addPsThen(ta2), contains("tunnel-attribute set"));
    assertEquals(ImmutableList.of(ta2), ps.getAllThens());
  }

  // ==========================================================================
  // §5: Bare terminators collapse last-wins; named non-bare flow control retained
  // ==========================================================================

  @Test
  void testAcceptThenRejectLastWins() {
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenAccept.INSTANCE), empty());
    assertThat(ps.addPsThen(PsThenReject.INSTANCE), contains("accept-or-reject"));
    assertEquals(ImmutableList.of(PsThenReject.INSTANCE), ps.getAllThens());
  }

  @Test
  void testRejectThenAcceptLastWins() {
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenReject.INSTANCE), empty());
    assertThat(ps.addPsThen(PsThenAccept.INSTANCE), contains("accept-or-reject"));
    assertEquals(ImmutableList.of(PsThenAccept.INSTANCE), ps.getAllThens());
  }

  @Test
  void testAcceptDedup() {
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenAccept.INSTANCE), empty());
    assertThat(ps.addPsThen(PsThenAccept.INSTANCE), contains("accept-or-reject (dedup)"));
    assertEquals(ImmutableList.of(PsThenAccept.INSTANCE), ps.getAllThens());
  }

  @Test
  void testDefaultActionAndBareTerminatorBothRetained() {
    // default-action accept and bare reject are different families. Both retained at commit.
    // Bare terminator overrides default-action: dead-with-bare-terminator warning fires.
    PsThens ps = new PsThens();
    PsThenDefaultActionAccept defaultAccept = new PsThenDefaultActionAccept();
    assertThat(ps.addPsThen(defaultAccept), empty());
    assertThat(
        ps.addPsThen(PsThenReject.INSTANCE),
        contains("default-action (dead-with-bare-terminator)"));
    assertThat(ps.getAllThens(), containsInAnyOrder(defaultAccept, PsThenReject.INSTANCE));
  }

  @Test
  void testAcceptThenNextTermSuppresses() {
    // accept; next term — both retained at commit; next-term wins at runtime
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenAccept.INSTANCE), empty());
    assertThat(
        ps.addPsThen(PsThenNextTerm.INSTANCE),
        contains("accept-or-reject (suppressed-by-next-term-or-policy)"));
    assertThat(
        ps.getAllThens(), containsInAnyOrder(PsThenAccept.INSTANCE, PsThenNextTerm.INSTANCE));
  }

  @Test
  void testAcceptThenNextPolicySuppresses() {
    // accept; next policy — same as next-term: accept does not fire at runtime
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenAccept.INSTANCE), empty());
    assertThat(
        ps.addPsThen(PsThenNextPolicy.INSTANCE),
        contains("accept-or-reject (suppressed-by-next-term-or-policy)"));
    assertThat(
        ps.getAllThens(), containsInAnyOrder(PsThenAccept.INSTANCE, PsThenNextPolicy.INSTANCE));
  }

  @Test
  void testNextTermThenAcceptIsDead() {
    // next term; accept — bare accept is dead config when next-term already present
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenNextTerm.INSTANCE), empty());
    assertThat(
        ps.addPsThen(PsThenAccept.INSTANCE),
        contains("accept-or-reject (dead-after-next-term-or-policy)"));
    assertThat(
        ps.getAllThens(), containsInAnyOrder(PsThenAccept.INSTANCE, PsThenNextTerm.INSTANCE));
  }

  @Test
  void testNextPolicyThenRejectIsDead() {
    // next policy; reject — bare reject is dead config when next-policy already present
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenNextPolicy.INSTANCE), empty());
    assertThat(
        ps.addPsThen(PsThenReject.INSTANCE),
        contains("accept-or-reject (dead-after-next-term-or-policy)"));
    assertThat(
        ps.getAllThens(), containsInAnyOrder(PsThenReject.INSTANCE, PsThenNextPolicy.INSTANCE));
  }

  @Test
  void testNextTermDedup() {
    // §5 row 4011: next term; next term — dedup (same family)
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenNextTerm.INSTANCE), empty());
    assertThat(ps.addPsThen(PsThenNextTerm.INSTANCE), contains("next-term-or-policy (dedup)"));
    assertEquals(ImmutableList.of(PsThenNextTerm.INSTANCE), ps.getAllThens());
  }

  @Test
  void testNextPolicyDedup() {
    // §5 row 4012: next policy; next policy — dedup (same family)
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenNextPolicy.INSTANCE), empty());
    assertThat(ps.addPsThen(PsThenNextPolicy.INSTANCE), contains("next-term-or-policy (dedup)"));
    assertEquals(ImmutableList.of(PsThenNextPolicy.INSTANCE), ps.getAllThens());
  }

  @Test
  void testNextTermThenNextPolicyLastWins() {
    // §5 row 4013: next term; next policy — same family, last-wins → next-policy retained
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenNextTerm.INSTANCE), empty());
    assertThat(ps.addPsThen(PsThenNextPolicy.INSTANCE), contains("next-term-or-policy"));
    assertEquals(ImmutableList.of(PsThenNextPolicy.INSTANCE), ps.getAllThens());
  }

  @Test
  void testNextPolicyThenNextTermLastWins() {
    // §5 row 4014: next policy; next term — last-wins → next-term retained
    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(PsThenNextPolicy.INSTANCE), empty());
    assertThat(ps.addPsThen(PsThenNextTerm.INSTANCE), contains("next-term-or-policy"));
    assertEquals(ImmutableList.of(PsThenNextTerm.INSTANCE), ps.getAllThens());
  }

  // ==========================================================================
  // default-action: separate last-wins family; dead when bare terminator is also present
  // ==========================================================================

  @Test
  void testDefaultActionAcceptDedup() {
    PsThens ps = new PsThens();
    PsThenDefaultActionAccept da1 = new PsThenDefaultActionAccept();
    PsThenDefaultActionAccept da2 = new PsThenDefaultActionAccept();
    assertThat(ps.addPsThen(da1), empty());
    assertThat(ps.addPsThen(da2), contains("default-action (dedup)"));
    assertThat(ps.getAllThens(), contains(da2));
  }

  @Test
  void testDefaultActionAcceptThenRejectLastWins() {
    PsThens ps = new PsThens();
    PsThenDefaultActionAccept daAccept = new PsThenDefaultActionAccept();
    PsThenDefaultActionReject daReject = new PsThenDefaultActionReject();
    assertThat(ps.addPsThen(daAccept), empty());
    assertThat(ps.addPsThen(daReject), contains("default-action"));
    assertThat(ps.getAllThens(), contains(daReject));
  }

  @Test
  void testBareAcceptThenDefaultActionIsDead() {
    // accept; default-action reject — default-action is dead config when added after a bare
    // terminator. Both lines retained at commit.
    PsThens ps = new PsThens();
    PsThenDefaultActionReject defaultReject = new PsThenDefaultActionReject();
    assertThat(ps.addPsThen(PsThenAccept.INSTANCE), empty());
    assertThat(ps.addPsThen(defaultReject), contains("default-action (dead-with-bare-terminator)"));
    assertThat(ps.getAllThens(), containsInAnyOrder(PsThenAccept.INSTANCE, defaultReject));
  }

  @Test
  void testDefaultActionRejectThenBareAcceptIsDeadConfig() {
    // default-action reject; accept — adding the bare terminator makes the default-action dead
    // config (the default-action was authored first but never fires).
    PsThens ps = new PsThens();
    PsThenDefaultActionReject defaultReject = new PsThenDefaultActionReject();
    assertThat(ps.addPsThen(defaultReject), empty());
    assertThat(
        ps.addPsThen(PsThenAccept.INSTANCE),
        contains("default-action (dead-with-bare-terminator)"));
    assertThat(ps.getAllThens(), containsInAnyOrder(PsThenAccept.INSTANCE, defaultReject));
  }

  // ==========================================================================
  // add-path send-count — scalar last-wins family
  // ==========================================================================

  @Test
  void testAddPathSendCountDedup() {
    PsThens ps = new PsThens();
    PsThenAddPathSendCount c16 = new PsThenAddPathSendCount(16);
    assertThat(ps.addPsThen(c16), empty());
    assertThat(
        ps.addPsThen(new PsThenAddPathSendCount(16)), contains("add-path send-count (dedup)"));
    assertEquals(ImmutableList.of(c16), ps.getAllThens());
  }

  @Test
  void testAddPathSendCountLastWins() {
    PsThens ps = new PsThens();
    PsThenAddPathSendCount c16 = new PsThenAddPathSendCount(16);
    PsThenAddPathSendCount c32 = new PsThenAddPathSendCount(32);
    assertThat(ps.addPsThen(c16), empty());
    assertThat(ps.addPsThen(c32), contains("add-path send-count"));
    assertEquals(ImmutableList.of(c32), ps.getAllThens());
  }
}
