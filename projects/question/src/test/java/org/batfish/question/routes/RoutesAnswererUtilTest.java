package org.batfish.question.routes;

import static org.batfish.datamodel.Prefix.MAX_PREFIX_LENGTH;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.routes.RoutesAnswerer.COL_ADMIN_DISTANCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_AS_PATH;
import static org.batfish.question.routes.RoutesAnswerer.COL_COMMUNITIES;
import static org.batfish.question.routes.RoutesAnswerer.COL_METRIC;
import static org.batfish.question.routes.RoutesAnswerer.COL_NETWORK;
import static org.batfish.question.routes.RoutesAnswerer.COL_NETWORK_PRESENCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP_IP;
import static org.batfish.question.routes.RoutesAnswerer.COL_NODE;
import static org.batfish.question.routes.RoutesAnswerer.COL_ROUTE_ENTRY_PRESENCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_VRF_NAME;
import static org.batfish.question.routes.RoutesAnswererUtil.alignRouteRowAttributes;
import static org.batfish.question.routes.RoutesAnswererUtil.computeNextHopNode;
import static org.batfish.question.routes.RoutesAnswererUtil.getBgpRibRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.getBgpRouteRowsDiff;
import static org.batfish.question.routes.RoutesAnswererUtil.getMainRibRoutes;
import static org.batfish.question.routes.RoutesAnswererUtil.groupMatchingBgpRoutesByPrefix;
import static org.batfish.question.routes.RoutesAnswererUtil.groupMatchingRoutesByPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpRoute.Builder;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.OspfExternalType2Route;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.batfish.question.routes.DiffRoutesOutput.PresenceStatus;
import org.batfish.question.routes.RoutesAnswererTest.MockRib;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.junit.Test;

/** Tests for {@link RoutesAnswererUtil} */
public class RoutesAnswererUtilTest {

