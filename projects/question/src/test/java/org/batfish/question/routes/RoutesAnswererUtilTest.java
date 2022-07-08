package org.batfish.question.routes;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.questions.BgpRouteStatus.BACKUP;
import static org.batfish.datamodel.questions.BgpRouteStatus.BEST;
import static org.batfish.datamodel.table.TableDiff.COL_BASE_PREFIX;
import static org.batfish.question.routes.RoutesAnswerer.COL_ADMIN_DISTANCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_AS_PATH;
import static org.batfish.question.routes.RoutesAnswerer.COL_CLUSTER_LIST;
import static org.batfish.question.routes.RoutesAnswerer.COL_COMMUNITIES;
import static org.batfish.question.routes.RoutesAnswerer.COL_LOCAL_PREF;
import static org.batfish.question.routes.RoutesAnswerer.COL_METRIC;
import static org.batfish.question.routes.RoutesAnswerer.COL_NETWORK;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP_INTERFACE;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP_IP;
import static org.batfish.question.routes.RoutesAnswerer.COL_NODE;
import static org.batfish.question.routes.RoutesAnswerer.COL_ORIGINATOR_ID;
import static org.batfish.question.routes.RoutesAnswerer.COL_ORIGIN_PROTOCOL;
import static org.batfish.question.routes.RoutesAnswerer.COL_PATH_ID;
import static org.batfish.question.routes.RoutesAnswerer.COL_PROTOCOL;
import static org.batfish.question.routes.RoutesAnswerer.COL_RECEIVED_FROM_IP;
import static org.batfish.question.routes.RoutesAnswerer.COL_ROUTE_DISTINGUISHER;
import static org.batfish.question.routes.RoutesAnswerer.COL_ROUTE_ENTRY_PRESENCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_TAG;
import static org.batfish.question.routes.RoutesAnswerer.COL_TUNNEL_ENCAPSULATION_ATTRIBUTE;
import static org.batfish.question.routes.RoutesAnswerer.COL_VRF_NAME;
import static org.batfish.question.routes.RoutesAnswerer.COL_WEIGHT;
import static org.batfish.question.routes.RoutesAnswererUtil.alignRouteRowAttributes;
import static org.batfish.question.routes.RoutesAnswererUtil.computeNextHopNode;
import static org.batfish.question.routes.RoutesAnswererUtil.getAbstractRouteRowsDiff;
import static org.batfish.question.routes.RoutesAnswererUtil.getBgpRibRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getBgpRouteRowsDiff;
import static org.batfish.question.routes.RoutesAnswererUtil.getEvpnRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getMainRibRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getMatchingPrefixRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getMatchingRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getRoutesDiff;
import static org.batfish.question.routes.RoutesAnswererUtil.groupBgpRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.groupRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.longestMatchingPrefix;
import static org.batfish.question.routes.RoutesAnswererUtil.populateRouteAttributes;
import static org.batfish.question.routes.RoutesAnswererUtil.prefixMatches;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.EvpnType3Route;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.OspfExternalType2Route;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRouteStatus;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.table.Row;
import org.batfish.question.routes.DiffRoutesOutput.KeyPresenceStatus;
import org.batfish.question.routes.RoutesAnswererUtil.RouteEntryPresenceStatus;
import org.batfish.question.routes.RoutesQuestion.PrefixMatchType;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.batfish.specifier.RoutingProtocolSpecifier;
import org.hamcrest.Matcher;
import org.junit.Test;

/** Tests for {@link RoutesAnswererUtil} */
public class RoutesAnswererUtilTest {

  @Test
  public void testAlignRtRowAttrs() {
    RouteRowAttribute rra1 = RouteRowAttribute.builder().setAdminDistance(10).build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setAdminDistance(30).build();
    RouteRowAttribute rra5 = RouteRowAttribute.builder().setAdminDistance(50).build();

    RouteRowAttribute rra2 = RouteRowAttribute.builder().setAdminDistance(20).build();
    RouteRowAttribute rra4 = RouteRowAttribute.builder().setAdminDistance(40).build();

    List<List<RouteRowAttribute>> alignedRouteRowattrs =
        alignRouteRowAttributes(
            ImmutableList.of(rra1, rra3, rra5), ImmutableList.of(rra2, rra4, rra5));

    // expected result after merging the two lists
    List<List<RouteRowAttribute>> expectedOutput =
        ImmutableList.of(
            Lists.newArrayList(rra1, null),
            Lists.newArrayList(null, rra2),
            Lists.newArrayList(rra3, null),
            Lists.newArrayList(null, rra4),
            Lists.newArrayList(rra5, rra5));

    assertThat(alignedRouteRowattrs, equalTo(expectedOutput));
  }

  @Test
  public void testAlignRtRowAttrsTrailingNulls1() {
    RouteRowAttribute rra1 = RouteRowAttribute.builder().setAdminDistance(10).build();
    RouteRowAttribute rra2 = RouteRowAttribute.builder().setAdminDistance(20).build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setAdminDistance(30).build();

    List<List<RouteRowAttribute>> alignedRouteRowattrs =
        alignRouteRowAttributes(ImmutableList.of(rra1, rra2, rra3), ImmutableList.of(rra1));

    // null trails for non-matching attributes
    List<List<RouteRowAttribute>> expectedOutput =
        ImmutableList.of(
            Lists.newArrayList(rra1, rra1),
            Lists.newArrayList(rra2, null),
            Lists.newArrayList(rra3, null));

    assertThat(alignedRouteRowattrs, equalTo(expectedOutput));
  }

  @Test
  public void testAlignRtRowAttrsTrailingNulls2() {
    RouteRowAttribute rra1 = RouteRowAttribute.builder().setAdminDistance(10).build();
    RouteRowAttribute rra2 = RouteRowAttribute.builder().setAdminDistance(20).build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setAdminDistance(30).build();

    List<List<RouteRowAttribute>> alignedRouteRowattrs =
        alignRouteRowAttributes(ImmutableList.of(rra1), ImmutableList.of(rra1, rra2, rra3));

    // null trails for non-matching attributes
    List<List<RouteRowAttribute>> expectedOutput =
        ImmutableList.of(
            Lists.newArrayList(rra1, rra1),
            Lists.newArrayList(null, rra2),
            Lists.newArrayList(null, rra3));

    assertThat(alignedRouteRowattrs, equalTo(expectedOutput));
  }

