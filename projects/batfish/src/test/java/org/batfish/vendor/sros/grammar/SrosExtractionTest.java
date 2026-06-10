package org.batfish.vendor.sros.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.config.Settings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.vendor.sros.representation.BgpGroup;
import org.batfish.vendor.sros.representation.BgpNeighbor;
import org.batfish.vendor.sros.representation.BgpProcess;
import org.batfish.vendor.sros.representation.Card;
import org.batfish.vendor.sros.representation.Community;
import org.batfish.vendor.sros.representation.PeerType;
import org.batfish.vendor.sros.representation.PolicyAction;
import org.batfish.vendor.sros.representation.PolicyStatement;
import org.batfish.vendor.sros.representation.PolicyStatementEntry;
import org.batfish.vendor.sros.representation.PrefixList;
import org.batfish.vendor.sros.representation.PrefixListEntry;
import org.batfish.vendor.sros.representation.Router;
import org.batfish.vendor.sros.representation.RouterInterface;
import org.batfish.vendor.sros.representation.SrosConfiguration;
import org.batfish.vendor.sros.representation.StaticRoute;
import org.junit.Test;

/** Tests of SR-OS feature extraction (P4): the canonical tree, the preprocessor, and the model. */
public final class SrosExtractionTest {

  /** The captured r1 config extracts the full characterized feature set with no warnings. */
  @Test
  public void testR1Extraction() {
    SrosConfiguration vc = parseVendorConfig("r1_admin_show_configuration.txt");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    assertThat(vc.getWarnings().getUnimplementedWarnings(), empty());

    // Hardware: card 1 (iom-1) with mda 1 (me6-100gb-qsfp28).
    assertThat(vc.getCards(), hasKey(1));
    Card card = vc.getCards().get(1);
    assertThat(card.getCardType(), equalTo("iom-1"));
    assertThat(card.getMdas(), hasKey(1));
    assertThat(card.getMdas().get(1).getMdaType(), equalTo("me6-100gb-qsfp28"));

    // Ports: the connector with breakout, and the breakout sub-port. Both admin-state enable.
    assertThat(vc.getPorts(), hasKey("1/1/c1"));
    assertThat(vc.getPorts(), hasKey("1/1/c1/1"));
    assertThat(vc.getPorts().get("1/1/c1").getAdminStateEnable(), equalTo(Boolean.TRUE));
    assertThat(vc.getPorts().get("1/1/c1").getBreakout(), equalTo("c1-100g"));
    assertThat(vc.getPorts().get("1/1/c1/1").getAdminStateEnable(), equalTo(Boolean.TRUE));
    assertThat(vc.getPorts().get("1/1/c1/1").getBreakout(), nullValue());

    // Router Base: AS 65001, two interfaces.
    assertThat(vc.getRouters(), hasKey("Base"));
    Router base = vc.getRouters().get("Base");
    assertThat(base.getAutonomousSystem(), equalTo(65001L));
    assertThat(base.getInterfaces().keySet(), containsInAnyOrder("system", "to-r2"));

    RouterInterface system = base.getInterfaces().get("system");
    assertThat(system.getPort(), nullValue());
    assertThat(system.getPrimaryAddress(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(system.getPrimaryPrefixLength(), equalTo(32));
    assertThat(system.getPrimaryConcreteAddress().toString(), equalTo("1.1.1.1/32"));

    RouterInterface toR2 = base.getInterfaces().get("to-r2");
    assertThat(toR2.getPort(), equalTo("1/1/c1/1"));
    assertThat(toR2.getPrimaryAddress(), equalTo(Ip.parse("10.0.0.0")));
    assertThat(toR2.getPrimaryPrefixLength(), equalTo(31));

    // BGP: router-id, one group, one neighbor referencing it.
    BgpProcess bgp = base.getBgpProcess();
    assertThat(bgp, not(nullValue()));
    assertThat(bgp.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(bgp.getGroups(), hasKey("ebgp"));
    BgpGroup ebgp = bgp.getGroups().get("ebgp");
    assertThat(ebgp.getPeerAs(), equalTo(65002L));
    assertThat(ebgp.getImportPolicies(), contains("import-all"));
    assertThat(ebgp.getExportPolicies(), contains("export-system"));
    assertThat(bgp.getNeighbors(), hasKey("10.0.0.1"));
    BgpNeighbor neighbor = bgp.getNeighbors().get("10.0.0.1");
    assertThat(neighbor.getGroup(), equalTo("ebgp"));

    // policy-options: one prefix-list with one exact entry; two policy-statements.
    assertThat(vc.getPrefixLists(), hasKey("system-pfx"));
    PrefixList pfx = vc.getPrefixLists().get("system-pfx");
    assertThat(
        pfx.getEntries(),
        contains(new PrefixListEntry(Prefix.parse("1.1.1.1/32"), PrefixListEntry.Type.EXACT)));

    assertThat(
        vc.getPolicyStatements().keySet(), containsInAnyOrder("export-system", "import-all"));
    PolicyStatement exportSystem = vc.getPolicyStatements().get("export-system");
    assertThat(exportSystem.getEntries(), hasKey(10L));
    assertThat(exportSystem.getEntries().get(10L).getFromPrefixLists(), contains("system-pfx"));
    assertThat(exportSystem.getEntries().get(10L).getAction(), equalTo(PolicyAction.ACCEPT));
    PolicyStatement importAll = vc.getPolicyStatements().get("import-all");
    assertThat(importAll.getEntries().keySet(), empty());
    assertThat(importAll.getDefaultAction(), equalTo(PolicyAction.ACCEPT));
  }

  /**
   * r1's security/ssh/user-params/persistent-indices subtrees are silently skipped (no warnings);
   * only {@code system name} is read from the system subtree.
   */
  @Test
  public void testR1UnmodeledSubtreesSilentlySkipped() {
    SrosConfiguration vc = parseVendorConfig("r1_admin_show_configuration.txt");
    assertThat(vc.getHostname(), equalTo("r1"));
    assertThat(vc.getWarnings().getUnimplementedWarnings(), empty());
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
  }

  /** A `system name` leaf becomes the Batfish hostname. */
  @Test
  public void testHostnameExtraction() {
    SrosConfiguration vc = parseVendorConfig("hostname.txt");
    assertThat(vc.getHostname(), equalTo("sros-r1"));
  }

  /**
   * static-routes extraction: next-hop and blackhole routes, the admin-state-enable flag, and the
   * preference/metric leaves (with their YANG defaults when absent).
   */
  @Test
  public void testStaticRoutesExtraction() {
    SrosConfiguration vc = parseVendorConfig("static_routes.txt");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    List<StaticRoute> routes = vc.getRouters().get("Base").getStaticRoutes();
    assertThat(routes, hasSize(6));

    // route 1: next-hop, admin-state enable, default preference 5 / metric 1.
    StaticRoute nh = routes.get(0);
    assertThat(nh.getPrefix(), equalTo(Prefix.parse("192.0.2.0/24")));
    assertThat(nh.getNextHopIp(), equalTo(Ip.parse("10.0.0.1")));
    assertThat(nh.getBlackhole(), equalTo(false));
    assertThat(nh.getAdminStateEnable(), equalTo(true));
    assertThat(nh.getPreference(), equalTo(StaticRoute.DEFAULT_PREFERENCE));
    assertThat(nh.getMetric(), equalTo(StaticRoute.DEFAULT_METRIC));

    // route 2: blackhole, admin-state enable.
    StaticRoute bh = routes.get(1);
    assertThat(bh.getPrefix(), equalTo(Prefix.parse("198.51.100.0/24")));
    assertThat(bh.getNextHopIp(), nullValue());
    assertThat(bh.getBlackhole(), equalTo(true));
    assertThat(bh.getAdminStateEnable(), equalTo(true));

    // route 3: next-hop with explicit preference 100 / metric 50.
    StaticRoute nhPref = routes.get(2);
    assertThat(nhPref.getPrefix(), equalTo(Prefix.parse("203.0.113.0/24")));
    assertThat(nhPref.getPreference(), equalTo(100));
    assertThat(nhPref.getMetric(), equalTo(50));

    // route 4: next-hop with no admin-state -> not enabled (will not be installed in conversion).
    StaticRoute nhNoAdmin = routes.get(3);
    assertThat(nhNoAdmin.getPrefix(), equalTo(Prefix.parse("100.64.0.0/24")));
    assertThat(nhNoAdmin.getNextHopIp(), equalTo(Ip.parse("10.0.0.1")));
    assertThat(nhNoAdmin.getAdminStateEnable(), equalTo(false));

    // routes 5-6: an ECMP route with two next-hops -> one StaticRoute per next-hop, each carrying
    // its own metric (10 / 20). Confirmed on SR-SIM 26.3.R1: equal-preference next-hops both
    // install (batfish/batfish#9989).
    StaticRoute ecmp1 = routes.get(4);
    StaticRoute ecmp2 = routes.get(5);
    assertThat(ecmp1.getPrefix(), equalTo(Prefix.parse("192.0.2.128/25")));
    assertThat(ecmp2.getPrefix(), equalTo(Prefix.parse("192.0.2.128/25")));
    assertThat(
        ImmutableSet.of(ecmp1.getNextHopIp(), ecmp2.getNextHopIp()),
        equalTo(ImmutableSet.of(Ip.parse("10.0.0.1"), Ip.parse("10.0.1.1"))));
    assertThat(ecmp1.getAdminStateEnable(), equalTo(true));
    assertThat(ecmp2.getAdminStateEnable(), equalTo(true));
    assertThat(
        ImmutableSet.of(ecmp1.getMetric(), ecmp2.getMetric()), equalTo(ImmutableSet.of(10, 20)));
  }

  /** aggregates extraction: the aggregate prefix and its {@code summary-only} flag. */
  @Test
  public void testAggregateExtraction() {
    SrosConfiguration vc = parseVendorConfig("aggregate.txt");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    List<org.batfish.vendor.sros.representation.Aggregate> aggs =
        vc.getRouters().get("Base").getAggregates();
    assertThat(aggs, hasSize(1));
    assertThat(aggs.get(0).getPrefix(), equalTo(Prefix.parse("10.100.0.0/16")));
    assertThat(aggs.get(0).getSummaryOnly(), equalTo(true));
  }

  /**
   * Comprehensive policy extraction: {@code from community name}, {@code from as-path name}, and
   * the {@code action} clauses local-preference / metric add / origin; plus the as-path list
   * expression.
   */
  @Test
  public void testComprehensivePolicyExtraction() {
    SrosConfiguration vc = parseVendorConfig("policy_comprehensive.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    assertThat(vc.getAsPathLists(), hasKey("via-65003"));
    assertThat(vc.getAsPathLists().get("via-65003").getExpression(), equalTo("65003"));

    PolicyStatement ps = vc.getPolicyStatements().get("import-rich");
    PolicyStatementEntry e10 = ps.getEntries().get(10L);
    assertThat(e10.getFromCommunities(), contains("from-r2-100"));
    assertThat(e10.getSetLocalPreference(), equalTo(250L));
    assertThat(e10.getCommunityAdds(), contains("tag-local"));

    PolicyStatementEntry e20 = ps.getEntries().get(20L);
    assertThat(e20.getFromAsPaths(), contains("via-65003"));
    assertThat(e20.getMetricAdd(), equalTo(33L));
    assertThat(e20.getSetOrigin(), equalTo(org.batfish.datamodel.OriginType.IGP));

    assertThat(ps.getDefaultAction(), equalTo(PolicyAction.ACCEPT));
  }

  /** LAG extraction: the lag name, admin-state, and its member ports. */
  @Test
  public void testLagExtraction() {
    SrosConfiguration vc = parseVendorConfig("lag.txt");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    assertThat(vc.getLags(), hasKey("lag-1"));
    org.batfish.vendor.sros.representation.Lag lag = vc.getLags().get("lag-1");
    assertThat(lag.getAdminStateEnable(), equalTo(true));
    assertThat(lag.getMemberPorts(), containsInAnyOrder("1/1/c1/1", "1/1/c2/1"));
    // The router interface binds the LAG by name.
    assertThat(
        vc.getRouters().get("Base").getInterfaces().get("to-peer").getPort(), equalTo("lag-1"));
  }

  /**
   * Policy set-clause + community-list + prefix-list-bound extraction: {@code action metric set},
   * {@code as-path-prepend as-path/repeat}, {@code community add}, a {@code community} list, and
   * the {@code through-length}/{@code start-length}/{@code end-length} prefix-list bounds.
   */
  @Test
  public void testPolicySetClausesExtraction() {
    SrosConfiguration vc = parseVendorConfig("policy_set_clauses.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());

    // community list "comm-tag" with one member.
    assertThat(vc.getCommunities(), hasKey("comm-tag"));
    Community comm = vc.getCommunities().get("comm-tag");
    assertThat(comm.getMembers(), contains("65001:100"));

    // prefix-list bounds: through-length on lo-range, start/end-length on host-range.
    PrefixListEntry through = vc.getPrefixLists().get("lo-range").getEntries().get(0);
    assertThat(through.getType(), equalTo(PrefixListEntry.Type.THROUGH));
    assertThat(through.getThroughLength(), equalTo(32));
    PrefixListEntry range = vc.getPrefixLists().get("host-range").getEntries().get(0);
    assertThat(range.getType(), equalTo(PrefixListEntry.Type.RANGE));
    assertThat(range.getStartLength(), equalTo(24));
    assertThat(range.getEndLength(), equalTo(32));

    // `to` entry captures its to-prefix list; `address-mask` entry captures its mask-pattern.
    PrefixListEntry to = vc.getPrefixLists().get("to-list").getEntries().get(0);
    assertThat(to.getType(), equalTo(PrefixListEntry.Type.TO));
    assertThat(
        to.getToPrefixes(),
        containsInAnyOrder(Prefix.parse("10.20.0.0/20"), Prefix.parse("10.20.16.0/24")));
    PrefixListEntry mask = vc.getPrefixLists().get("mask-list").getEntries().get(0);
    assertThat(mask.getType(), equalTo(PrefixListEntry.Type.ADDRESS_MASK));
    assertThat(mask.getMaskPatterns(), contains(Ip.parse("255.255.0.0")));

    // entry 10 set-clauses: metric 50, as-path-prepend 65001 x2, community add comm-tag.
    PolicyStatement ps = vc.getPolicyStatements().get("export-to-r2");
    PolicyStatementEntry e10 = ps.getEntries().get(10L);
    assertThat(e10.getSetMetric(), equalTo(50L));
    assertThat(e10.getAsPathPrependAsn(), equalTo(65001L));
    assertThat(e10.getAsPathPrependRepeat(), equalTo(2));
    assertThat(e10.getCommunityAdds(), contains("comm-tag"));

    // entry 20 set-clause: metric 200, no prepend (default repeat), no community.
    PolicyStatementEntry e20 = ps.getEntries().get(20L);
    assertThat(e20.getSetMetric(), equalTo(200L));
    assertThat(e20.getAsPathPrependAsn(), nullValue());
    assertThat(e20.getCommunityAdds(), empty());
  }

  /**
   * OSPF extraction: instance, router-id, admin-state, areas, and per-area interface type/metric.
   */
  @Test
  public void testOspfExtraction() {
    SrosConfiguration vc = parseVendorConfig("ospf.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    org.batfish.vendor.sros.representation.OspfProcess proc =
        vc.getRouters().get("Base").getOspfProcess();
    assertThat(proc, notNullValue());
    assertThat(proc.getInstance(), equalTo(0));
    assertThat(proc.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(proc.getAdminStateEnable(), equalTo(true));
    org.batfish.vendor.sros.representation.OspfArea area = proc.getAreas().get("0.0.0.0");
    assertThat(area, notNullValue());
    assertThat(area.getInterfaces().keySet(), containsInAnyOrder("system", "to-r3"));
    org.batfish.vendor.sros.representation.OspfAreaInterface toR3 =
        area.getInterfaces().get("to-r3");
    assertThat(
        toR3.getInterfaceType(),
        equalTo(
            org.batfish.vendor.sros.representation.OspfAreaInterface.InterfaceType.POINT_TO_POINT));
    assertThat(toR3.getMetric(), equalTo(100));
    assertThat(area.getInterfaces().get("system").getMetric(), nullValue());
  }

  /**
   * IS-IS extraction: instance, admin-state, system-id, area-address, level-capability, and the
   * per-interface interface-type/passive.
   */
  @Test
  public void testIsisExtraction() {
    SrosConfiguration vc = parseVendorConfig("isis.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    org.batfish.vendor.sros.representation.IsisProcess proc =
        vc.getRouters().get("Base").getIsisProcess();
    assertThat(proc, notNullValue());
    assertThat(proc.getInstance(), equalTo(0));
    assertThat(proc.getAdminStateEnable(), equalTo(true));
    assertThat(proc.getSystemId(), equalTo("0100.1000.0001"));
    assertThat(proc.getAreaAddresses(), contains("49.0001"));
    assertThat(
        proc.getLevelCapability(),
        equalTo(org.batfish.vendor.sros.representation.IsisProcess.LevelCapability.LEVEL_2));
    assertThat(proc.getInterfaces().keySet(), containsInAnyOrder("system", "to-r3"));
    // system is passive (advertised, no adjacency); to-r3 is point-to-point and active.
    assertThat(proc.getInterfaces().get("system").getPassive(), equalTo(true));
    org.batfish.vendor.sros.representation.IsisProcessInterface toR3 =
        proc.getInterfaces().get("to-r3");
    assertThat(toR3.getPassive(), equalTo(false));
    assertThat(
        toR3.getInterfaceType(),
        equalTo(
            org.batfish.vendor.sros.representation.IsisProcessInterface.InterfaceType
                .POINT_TO_POINT));
  }

  /**
   * VPRN extraction: a {@code service vprn "<name>"} becomes a {@link Router} with its interfaces.
   */
  @Test
  public void testVprnExtraction() {
    SrosConfiguration vc = parseVendorConfig("vprn.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    assertThat(vc.getRouters().keySet(), containsInAnyOrder("Base", "red"));
    Router red = vc.getRouters().get("red");
    assertThat(red.getInterfaces(), hasKey("red-lo"));
    assertThat(
        red.getInterfaces().get("red-lo").getPrimaryAddress(), equalTo(Ip.parse("172.16.0.1")));
    assertThat(red.getInterfaces().get("red-lo").getPort(), nullValue());

    // bgp-ipvpn mpls: route-distinguisher + vrf-target (single community -> import + export).
    org.batfish.vendor.sros.representation.BgpIpvpn ipvpn = red.getBgpIpvpn();
    assertThat(ipvpn, notNullValue());
    assertThat(ipvpn.getRouteDistinguisher(), equalTo("65000:1"));
    assertThat(ipvpn.getImportRouteTargets(), contains("target:65000:1"));
    assertThat(ipvpn.getExportRouteTargets(), contains("target:65000:1"));
  }

  /** Route-reflector extraction: a group's cluster-id and next-hop-self inherit to its neighbor. */
  @Test
  public void testRouteReflectorExtraction() {
    SrosConfiguration vc = parseVendorConfig("route_reflector.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    BgpGroup clients = vc.getRouters().get("Base").getBgpProcess().getGroups().get("clients");
    assertThat(clients.getClusterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(clients.getNextHopSelf(), equalTo(true));
    BgpNeighbor nbr = vc.getRouters().get("Base").getBgpProcess().getNeighbors().get("10.0.0.1");
    // inherited from the group
    assertThat(nbr.getClusterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(nbr.getNextHopSelf(), equalTo(true));
  }

  /** from-protocol extraction: {@code from protocol name [static]} on a policy entry. */
  @Test
  public void testFromProtocolExtraction() {
    SrosConfiguration vc = parseVendorConfig("redistribute_static.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    PolicyStatement ps = vc.getPolicyStatements().get("export-redist");
    assertThat(
        ps.getEntries().get(20L).getFromProtocols(),
        contains(org.batfish.vendor.sros.representation.FromProtocol.STATIC));
  }

  /**
   * Extraction warns (never silently skips/defaults) on: an illegal prefix-list 'through' missing
   * its length, an inverted 'range', an unrecognized from-protocol, and a non-boolean flag value.
   * Each is a line-stamped parse warning.
   */
  @Test
  public void testExtractionWarnings() {
    SrosConfiguration vc = parseVendorConfig("warnings.txt");
    // through entry with no through-length, and an inverted range.
    warningOnLine(vc, "prefix-list 'through' entry is missing through-length");
    warningOnLine(vc, "prefix-list range start-length 30 is greater than end-length 24");
    // unrecognized from-protocol is warned and dropped; the valid one is kept.
    warningOnLine(vc, "unrecognized from-protocol 'bogusproto'");
    assertThat(
        vc.getPolicyStatements().get("p").getEntries().get(10L).getFromProtocols(),
        contains(org.batfish.vendor.sros.representation.FromProtocol.STATIC));
    // non-boolean next-hop-self value is warned, flag left unset (null).
    warningOnLine(vc, "expected next-hop-self to be true or false but got 'bogus'");
    assertThat(
        vc.getRouters().get("Base").getBgpProcess().getGroups().get("g").getNextHopSelf(),
        nullValue());
  }

  /**
   * BGP group→neighbor inheritance is resolved on the model at extraction: a neighbor that sets
   * none of {@code type}/{@code peer-as}/{@code import}/{@code export} inherits them from its
   * group.
   */
  @Test
  public void testBgpNeighborInheritsFromGroup() {
    SrosConfiguration vc = parseVendorConfig("bgp_type_and_inheritance.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    BgpNeighbor neighbor =
        vc.getRouters().get("Base").getBgpProcess().getNeighbors().get("10.0.0.1");
    assertThat(neighbor.getType(), equalTo(PeerType.EXTERNAL));
    assertThat(neighbor.getPeerAs(), equalTo(65002L));
    assertThat(neighbor.getImportPolicies(), contains("imp"));
  }

  /**
   * apply-groups expansion: a group applied at {@code router "Base"} contributes {@code bgp group
   * "ebgp" peer-as}, while the locally-configured import policy is preserved (local wins).
   */
  @Test
  public void testApplyGroupsExpansion() {
    SrosConfiguration vc = parseVendorConfig("apply_groups.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    BgpGroup ebgp = vc.getRouters().get("Base").getBgpProcess().getGroups().get("ebgp");
    assertThat(ebgp.getPeerAs(), equalTo(65010L)); // inherited from the group
    assertThat(ebgp.getImportPolicies(), contains("import-all")); // local config preserved
  }

  /**
   * apply-groups with a regex list-key ({@code interface "<to-.*>"}) grafts onto matching branches,
   * and {@code apply-groups-exclude} suppresses inheritance at a branch.
   */
  @Test
  public void testApplyGroupsRegexAndExclude() {
    SrosConfiguration vc = parseVendorConfig("apply_groups_regex.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    Router base = vc.getRouters().get("Base");
    // to-r2 matches the regex key and inherits prefix-length 31.
    assertThat(base.getInterfaces().get("to-r2").getPrimaryPrefixLength(), equalTo(31));
    // to-r3 excludes the group, so it does not inherit prefix-length.
    assertThat(base.getInterfaces().get("to-r3").getPrimaryPrefixLength(), nullValue());
  }

  /**
   * delete edit: a trailing flat {@code /configure router "Base" delete interface "to-r2"} removes
   * that interface from the materialized model, leaving the others.
   */
  @Test
  public void testDeleteEdit() {
    SrosConfiguration vc = parseVendorConfig("delete_edit.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    Router base = vc.getRouters().get("Base");
    assertThat(base.getInterfaces().keySet(), contains("system"));
  }

  /**
   * A malformed/out-of-range value produces a line-stamped {@link ParseWarning} (annotate-visible),
   * not a context-free red-flag. {@code bad_values.txt} has an out-of-range autonomous-system on
   * line 4 and an out-of-range prefix-length on line 9.
   */
  @Test
  public void testBadValuesWarnWithLines() {
    SrosConfiguration vc = parseVendorConfig("bad_values.txt");

    // No silent red-flags: the value problems are reported as ParseWarnings instead.
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());

    assertThat(
        warningOnLine(vc, "Expected autonomous-system in range 1-4294967295, but got '5000000000'"),
        equalTo(7));
    assertThat(
        warningOnLine(vc, "Expected prefix-length in range 0-32, but got '33'"), equalTo(12));

    // The out-of-range values are dropped (not applied) on the model.
    Router base = vc.getRouters().get("Base");
    assertThat(base.getAutonomousSystem(), nullValue());
    assertThat(base.getInterfaces().get("to-r2").getPrimaryPrefixLength(), nullValue());
  }

  /**
   * A bad value inherited via apply-groups is cited to the group's source line (where the value is
   * actually written), not the applying branch. {@code apply_groups_bad_value.txt} writes the
   * out-of-range autonomous-system inside the group on line 9.
   */
  @Test
  public void testApplyGroupsInheritedBadValueCitesGroupLine() {
    SrosConfiguration vc = parseVendorConfig("apply_groups_bad_value.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    assertThat(
        warningOnLine(vc, "Expected autonomous-system in range 1-4294967295, but got '5000000000'"),
        equalTo(9));
  }

  /**
   * An unrecognized prefix-list match type produces a line-stamped {@link ParseWarning} and the
   * entry is dropped. {@code bad_prefix_type.txt} has {@code type bogus} on line 8.
   */
  @Test
  public void testUnrecognizedPrefixTypeWarns() {
    SrosConfiguration vc = parseVendorConfig("bad_prefix_type.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    assertThat(warningOnLine(vc, "unrecognized prefix-list match type 'bogus'"), equalTo(8));
    // The entry with the bad type is dropped, leaving the prefix-list empty.
    assertThat(vc.getPrefixLists().get("system-pfx").getEntries(), empty());
  }

  /**
   * Unrecognized enumerated values (port {@code admin-state}, policy {@code action-type}) warn with
   * a line and leave the field unset, like the prefix-list match type. {@code bad_enums.txt} has
   * {@code admin-state bogus-state} on line 7 and {@code action-type bogus-action} on line 13.
   */
  @Test
  public void testUnrecognizedEnumsWarn() {
    SrosConfiguration vc = parseVendorConfig("bad_enums.txt");
    assertThat(vc.getWarnings().getRedFlagWarnings(), empty());
    assertThat(warningOnLine(vc, "unrecognized admin-state 'bogus-state'"), equalTo(7));
    assertThat(warningOnLine(vc, "unrecognized action-type 'bogus-action'"), equalTo(13));
    // The unrecognized values leave the corresponding fields unset.
    assertThat(vc.getPorts().get("1/1/c1/1").getAdminStateEnable(), nullValue());
    assertThat(vc.getPolicyStatements().get("p").getEntries().get(10L).getAction(), nullValue());
  }

  /** Returns the source line of the single parse warning with the given comment. */
  private static int warningOnLine(SrosConfiguration vc, String comment) {
    List<ParseWarning> matching =
        vc.getWarnings().getParseWarnings().stream()
            .filter(w -> w.getComment().equals(comment))
            .collect(java.util.stream.Collectors.toList());
    assertThat("exactly one warning with comment: " + comment, matching, hasSize(1));
    return matching.get(0).getLine();
  }

  private static @Nonnull SrosConfiguration parseVendorConfig(String filename) {
    String src = readResource(TESTCONFIGS_PREFIX + filename, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    SrosCombinedParser parser = new SrosCombinedParser(src, settings);
    Warnings warnings = new Warnings(true, true, true);
    SrosControlPlaneExtractor extractor =
        new SrosControlPlaneExtractor(src, parser, warnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    SrosConfiguration vc = (SrosConfiguration) extractor.getVendorConfiguration();
    vc.setFilename(TESTCONFIGS_PREFIX + filename);
    // Crash if not serializable.
    vc = SerializationUtils.clone(vc);
    vc.setWarnings(warnings);
    return vc;
  }

  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/sros/grammar/testconfigs/";
}