  @Test
  public void testAlignRtRowAttrsNHIP() {
    RouteRowAttribute rra1 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.1")).build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.3")).build();
    RouteRowAttribute rra5 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.5")).build();

    RouteRowAttribute rra2 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.2")).build();
    RouteRowAttribute rra4 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.4")).build();

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
  public void testAlignRtRowAttrsAutoNHIP() {
    RouteRowAttribute rraConnected =
        RouteRowAttribute.builder().setNextHopIp(Ip.AUTO).setProtocol("connected").build();
    RouteRowAttribute rraAggregate =
        RouteRowAttribute.builder().setNextHopIp(Ip.AUTO).setProtocol("aggregate").build();

    List<List<RouteRowAttribute>> alignedRouteRowattrs =
        alignRouteRowAttributes(ImmutableList.of(rraConnected), ImmutableList.of(rraAggregate));

    // merging in sorted order of protocols
    List<List<RouteRowAttribute>> expectedOutput =
        ImmutableList.of(
            Lists.newArrayList(null, rraAggregate), Lists.newArrayList(rraConnected, null));

    assertThat(alignedRouteRowattrs, equalTo(expectedOutput));
  }

  @Test
  public void testAlignRtRowAttrsTrailingNulls1() {
    RouteRowAttribute rra1 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.1")).build();
    RouteRowAttribute rra2 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.2")).build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.3")).build();

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
    RouteRowAttribute rra1 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.1")).build();
    RouteRowAttribute rra2 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.2")).build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.3")).build();

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
    RouteRowAttribute rra1 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.1")).build();
    RouteRowAttribute rra2 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.2")).build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.3")).build();

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
  public void testAlignRtRowEqualNHAndProto() {
    RouteRowAttribute rraConnected1 =
        RouteRowAttribute.builder()
            .setNextHopIp(Ip.AUTO)
            .setProtocol("connected")
            .setMetric(1L)
            .build();
    RouteRowAttribute rraConnected2 =
        RouteRowAttribute.builder()
            .setNextHopIp(Ip.AUTO)
            .setProtocol("connected")
            .setMetric(2L)
            .build();

    List<List<RouteRowAttribute>> alignedRouteRowattrs =
        alignRouteRowAttributes(ImmutableList.of(rraConnected1), ImmutableList.of(rraConnected2));

    // different metric will affect the pairing
    List<List<RouteRowAttribute>> expectedOutput =
        ImmutableList.of(
            Lists.newArrayList(rraConnected1, null), Lists.newArrayList(null, rraConnected2));

    assertThat(alignedRouteRowattrs, equalTo(expectedOutput));
  }

  @Test
  public void testMainRibColumnsValue() {
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs =
        ImmutableSortedMap.of(
            "n1",
            ImmutableSortedMap.of(
                Configuration.DEFAULT_VRF_NAME,
                new MockRib<>(
                    ImmutableSet.of(
                        OspfExternalType2Route.builder()
                            .setNetwork(Prefix.parse("1.1.1.0/24"))
                            .setNextHopIp(new Ip("1.1.1.2"))
                            .setAdmin(10)
                            .setMetric(30L)
                            .setLsaMetric(2)
                            .setCostToAdvertiser(2)
                            .setArea(1L)
                            .setAdvertiser("n2")
                            .setOspfMetricType(OspfMetricType.E2)
                            .build()))));

    Multiset<Row> actual = getMainRibRoutes(ribs, ImmutableSet.of("n1"), null, ".*", ".*", null);

    assertThat(actual, hasSize(1));
    Row row = actual.iterator().next();
    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("n1")), Schema.NODE),
            hasColumn(COL_VRF_NAME, equalTo(Configuration.DEFAULT_VRF_NAME), Schema.STRING),
            hasColumn(COL_NETWORK, equalTo(Prefix.parse("1.1.1.0/24")), Schema.PREFIX),
            hasColumn(COL_NEXT_HOP_IP, equalTo(new Ip("1.1.1.2")), Schema.IP),
            hasColumn(COL_ADMIN_DISTANCE, equalTo(10), Schema.INTEGER),
            hasColumn(COL_METRIC, equalTo(30L), Schema.LONG)));
  }

  @Test
  public void testBgpRibRouteColumnsValue() {
    Ip ip = new Ip("1.1.1.1");
    Table<String, String, Set<BgpRoute>> bgpRouteTable = HashBasedTable.create();
    bgpRouteTable.put(
        "node",
        "vrf",
        ImmutableSet.of(
            new Builder()
                .setNetwork(new Prefix(ip, MAX_PREFIX_LENGTH))
                .setOriginType(OriginType.IGP)
                .setNextHopIp(ip)
                .setCommunities(ImmutableSortedSet.of(65537L))
                .setProtocol(RoutingProtocol.BGP)
                .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
                .setTag(1)
                .build()));
    Multiset<Row> rows =
        getBgpRibRoutes(bgpRouteTable, RibProtocol.BGP, ImmutableSet.of("node"), null, ".*", ".*");

    assertThat(rows, hasSize(1));

    Row row = rows.iterator().next();

    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("node")), Schema.NODE),
            hasColumn(COL_VRF_NAME, equalTo("vrf"), Schema.STRING),
            hasColumn(COL_NETWORK, equalTo(new Prefix(ip, MAX_PREFIX_LENGTH)), Schema.PREFIX),
            hasColumn(COL_NEXT_HOP_IP, equalTo(ip), Schema.IP)));

    assertThat(
        row,
        allOf(
            hasColumn(
                COL_COMMUNITIES, equalTo(ImmutableList.of("1:1")), Schema.list(Schema.STRING)),
            hasColumn(COL_AS_PATH, equalTo("1 2"), Schema.STRING)));
  }

  @Test
  public void testGetBgpRoutesCommunities() {
    Ip ip = new Ip("1.1.1.1");
    Table<String, String, Set<BgpRoute>> bgpRouteTable = HashBasedTable.create();
    bgpRouteTable.put(
        "node",
        "vrf",
        ImmutableSet.of(
            new Builder()
                .setNetwork(new Prefix(ip, MAX_PREFIX_LENGTH))
                .setOriginType(OriginType.IGP)
                .setOriginatorIp(ip)
                .setReceivedFromIp(ip)
                .setCommunities(ImmutableSortedSet.of(65537L))
                .setProtocol(RoutingProtocol.BGP)
                .build()));
    Multiset<Row> rows =
        getBgpRibRoutes(bgpRouteTable, RibProtocol.BGP, ImmutableSet.of("node"), null, ".*", ".*");
    assertThat(
        rows.iterator().next().get(COL_COMMUNITIES, Schema.list(Schema.STRING)),
        equalTo(ImmutableList.of("1:1")));
  }

  @Test
  public void testComputeNextHopNode() {
    assertThat(computeNextHopNode(null, ImmutableMap.of()), nullValue());
    assertThat(computeNextHopNode(new Ip("1.1.1.1"), null), nullValue());
    assertThat(computeNextHopNode(new Ip("1.1.1.1"), ImmutableMap.of()), nullValue());
    assertThat(
        computeNextHopNode(
            new Ip("1.1.1.1"), ImmutableMap.of(new Ip("1.1.1.1"), ImmutableSet.of("n1"))),
        equalTo("n1"));
    assertThat(
        computeNextHopNode(
            new Ip("1.1.1.1"), ImmutableMap.of(new Ip("1.1.1.2"), ImmutableSet.of("n1"))),
        nullValue());
  }

  @Test
  public void testBgpRoutesRowDiff() {
    RouteRowAttribute.Builder routeRowAttrBuilder =
        RouteRowAttribute.builder()
            .setNextHopIp(new Ip("1.1.1.1"))
            .setProtocol("bgp")
            .setMetric(2L)
            .setOriginProtocol("bgp")
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .setLocalPreference(1L);
    List<List<RouteRowAttribute>> diffMatrix = new ArrayList<>();
    diffMatrix.add(Lists.newArrayList(routeRowAttrBuilder.build(), routeRowAttrBuilder.build()));
    diffMatrix.add(
        Lists.newArrayList(routeRowAttrBuilder.setNextHopIp(new Ip("1.1.1.2")).build(), null));

    List<DiffRoutesOutput> diff =
        ImmutableList.of(
            new DiffRoutesOutput(
                new RouteRowKey("node", "vrf", Prefix.parse("1.1.1.1/24")),
                diffMatrix,
                PresenceStatus.IN_BOTH));
    Multiset<Row> rows = getBgpRouteRowsDiff(diff, RibProtocol.BGP);

    assertThat(
        rows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(
                    hasColumn(
                        COL_NETWORK_PRESENCE,
                        equalTo(PresenceStatus.IN_BOTH.presenceStatusName()),
                        Schema.STRING),
                    hasColumn(
                        COL_ROUTE_ENTRY_PRESENCE,
                        equalTo(PresenceStatus.IN_BOTH.presenceStatusName()),
                        Schema.STRING)),
                allOf(
                    hasColumn(
                        COL_NETWORK_PRESENCE,
                        equalTo(PresenceStatus.IN_BOTH.presenceStatusName()),
                        Schema.STRING),
                    hasColumn(
                        COL_ROUTE_ENTRY_PRESENCE,
                        equalTo(PresenceStatus.ONLY_IN_SNAPSHOT.presenceStatusName()),
                        Schema.STRING)))));
  }

  @Test
  public void testGroupMatchingRoutesByPrefix() {
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs =
        ImmutableSortedMap.of(
            "n1",
            ImmutableSortedMap.of(
                Configuration.DEFAULT_VRF_NAME,
                new MockRib<>(
                    ImmutableSet.of(
                        OspfExternalType2Route.builder()
                            .setNetwork(Prefix.parse("1.1.1.0/24"))
                            .setNextHopIp(new Ip("1.1.1.2"))
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
                            .setNextHopIp(new Ip("1.1.1.3"))
                            .setAdmin(10)
                            .setMetric(30L)
                            .setLsaMetric(2)
                            .setCostToAdvertiser(2)
                            .setArea(1L)
                            .setAdvertiser("n2")
                            .setOspfMetricType(OspfMetricType.E2)
                            .build()))));

    Map<RouteRowKey, SortedSet<RouteRowAttribute>> grouped =
        groupMatchingRoutesByPrefix(ribs, ImmutableSet.of("n1"), null, ".*", ".*", null);

    assertThat(grouped.keySet(), hasSize(1));
    RouteRowKey expectedKey =
        new RouteRowKey("n1", Configuration.DEFAULT_VRF_NAME, Prefix.parse("1.1.1.0/24"));
    assertThat(grouped.keySet().iterator().next(), equalTo(expectedKey));
    assertThat(grouped.get(expectedKey), hasSize(2));
  }

  @Test
  public void testGroupMatchingBgpRoutesByPrefix() {
    BgpRoute bgpRoute1 =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(new Ip("1.1.1.2"))
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setAdmin(10)
            .setMetric(30L)
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .build();

    BgpRoute bgpRoute2 =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(new Ip("1.1.1.3"))
            .setAdmin(10)
            .setMetric(30L)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 2L)))
            .build();

    Table<String, String, Set<BgpRoute>> bgpTable = HashBasedTable.create();
    bgpTable.row("node").put(Configuration.DEFAULT_VRF_NAME, new HashSet<>());
    bgpTable.row("node").get(Configuration.DEFAULT_VRF_NAME).add(bgpRoute1);
    bgpTable.row("node").get(Configuration.DEFAULT_VRF_NAME).add(bgpRoute2);

    Map<RouteRowKey, SortedSet<RouteRowAttribute>> grouped =
        groupMatchingBgpRoutesByPrefix(bgpTable, ImmutableSet.of("node"), ".*", null, ".*");

    assertThat(grouped.keySet(), hasSize(1));
    RouteRowKey expectedKey =
        new RouteRowKey("node", Configuration.DEFAULT_VRF_NAME, Prefix.parse("1.1.1.0/24"));
    assertThat(grouped.keySet().iterator().next(), equalTo(expectedKey));

    assertThat(grouped.get(expectedKey), hasSize(2));
  }
}