  @Test
  public void testAlignRtRowAttrsLeadingNulls() {
    RouteRowAttribute rra1 = RouteRowAttribute.builder().setAdminDistance(10).build();
    RouteRowAttribute rra2 = RouteRowAttribute.builder().setAdminDistance(20).build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setAdminDistance(30).build();

    List<List<RouteRowAttribute>> alignedRouteRowattrs =
        alignRouteRowAttributes(ImmutableList.of(rra3), ImmutableList.of(rra1, rra2, rra3));

    // trailing nulls for non-matching attributes
    List<List<RouteRowAttribute>> expectedOutput =
        ImmutableList.of(
            Lists.newArrayList(null, rra1),
            Lists.newArrayList(null, rra2),
            Lists.newArrayList(rra3, rra3));

    assertThat(alignedRouteRowattrs, equalTo(expectedOutput));
  }

  @Test
  public void testMainRibColumnsValue() {
    AbstractRoute route =
        OspfExternalType2Route.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHop(NextHopInterface.of("e0", Ip.parse("1.1.1.2")))
            .setAdmin(10)
            .setMetric(2L << 34)
            .setLsaMetric(2)
            .setCostToAdvertiser(2)
            .setArea(1L)
            .setAdvertiser("n2")
            .setOspfMetricType(OspfMetricType.E2)
            .setTag(2L << 35)
            .build();
    FinalMainRib rib = FinalMainRib.of(route);

    Multiset<Row> actual =
        getMainRibRoutes(
            ImmutableTable.of("n1", Configuration.DEFAULT_VRF_NAME, rib),
            ImmutableMultimap.of("n1", Configuration.DEFAULT_VRF_NAME),
            null,
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER,
            PrefixMatchType.EXACT);
    assertThat(
        actual,
        contains(
            allOf(
                hasColumn(COL_NODE, new Node("n1"), Schema.NODE),
                hasColumn(COL_VRF_NAME, Configuration.DEFAULT_VRF_NAME, Schema.STRING),
                hasColumn(COL_NETWORK, Prefix.parse("1.1.1.0/24"), Schema.PREFIX),
                hasColumn(COL_NEXT_HOP_IP, Ip.parse("1.1.1.2"), Schema.IP),
                hasColumn(COL_NEXT_HOP_INTERFACE, "e0", Schema.STRING),
                hasColumn(COL_PROTOCOL, "ospfE2", Schema.STRING),
                hasColumn(COL_TAG, equalTo(2L << 35), Schema.LONG),
                hasColumn(COL_ADMIN_DISTANCE, equalTo(10), Schema.INTEGER),
                hasColumn(COL_METRIC, equalTo(2L << 34), Schema.LONG))));
  }

  @Test
  public void testBgpRibRouteColumnsValue() {
    // Create two BGP routes: one standard route and one from a BGP unnumbered session
    Ip ip = Ip.parse("1.1.1.1");
    Prefix prefix = ip.toPrefix();
    Ip bgpUnnumIp = Ip.parse("169.254.0.1");
    Bgpv4Route.Builder rb =
        Bgpv4Route.testBuilder()
            .setNetwork(prefix)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setCommunities(ImmutableSortedSet.of(StandardCommunity.of(65537L)))
            .setProtocol(RoutingProtocol.BGP)
            .setOriginatorIp(Ip.parse("1.1.1.2"))
            .setPathId(1)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("3.3.3.3")))
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .setWeight(7);
    Bgpv4Route standardRoute = rb.setNextHopIp(ip).build();
    Bgpv4Route unnumRoute = rb.setNextHopIp(bgpUnnumIp).setNextHopInterface("iface").build();

    Table<String, String, Set<Bgpv4Route>> bgpRouteTable = HashBasedTable.create();
    bgpRouteTable.put("node", "vrf", ImmutableSet.of(standardRoute, unnumRoute));
    Multiset<Row> rows =
        getBgpRibRoutes(
            bgpRouteTable,
            ImmutableTable.of(),
            ImmutableMultimap.of("node", "vrf"),
            null,
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER,
            ImmutableSet.of(BEST),
            PrefixMatchType.EXACT);

    // Both routes should have the same values for these columns
    Matcher<Row> commonMatcher =
        allOf(
            hasColumn(COL_NODE, new Node("node"), Schema.NODE),
            hasColumn(COL_VRF_NAME, "vrf", Schema.STRING),
            hasColumn(COL_NETWORK, prefix, Schema.PREFIX),
            hasColumn(COL_PROTOCOL, "bgp", Schema.STRING),
            hasColumn(COL_AS_PATH, "1 2", Schema.STRING),
            hasColumn(COL_METRIC, 0, Schema.INTEGER),
            hasColumn(COL_LOCAL_PREF, 0L, Schema.LONG),
            hasColumn(COL_COMMUNITIES, ImmutableList.of("1:1"), Schema.list(Schema.STRING)),
            hasColumn(COL_ORIGIN_PROTOCOL, nullValue(), Schema.STRING),
            hasColumn(COL_TAG, nullValue(), Schema.INTEGER),
            hasColumn(COL_ORIGINATOR_ID, Ip.parse("1.1.1.2"), Schema.IP),
            hasColumn(COL_RECEIVED_FROM_IP, Ip.parse("3.3.3.3"), Schema.IP),
            hasColumn(COL_PATH_ID, 1, Schema.INTEGER),
            hasColumn(COL_CLUSTER_LIST, nullValue(), Schema.list(Schema.LONG)),
            hasColumn(COL_TUNNEL_ENCAPSULATION_ATTRIBUTE, equalTo(null), Schema.STRING),
            hasColumn(COL_WEIGHT, 7, Schema.INTEGER));

    assertThat(
        rows,
        containsInAnyOrder(
            // Standard route
            allOf(
                commonMatcher,
                hasColumn(COL_NEXT_HOP_IP, ip, Schema.IP),
                hasColumn(COL_NEXT_HOP_INTERFACE, "dynamic", Schema.STRING)),
            // Route from BGP unnumbered session
            allOf(
                commonMatcher,
                hasColumn(COL_NEXT_HOP_IP, nullValue(), Schema.IP),
                hasColumn(COL_NEXT_HOP_INTERFACE, "iface", Schema.STRING))));
  }

  @Test
  public void testBgpRibRoutes_empty() {
    Multiset<Row> rows =
        getBgpRibRoutes(
            ImmutableTable.of(), // no BGP rib for the specified vrfs
            ImmutableTable.of(),
            ImmutableMultimap.of("node", "vrf"),
            null,
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER,
            ImmutableSet.of(BEST),
            PrefixMatchType.EXACT);
    assertThat(rows, empty());
  }

  @Test
  public void testGetBgpRoutesCommunities() {
    Ip ip = Ip.parse("1.1.1.1");
    Table<String, String, Set<Bgpv4Route>> bgpRouteTable = HashBasedTable.create();
    bgpRouteTable.put(
        "node",
        "vrf",
        ImmutableSet.of(
            Bgpv4Route.builder()
                .setNetwork(ip.toPrefix())
                .setOriginMechanism(OriginMechanism.LEARNED)
                .setOriginType(OriginType.IGP)
                .setOriginatorIp(ip)
                .setNextHop(NextHopIp.of(ip))
                .setReceivedFrom(ReceivedFromIp.of(ip))
                .setCommunities(ImmutableSortedSet.of(StandardCommunity.of(65537L)))
                .setProtocol(RoutingProtocol.BGP)
                .build()));
    Multiset<Row> rows =
        getBgpRibRoutes(
            bgpRouteTable,
            ImmutableTable.of(),
            ImmutableMultimap.of("node", "vrf"),
            null,
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER,
            ImmutableSet.of(BEST),
            PrefixMatchType.EXACT);
    assertThat(
        rows.iterator().next().get(COL_COMMUNITIES, Schema.list(Schema.STRING)),
        equalTo(ImmutableList.of("1:1")));
  }

  @Test
  public void testEvpnRibRouteColumns() {
    // Create two EVPN routes: one standard route and one from a BGP unnumbered session
    Ip ip = Ip.parse("1.1.1.1");
    Prefix prefix = ip.toPrefix();
    EvpnType3Route.Builder rb =
        EvpnType3Route.builder()
            .setVniIp(ip)
            .setRouteDistinguisher(RouteDistinguisher.from(ip, 1))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setCommunities(ImmutableSortedSet.of(StandardCommunity.of(65537L)))
            .setProtocol(RoutingProtocol.BGP)
            .setOriginatorIp(Ip.parse("1.1.1.2"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.2")))
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .setVni(1)
            .setWeight(7);
    EvpnType3Route standardRoute = rb.setNextHopIp(ip).build();

    Table<String, String, Set<EvpnRoute<?, ?>>> evpnRouteTable = HashBasedTable.create();
    evpnRouteTable.put("node", "vrf", ImmutableSet.of(standardRoute));
    Multiset<Row> rows =
        getEvpnRoutes(
            evpnRouteTable,
            ImmutableTable.of(),
            ImmutableMultimap.of("node", "vrf"),
            null,
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER,
            ImmutableSet.of(BEST),
            PrefixMatchType.EXACT);

    assertThat(
        rows,
        contains(
            allOf(
                hasColumn(COL_NODE, new Node("node"), Schema.NODE),
                hasColumn(COL_VRF_NAME, "vrf", Schema.STRING),
                hasColumn(COL_NETWORK, prefix, Schema.PREFIX),
                hasColumn(
                    COL_ROUTE_DISTINGUISHER,
                    RouteDistinguisher.from(ip, 1).toString(),
                    Schema.STRING),
                hasColumn(COL_PROTOCOL, "bgp", Schema.STRING),
                hasColumn(COL_AS_PATH, "1 2", Schema.STRING),
                hasColumn(COL_METRIC, 0, Schema.INTEGER),
                hasColumn(COL_LOCAL_PREF, 0L, Schema.LONG),
                hasColumn(COL_COMMUNITIES, ImmutableList.of("1:1"), Schema.list(Schema.STRING)),
                hasColumn(COL_ORIGIN_PROTOCOL, nullValue(), Schema.STRING),
                hasColumn(COL_PATH_ID, nullValue(), Schema.INTEGER),
                hasColumn(COL_TUNNEL_ENCAPSULATION_ATTRIBUTE, equalTo(null), Schema.STRING),
                hasColumn(COL_WEIGHT, 7, Schema.INTEGER),
                hasColumn(COL_TAG, nullValue(), Schema.INTEGER),
                hasColumn(COL_NEXT_HOP_IP, ip, Schema.IP),
                hasColumn(COL_NEXT_HOP_INTERFACE, "dynamic", Schema.STRING))));
  }

  @Test
  public void testEvpnRibRoutes_empty() {
    Multiset<Row> rows =
        getEvpnRoutes(
            ImmutableTable.of(), // no EVPN rib for the specified vrfs
            ImmutableTable.of(),
            ImmutableMultimap.of("node", "vrf"),
            null,
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER,
            ImmutableSet.of(BEST, BACKUP),
            PrefixMatchType.EXACT);
    assertThat(rows, empty());
  }

  @Test
  public void testComputeNextHopNode() {
    assertThat(computeNextHopNode(null, ImmutableMap.of()), nullValue());
    assertThat(computeNextHopNode(Ip.parse("1.1.1.1"), null), nullValue());
    assertThat(computeNextHopNode(Ip.parse("1.1.1.1"), ImmutableMap.of()), nullValue());
    assertThat(
        computeNextHopNode(
            Ip.parse("1.1.1.1"), ImmutableMap.of(Ip.parse("1.1.1.1"), ImmutableSet.of("n1"))),
        equalTo("n1"));
    assertThat(
        computeNextHopNode(
            Ip.parse("1.1.1.1"), ImmutableMap.of(Ip.parse("1.1.1.2"), ImmutableSet.of("n1"))),
        nullValue());
    assertThat(
        computeNextHopNode(
            Ip.parse("1.1.1.1"), ImmutableMap.of(Ip.parse("1.1.1.1"), ImmutableSet.of("n1", "n2"))),
        nullValue());
  }

  @Test
  public void testBgpRoutesRowDiff() {
    RouteRowAttribute.Builder routeRowAttrBuilder =
        RouteRowAttribute.builder()
            .setAdminDistance(200)
            .setMetric(2L)
            .setOriginProtocol("bgp")
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .setLocalPreference(1L)
            .setWeight(7);
    List<List<RouteRowAttribute>> diffMatrix = new ArrayList<>();
    diffMatrix.add(Lists.newArrayList(routeRowAttrBuilder.build(), routeRowAttrBuilder.build()));

    List<List<RouteRowAttribute>> diffMatrixChanged = new ArrayList<>();
    diffMatrix.add(
        Lists.newArrayList(routeRowAttrBuilder.build(), routeRowAttrBuilder.setMetric(1L).build()));

    List<List<RouteRowAttribute>> diffMatrixMissingRefs = new ArrayList<>();
    diffMatrixMissingRefs.add(Lists.newArrayList(routeRowAttrBuilder.build(), null));

    List<List<RouteRowAttribute>> diffMatrixMissingBase = new ArrayList<>();
    diffMatrixMissingBase.add(Lists.newArrayList(null, routeRowAttrBuilder.build()));

    Ip nextHopIp = Ip.parse("1.1.1.2");
    NextHop nextHop = NextHopIp.of(nextHopIp);

    Ip receivedFromIp = Ip.parse("2.2.2.2");

    List<DiffRoutesOutput> diff =
        ImmutableList.of(
            new DiffRoutesOutput(
                new RouteRowKey("node", "vrf", Prefix.parse("1.1.1.1/24")),
                new BgpRouteRowSecondaryKey(nextHop, "bgp", receivedFromIp),
                KeyPresenceStatus.IN_BOTH,
                diffMatrix,
                KeyPresenceStatus.IN_BOTH),
            new DiffRoutesOutput(
                new RouteRowKey("node", "vrf", Prefix.parse("1.1.1.1/24")),
                new BgpRouteRowSecondaryKey(nextHop, "bgp", receivedFromIp),
                KeyPresenceStatus.IN_BOTH,
                diffMatrixChanged,
                KeyPresenceStatus.IN_BOTH),
            new DiffRoutesOutput(
                new RouteRowKey("node", "vrf", Prefix.parse("1.1.1.1/24")),
                new BgpRouteRowSecondaryKey(nextHop, "bgp", receivedFromIp),
                KeyPresenceStatus.ONLY_IN_SNAPSHOT,
                diffMatrixMissingRefs,
                KeyPresenceStatus.IN_BOTH),
            new DiffRoutesOutput(
                new RouteRowKey("node", "vrf", Prefix.parse("1.1.1.1/24")),
                new BgpRouteRowSecondaryKey(nextHop, "bgp", receivedFromIp),
                KeyPresenceStatus.ONLY_IN_REFERENCE,
                diffMatrixMissingBase,
                KeyPresenceStatus.IN_BOTH),
            new DiffRoutesOutput(
                new RouteRowKey("node", "vrf", Prefix.parse("1.1.1.1/24")),
                new BgpRouteRowSecondaryKey(nextHop, "bgp", receivedFromIp),
                KeyPresenceStatus.IN_BOTH,
                diffMatrixMissingRefs,
                KeyPresenceStatus.IN_BOTH),
            new DiffRoutesOutput(
                new RouteRowKey("node", "vrf", Prefix.parse("1.1.1.1/24")),
                new BgpRouteRowSecondaryKey(nextHop, "bgp", receivedFromIp),
                KeyPresenceStatus.IN_BOTH,
                diffMatrixMissingBase,
                KeyPresenceStatus.IN_BOTH));
    Multiset<Row> rows = getBgpRouteRowsDiff(diff, RibProtocol.BGP);

    assertThat(
        rows,
        containsInAnyOrder(
            ImmutableList.of(
                hasColumn(
                    COL_ROUTE_ENTRY_PRESENCE,
                    equalTo(RouteEntryPresenceStatus.UNCHANGED.routeEntryPresenceName()),
                    Schema.STRING),
                hasColumn(
                    COL_ROUTE_ENTRY_PRESENCE,
                    equalTo(RouteEntryPresenceStatus.CHANGED.routeEntryPresenceName()),
                    Schema.STRING),
                hasColumn(
                    COL_ROUTE_ENTRY_PRESENCE,
                    equalTo(RouteEntryPresenceStatus.ONLY_IN_SNAPSHOT.routeEntryPresenceName()),
                    Schema.STRING),
                hasColumn(
                    COL_ROUTE_ENTRY_PRESENCE,
                    equalTo(RouteEntryPresenceStatus.ONLY_IN_REFERENCE.routeEntryPresenceName()),
                    Schema.STRING),
                hasColumn(
                    COL_ROUTE_ENTRY_PRESENCE,
                    equalTo(RouteEntryPresenceStatus.ONLY_IN_SNAPSHOT.routeEntryPresenceName()),
                    Schema.STRING),
                hasColumn(
                    COL_ROUTE_ENTRY_PRESENCE,
                    equalTo(RouteEntryPresenceStatus.ONLY_IN_REFERENCE.routeEntryPresenceName()),
                    Schema.STRING))));
  }

  @Test
  public void testGroupMatchingRoutesByPrefix() {
    FinalMainRib rib =
        FinalMainRib.of(
            OspfExternalType2Route.builder()
                .setNetwork(Prefix.parse("1.1.1.0/24"))
                .setNextHop(NextHopInterface.of("e0", Ip.parse("1.1.1.2")))
                .setAdmin(10)
                .setMetric(30L)
                .setLsaMetric(2)
                .setCostToAdvertiser(2)
                .setArea(1L)
                .setAdvertiser("n2")
                .setOspfMetricType(OspfMetricType.E2)
                .build(),
            OspfExternalType2Route.builder()
                .setNetwork(Prefix.parse("1.1.1.0/24"))
                .setNextHop(NextHopInterface.of("e0", Ip.parse("1.1.1.3")))
                .setAdmin(10)
                .setMetric(20L)
                .setLsaMetric(2)
                .setCostToAdvertiser(2)
                .setArea(1L)
                .setAdvertiser("n2")
                .setOspfMetricType(OspfMetricType.E2)
                .build());

    Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>> grouped =
        groupRoutes(
            ImmutableTable.of("n1", Configuration.DEFAULT_VRF_NAME, rib),
            ImmutableSet.of("n1"),
            null,
            ".*",
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER);

    assertThat(grouped.keySet(), hasSize(1));
    RouteRowKey expectedKey =
        new RouteRowKey("n1", Configuration.DEFAULT_VRF_NAME, Prefix.parse("1.1.1.0/24"));
    assertThat(grouped.keySet().iterator().next(), equalTo(expectedKey));

    Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> innerGroup = grouped.get(expectedKey);

    // checking equality of inner group
    Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> expectedInnerMap =
        ImmutableMap.of(
            new MainRibRouteRowSecondaryKey(
                NextHopInterface.of("e0", Ip.parse("1.1.1.2")), "ospfE2"),
            ImmutableSortedSet.of(
                RouteRowAttribute.builder()
                    .setAdminDistance(10)
                    .setMetric(30L)
                    .setNextHopInterface("e0")
                    .build()),
            new MainRibRouteRowSecondaryKey(
                NextHopInterface.of("e0", Ip.parse("1.1.1.3")), "ospfE2"),
            ImmutableSortedSet.of(
                RouteRowAttribute.builder()
                    .setAdminDistance(10)
                    .setMetric(20L)
                    .setNextHopInterface("e0")
                    .build()));
    // matching the secondary key
    assertThat(innerGroup, equalTo(expectedInnerMap));
  }

  @Test
  public void testGroupBgpRoutes_filterByProtocol() {
    Bgpv4Route bgpRoute =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.2"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.2")))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.parse("1.1.1.2"))
            .setProtocol(RoutingProtocol.BGP)
            .setLocalPreference(1L)
            .setAdmin(10)
            .setMetric(30L)
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .build();

    Bgpv4Route ibgpRoute =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.3"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.2")))
            .setAdmin(10)
            .setMetric(20L)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.parse("1.1.1.2"))
            .setProtocol(RoutingProtocol.IBGP)
            .setLocalPreference(1L)
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .build();

    Table<String, String, Set<Bgpv4Route>> bgpTable = HashBasedTable.create();
    bgpTable.row("node").put(Configuration.DEFAULT_VRF_NAME, new HashSet<>());
    bgpTable.row("node").get(Configuration.DEFAULT_VRF_NAME).add(bgpRoute);
    bgpTable.row("node").get(Configuration.DEFAULT_VRF_NAME).add(ibgpRoute);

    Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>> grouped =
        groupBgpRoutes(
            bgpTable,
            ImmutableTable.of(),
            ImmutableSet.of("node"),
            ".*",
            null,
            // only include the IBGP route
            new RoutingProtocolSpecifier("IBGP"));

    assertThat(grouped.keySet(), hasSize(1));

    RouteRowKey expectedKey =
        new RouteRowKey("node", Configuration.DEFAULT_VRF_NAME, Prefix.parse("1.1.1.0/24"));
    assertThat(grouped.keySet(), contains(expectedKey));

    Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> innerGroup = grouped.get(expectedKey);

    // only the ibgp route is included because of the RoutingProtocolSpecifier above
    Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> expectedInnerMap =
        ImmutableMap.of(
            new BgpRouteRowSecondaryKey(
                NextHopIp.of(Ip.parse("1.1.1.3")), "ibgp", Ip.parse("2.2.2.2")),
            ImmutableSortedSet.of(
                RouteRowAttribute.builder()
                    .setAdminDistance(10)
                    .setMetric(20L)
                    .setLocalPreference(1L)
                    .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
                    .setOriginType(OriginType.IGP)
                    .setStatus(BEST)
                    .build()));
    // matching the secondary key
    assertThat(innerGroup, equalTo(expectedInnerMap));
  }

  @Test
  public void testGroupMatchingBgpRoutesByPrefix() {
    Bgpv4Route bgpv4Route1 =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.2"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.2")))
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.parse("1.1.1.2"))
            .setProtocol(RoutingProtocol.BGP)
            .setLocalPreference(1L)
            .setAdmin(10)
            .setMetric(30L)
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .build();

    Bgpv4Route bgpv4Route2 =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.3"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.3")))
            .setAdmin(10)
            .setMetric(20L)
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.parse("1.1.1.2"))
            .setProtocol(RoutingProtocol.BGP)
            .setLocalPreference(1L)
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .build();

    Table<String, String, Set<Bgpv4Route>> bgpTable = HashBasedTable.create();
    bgpTable.row("node").put(Configuration.DEFAULT_VRF_NAME, new HashSet<>());
    bgpTable.row("node").get(Configuration.DEFAULT_VRF_NAME).add(bgpv4Route1);
    bgpTable.row("node").get(Configuration.DEFAULT_VRF_NAME).add(bgpv4Route2);

    Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>> grouped =
        groupBgpRoutes(
            bgpTable,
            ImmutableTable.of(),
            ImmutableSet.of("node"),
            ".*",
            null,
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER);

    assertThat(grouped.keySet(), hasSize(1));

    RouteRowKey expectedKey =
        new RouteRowKey("node", Configuration.DEFAULT_VRF_NAME, Prefix.parse("1.1.1.0/24"));
    assertThat(grouped.keySet(), contains(expectedKey));

    Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> innerGroup = grouped.get(expectedKey);

    // checking equality of inner group
    Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> expectedInnerMap =
        ImmutableMap.of(
            new BgpRouteRowSecondaryKey(
                NextHopIp.of(Ip.parse("1.1.1.2")), "bgp", Ip.parse("1.1.1.2")),
            ImmutableSortedSet.of(
                RouteRowAttribute.builder()
                    .setAdminDistance(10)
                    .setMetric(30L)
                    .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
                    .setLocalPreference(1L)
                    .setOriginType(OriginType.IGP)
                    .setStatus(BEST)
                    .build()),
            new BgpRouteRowSecondaryKey(
                NextHopIp.of(Ip.parse("1.1.1.3")), "bgp", Ip.parse("1.1.1.3")),
            ImmutableSortedSet.of(
                RouteRowAttribute.builder()
                    .setAdminDistance(10)
                    .setMetric(20L)
                    .setLocalPreference(1L)
                    .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
                    .setOriginType(OriginType.IGP)
                    .setStatus(BEST)
                    .build()));
    // matching the secondary key
    assertThat(innerGroup, equalTo(expectedInnerMap));
  }

  /** Test that groupBgpRoutes produces a diff when a route goes from back to best. */
  @Test
  public void testGroupBgpRoutes_backup() {
    Bgpv4Route route =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.2"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.2")))
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.parse("1.1.1.2"))
            .setProtocol(RoutingProtocol.BGP)
            .setLocalPreference(1L)
            .setAdmin(10)
            .setMetric(30L)
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .build();

    Table<String, String, Set<Bgpv4Route>> backupRoutes = HashBasedTable.create();
    backupRoutes.row("node").put(Configuration.DEFAULT_VRF_NAME, ImmutableSet.of(route));

    Map<RouteRowKey, Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>> grouped =
        groupBgpRoutes(
            ImmutableTable.of(),
            backupRoutes,
            ImmutableSet.of("node"),
            ".*",
            null,
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER);

    assertThat(grouped.keySet(), hasSize(1));

    RouteRowKey expectedKey =
        new RouteRowKey("node", Configuration.DEFAULT_VRF_NAME, Prefix.parse("1.1.1.0/24"));
    assertThat(grouped.keySet(), contains(expectedKey));

    Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> innerGroup = grouped.get(expectedKey);

    // checking equality of inner group
    Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> expectedInnerMap =
        ImmutableMap.of(
            new BgpRouteRowSecondaryKey(
                NextHopIp.of(Ip.parse("1.1.1.2")), "bgp", Ip.parse("1.1.1.2")),
            ImmutableSortedSet.of(
                RouteRowAttribute.builder()
                    .setAdminDistance(10)
                    .setMetric(30L)
                    .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
                    .setLocalPreference(1L)
                    .setOriginType(OriginType.IGP)
                    .setStatus(BACKUP)
                    .build()));
    // matching the secondary key
    assertThat(innerGroup, equalTo(expectedInnerMap));
  }

  @Test
  public void testGetRoutesDiffCommonKey() {
    RouteRowKey routeRowKey = new RouteRowKey("node", "vrf", Prefix.parse("2.2.2.2/24"));
    RouteRowSecondaryKey rrsk1 =
        new BgpRouteRowSecondaryKey(NextHopIp.of(Ip.parse("1.1.1.1")), "bgp", null);
    RouteRowSecondaryKey rrsk2 =
        new BgpRouteRowSecondaryKey(NextHopIp.of(Ip.parse("1.1.1.2")), "bgp", null);
    RouteRowSecondaryKey rrsk3 =
        new BgpRouteRowSecondaryKey(NextHopIp.of(Ip.parse("1.1.1.3")), "bgp", null);
    RouteRowSecondaryKey rrsk4 =
        new BgpRouteRowSecondaryKey(NextHopIp.of(Ip.parse("1.1.1.4")), "bgp", null);

    RouteRowAttribute rra1 = RouteRowAttribute.builder().setAdminDistance(10).build();
    RouteRowAttribute rra2 = RouteRowAttribute.builder().setAdminDistance(20).build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setAdminDistance(30).build();
    RouteRowAttribute rra4 = RouteRowAttribute.builder().setAdminDistance(40).build();
    RouteRowAttribute rra5 = RouteRowAttribute.builder().setAdminDistance(50).build();

    Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> innerGroupsInBase =
        ImmutableMap.<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>builder()
            .put(rrsk1, ImmutableSortedSet.of(rra1))
            .put(rrsk2, ImmutableSortedSet.of(rra1, rra2))
            .put(rrsk3, ImmutableSortedSet.of(rra4))
            .build();

    Map<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>> innerGroupsInRef =
        ImmutableMap.<RouteRowSecondaryKey, SortedSet<RouteRowAttribute>>builder()
            .put(rrsk1, ImmutableSortedSet.of(rra2))
            .put(rrsk2, ImmutableSortedSet.of(rra1, rra3))
            .put(rrsk4, ImmutableSortedSet.of(rra5))
            .build();

    List<DiffRoutesOutput> diffRoutesOutputs =
        getRoutesDiff(
            ImmutableMap.of(routeRowKey, innerGroupsInBase),
            ImmutableMap.of(routeRowKey, innerGroupsInRef));

    assertThat(
        diffRoutesOutputs,
        containsInAnyOrder(
            new DiffRoutesOutput(
                routeRowKey,
                rrsk1,
                KeyPresenceStatus.IN_BOTH,
                ImmutableList.of(Lists.newArrayList(rra1, rra2)),
                KeyPresenceStatus.IN_BOTH),
            new DiffRoutesOutput(
                routeRowKey,
                rrsk2,
                KeyPresenceStatus.IN_BOTH,
                ImmutableList.of(
                    Lists.newArrayList(rra1, rra1),
                    Lists.newArrayList(rra2, null),
                    Lists.newArrayList(null, rra3)),
                KeyPresenceStatus.IN_BOTH),
            new DiffRoutesOutput(
                routeRowKey,
                rrsk3,
                KeyPresenceStatus.ONLY_IN_SNAPSHOT,
                ImmutableList.of(Lists.newArrayList(rra4, null)),
                KeyPresenceStatus.IN_BOTH),
            new DiffRoutesOutput(
                routeRowKey,
                rrsk4,
                KeyPresenceStatus.ONLY_IN_REFERENCE,
                ImmutableList.of(Lists.newArrayList(null, rra5)),
                KeyPresenceStatus.IN_BOTH)));
  }

  @Test
  public void testGetRoutesDiffNonCommonKey() {
    RouteRowKey routeRowKey1 = new RouteRowKey("node1", "vrf", Prefix.parse("1.1.1.1/24"));
    RouteRowKey routeRowKey2 = new RouteRowKey("node2", "vrf", Prefix.parse("1.1.1.2/24"));

    RouteRowSecondaryKey rrsk1 =
        new BgpRouteRowSecondaryKey(NextHopIp.of(Ip.parse("1.1.1.1")), "bgp", null);
    RouteRowSecondaryKey rrsk2 =
        new BgpRouteRowSecondaryKey(NextHopIp.of(Ip.parse("1.1.1.2")), "bgp", null);

    RouteRowAttribute rra1 = RouteRowAttribute.builder().setAdminDistance(11).build();
    RouteRowAttribute rra2 = RouteRowAttribute.builder().setAdminDistance(22).build();

    List<DiffRoutesOutput> diffRoutesOutputs =
        getRoutesDiff(
            ImmutableMap.of(routeRowKey1, ImmutableMap.of(rrsk1, ImmutableSortedSet.of(rra1))),
            ImmutableMap.of(routeRowKey2, ImmutableMap.of(rrsk2, ImmutableSortedSet.of(rra2))));

    assertThat(
        diffRoutesOutputs,
        containsInAnyOrder(
            new DiffRoutesOutput(
                routeRowKey1,
                rrsk1,
                KeyPresenceStatus.ONLY_IN_SNAPSHOT,
                ImmutableList.of(Lists.newArrayList(rra1, null)),
                KeyPresenceStatus.ONLY_IN_SNAPSHOT),
            new DiffRoutesOutput(
                routeRowKey2,
                rrsk2,
                KeyPresenceStatus.ONLY_IN_REFERENCE,
                ImmutableList.of(Lists.newArrayList(null, rra2)),
                KeyPresenceStatus.ONLY_IN_REFERENCE)));
  }

  @Test
  public void testAbstractRoutesRowDiff() {
    RouteRowAttribute.Builder routeRowAttrBuilder =
        RouteRowAttribute.builder().setAdminDistance(200).setMetric(2L);

    List<List<RouteRowAttribute>> diffMatrix = new ArrayList<>();
    diffMatrix.add(Lists.newArrayList(routeRowAttrBuilder.build(), routeRowAttrBuilder.build()));

    List<DiffRoutesOutput> diff =
        ImmutableList.of(
            new DiffRoutesOutput(
                new RouteRowKey("node", "vrf", Prefix.parse("1.1.1.1/24")),
                new MainRibRouteRowSecondaryKey(NextHopIp.of(Ip.parse("1.1.1.1")), "bgp"),
                KeyPresenceStatus.IN_BOTH,
                diffMatrix,
                KeyPresenceStatus.IN_BOTH));
    Multiset<Row> rows = getAbstractRouteRowsDiff(diff);

    assertThat(
        rows,
        containsInAnyOrder(
            ImmutableList.of(
                hasColumn(
                    COL_ROUTE_ENTRY_PRESENCE,
                    equalTo(RouteEntryPresenceStatus.UNCHANGED.routeEntryPresenceName()),
                    Schema.STRING))));
  }

  @Test
  public void testPopulateRouteRowAttributes() {
    RouteRowAttribute routeRowAttribute =
        RouteRowAttribute.builder()
            .setNextHopInterface("nhIface1")
            .setMetric(1L)
            .setAdminDistance(1)
            .setTag(1L)
            .build();
    Row.RowBuilder rowBuilder = Row.builder();

    populateRouteAttributes(rowBuilder, routeRowAttribute, true);

    assertThat(
        rowBuilder.build(),
        equalTo(
            Row.builder()
                .put(COL_BASE_PREFIX + COL_NEXT_HOP_INTERFACE, "nhIface1")
                .put(COL_BASE_PREFIX + COL_METRIC, 1L)
                .put(COL_BASE_PREFIX + COL_ADMIN_DISTANCE, 1)
                .put(COL_BASE_PREFIX + COL_TAG, 1L)
                .build()));
  }

  @Test
  public void testPrefixMatches() {
    Prefix p24 = Prefix.parse("1.1.1.0/24");
    Prefix p16 = Prefix.parse("1.1.0.0/16");
    Prefix p32 = Prefix.parse("1.1.1.1/32");

    assertTrue(prefixMatches(PrefixMatchType.EXACT, p24, Prefix.parse("1.1.1.0/24")));
    assertFalse(prefixMatches(PrefixMatchType.EXACT, p24, p16));

    assertTrue(prefixMatches(PrefixMatchType.LONGER_PREFIXES, p24, p24));
    assertTrue(prefixMatches(PrefixMatchType.LONGER_PREFIXES, p24, p32));
    assertFalse(prefixMatches(PrefixMatchType.LONGER_PREFIXES, p24, p16));
    assertFalse(prefixMatches(PrefixMatchType.LONGER_PREFIXES, p24, Prefix.parse("2.1.1.0/24")));

    assertTrue(prefixMatches(PrefixMatchType.SHORTER_PREFIXES, p24, p24));
    assertTrue(prefixMatches(PrefixMatchType.SHORTER_PREFIXES, p24, p16));
    assertFalse(prefixMatches(PrefixMatchType.SHORTER_PREFIXES, p24, p32));
    assertFalse(prefixMatches(PrefixMatchType.SHORTER_PREFIXES, p24, Prefix.parse("2.1.1.0/24")));
  }

  @Test
  public void testGetMatchingPrefixRoutes_fromRib() {
    AbstractRoute r1 = new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "r1");
    AbstractRoute r2 = new ConnectedRoute(Prefix.parse("1.2.1.0/24"), "r2");
    FinalMainRib rib = FinalMainRib.of(r1, r2);

    // both routes are returned when network is null
    assertThat(
        getMatchingPrefixRoutes(PrefixMatchType.EXACT, null, rib).collect(Collectors.toSet()),
        containsInAnyOrder(r1, r2));

    // match conditions other than LPM
    assertThat(
        getMatchingPrefixRoutes(PrefixMatchType.EXACT, Prefix.parse("1.1.1.1/24"), rib)
            .collect(Collectors.toSet()),
        contains(r1));
    assertThat(
        getMatchingPrefixRoutes(PrefixMatchType.LONGER_PREFIXES, Prefix.parse("1.0.0.0/8"), rib)
            .collect(Collectors.toSet()),
        containsInAnyOrder(r1, r2));

    // LPM
    assertThat(
        getMatchingPrefixRoutes(
                PrefixMatchType.LONGEST_PREFIX_MATCH, Prefix.parse("1.1.1.1/32"), rib)
            .collect(Collectors.toSet()),
        contains(r1));
    assertTrue(
        getMatchingPrefixRoutes(
                PrefixMatchType.LONGEST_PREFIX_MATCH, Prefix.parse("2.1.1.1/32"), rib)
            .collect(Collectors.toSet())
            .isEmpty());
  }

  private static Map<BgpRouteStatus, Set<AbstractRoute>> closeStream(
      Map<BgpRouteStatus, Stream<AbstractRoute>> routes) {
    return routes.entrySet().stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey, e -> e.getValue().collect(ImmutableSet.toImmutableSet())));
  }

  @Test
  public void testGetMatchingRoutes() {
    Prefix bestAndBackup = Prefix.parse("1.1.1.0/24");
    Prefix bestOnly = Prefix.parse("1.2.1.0/24");

    AbstractRoute r1 = new ConnectedRoute(bestAndBackup, "r1");
    AbstractRoute r2 = new ConnectedRoute(bestOnly, "r2");
    Set<AbstractRoute> bestRoutes = ImmutableSet.of(r1, r2);

    AbstractRoute r3 = new ConnectedRoute(bestAndBackup, "r3");
    Set<AbstractRoute> backupRoutes = ImmutableSet.of(r3);

    {
      // network is null
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  null,
                  ImmutableSet.of(BEST, BACKUP),
                  PrefixMatchType.EXACT)),
          equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r1, r2), BACKUP, ImmutableSet.of(r3))));

      // filtering by status type
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes, backupRoutes, null, ImmutableSet.of(BEST), PrefixMatchType.EXACT)),
          equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r1, r2))));
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes, backupRoutes, null, ImmutableSet.of(BACKUP), PrefixMatchType.EXACT)),
          equalTo(ImmutableMap.of(BACKUP, ImmutableSet.of(r3))));
    }

    {
      // network is in both best and backup routes
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestAndBackup,
                  ImmutableSet.of(BEST, BACKUP),
                  PrefixMatchType.EXACT)),
          equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r1), BACKUP, ImmutableSet.of(r3))));

      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestAndBackup,
                  ImmutableSet.of(BEST),
                  PrefixMatchType.EXACT)),
          equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r1))));

      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestAndBackup,
                  ImmutableSet.of(BACKUP),
                  PrefixMatchType.EXACT)),
          equalTo(ImmutableMap.of(BACKUP, ImmutableSet.of(r3))));
    }

    {
      // network is only in best routes
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestOnly,
                  ImmutableSet.of(BEST, BACKUP),
                  PrefixMatchType.EXACT)),
          equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r2), BACKUP, ImmutableSet.of())));

      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestOnly,
                  ImmutableSet.of(BEST),
                  PrefixMatchType.EXACT)),
          equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r2))));

      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestOnly,
                  ImmutableSet.of(BACKUP),
                  PrefixMatchType.EXACT)),
          equalTo(ImmutableMap.of(BACKUP, ImmutableSet.of())));
    }

    {
      // LPM case: both best and backup match
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestAndBackup,
                  ImmutableSet.of(BEST, BACKUP),
                  PrefixMatchType.LONGEST_PREFIX_MATCH)),
          equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r1), BACKUP, ImmutableSet.of(r3))));
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestAndBackup,
                  ImmutableSet.of(BEST),
                  PrefixMatchType.LONGEST_PREFIX_MATCH)),
          equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r1))));
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestAndBackup,
                  ImmutableSet.of(BACKUP),
                  PrefixMatchType.LONGEST_PREFIX_MATCH)),
          equalTo(ImmutableMap.of(BACKUP, ImmutableSet.of(r3))));
    }
    {
      // LPM case: prefix only in best
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestOnly,
                  ImmutableSet.of(BEST, BACKUP),
                  PrefixMatchType.LONGEST_PREFIX_MATCH)),
          equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r2), BACKUP, ImmutableSet.of())));
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestOnly,
                  ImmutableSet.of(BEST),
                  PrefixMatchType.LONGEST_PREFIX_MATCH)),
          equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r2))));
      assertThat(
          closeStream(
              getMatchingRoutes(
                  bestRoutes,
                  backupRoutes,
                  bestOnly,
                  ImmutableSet.of(BACKUP),
                  PrefixMatchType.LONGEST_PREFIX_MATCH)),
          equalTo(ImmutableMap.of(BACKUP, ImmutableSet.of())));
    }
    {
      {
        // multi-prefix matcher
        assertThat(
            closeStream(
                getMatchingRoutes(
                    bestRoutes,
                    backupRoutes,
                    Prefix.ZERO,
                    ImmutableSet.of(BEST, BACKUP),
                    PrefixMatchType.LONGER_PREFIXES)),
            equalTo(ImmutableMap.of(BEST, ImmutableSet.of(r1, r2), BACKUP, ImmutableSet.of(r3))));
      }
    }
  }

  @Test
  public void testLongestPrefixMatch() {
    AbstractRoute r1 = new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "r1");
    AbstractRoute r2 = new ConnectedRoute(Prefix.parse("1.2.1.0/24"), "r2");
    Set<AbstractRoute> routes = ImmutableSet.of(r1, r2);

    assertThat(
        longestMatchingPrefix(Prefix.parse("1.1.1.0/24"), routes),
        equalTo(Optional.of(r1.getNetwork())));
    assertThat(
        longestMatchingPrefix(Prefix.parse("1.1.1.1/32"), routes),
        equalTo(Optional.of(r1.getNetwork())));
    assertThat(
        longestMatchingPrefix(Prefix.parse("1.2.1.0/24"), routes),
        equalTo(Optional.of(r2.getNetwork())));

    assertThat(longestMatchingPrefix(Prefix.parse("1.1.1.0/8"), routes), equalTo(Optional.empty()));
    assertThat(
        longestMatchingPrefix(Prefix.parse("2.1.1.0/32"), routes), equalTo(Optional.empty()));
  }
}
