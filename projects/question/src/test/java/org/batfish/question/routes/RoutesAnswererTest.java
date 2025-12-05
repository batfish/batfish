package org.batfish.question.routes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Comparator.naturalOrder;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasWarnings;
import static org.batfish.datamodel.table.TableDiff.COL_BASE_PREFIX;
import static org.batfish.datamodel.table.TableDiff.COL_DELTA_PREFIX;
import static org.batfish.question.routes.RoutesAnswerer.BGP_COMPARATOR;
import static org.batfish.question.routes.RoutesAnswerer.COL_ADMIN_DISTANCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_AS_PATH;
import static org.batfish.question.routes.RoutesAnswerer.COL_CLUSTER_LIST;
import static org.batfish.question.routes.RoutesAnswerer.COL_COMMUNITIES;
import static org.batfish.question.routes.RoutesAnswerer.COL_LOCAL_PREF;
import static org.batfish.question.routes.RoutesAnswerer.COL_METRIC;
import static org.batfish.question.routes.RoutesAnswerer.COL_NETWORK;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP_INTERFACE;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP_IP;
import static org.batfish.question.routes.RoutesAnswerer.COL_NODE;
import static org.batfish.question.routes.RoutesAnswerer.COL_ORIGINATOR_ID;
import static org.batfish.question.routes.RoutesAnswerer.COL_ORIGIN_PROTOCOL;
import static org.batfish.question.routes.RoutesAnswerer.COL_ORIGIN_TYPE;
import static org.batfish.question.routes.RoutesAnswerer.COL_PATH_ID;
import static org.batfish.question.routes.RoutesAnswerer.COL_PROTOCOL;
import static org.batfish.question.routes.RoutesAnswerer.COL_RECEIVED_FROM_IP;
import static org.batfish.question.routes.RoutesAnswerer.COL_ROUTE_DISTINGUISHER;
import static org.batfish.question.routes.RoutesAnswerer.COL_ROUTE_ENTRY_PRESENCE;
import static org.batfish.question.routes.RoutesAnswerer.COL_STATUS;
import static org.batfish.question.routes.RoutesAnswerer.COL_TAG;
import static org.batfish.question.routes.RoutesAnswerer.COL_TUNNEL_ENCAPSULATION_ATTRIBUTE;
import static org.batfish.question.routes.RoutesAnswerer.COL_VRF_NAME;
import static org.batfish.question.routes.RoutesAnswerer.COL_WEIGHT;
import static org.batfish.question.routes.RoutesAnswerer.DIFF_COMPARATOR;
import static org.batfish.question.routes.RoutesAnswerer.EVPN_COMPARATOR;
import static org.batfish.question.routes.RoutesAnswerer.MAIN_RIB_COMPARATOR;
import static org.batfish.question.routes.RoutesAnswerer.getDiffTableMetadata;
import static org.batfish.question.routes.RoutesAnswerer.getTableMetadata;
import static org.batfish.question.routes.RoutesAnswererUtil.getMainRibRoutes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Multiset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.plugin.IBatfishTestAdapter.TopologyProviderTestAdapter;
import org.batfish.common.topology.GlobalBroadcastNoPointToPoint;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LocalRoute;
import org.batfish.datamodel.MockDataPlane;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.question.routes.RoutesAnswererUtil.RouteEntryPresenceStatus;
import org.batfish.question.routes.RoutesQuestion.PrefixMatchType;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.RoutingProtocolSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.junit.Test;

/** Tests of {@link RoutesAnswerer}. */
public class RoutesAnswererTest {
  private static IBatfish makeBatfish(Configuration... configs) {
    return new IBatfishTestAdapter() {
      @Override
      public Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot snapshot) {
        return ImmutableMap.of();
      }

      @Override
      public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
        return Arrays.stream(configs)
            .collect(
                ImmutableSortedMap.toImmutableSortedMap(
                    Comparator.naturalOrder(), Configuration::getHostname, Function.identity()));
      }

      @Override
      public DataPlane loadDataPlane(NetworkSnapshot snapshot) {
        return MockDataPlane.builder()
            .setRibs(
                Arrays.stream(configs)
                    .flatMap(
                        c ->
                            c.getVrfs().values().stream()
                                .map(
                                    vrf ->
                                        new AbstractMap.SimpleImmutableEntry<>(
                                            c.getHostname(), vrf)))
                    .collect(
                        ImmutableTable.toImmutableTable(
                            Entry::getKey,
                            e -> e.getValue().getName(),
                            e -> FinalMainRib.of(e.getValue().getStaticRoutes()))))
            .build();
      }
    };
  }

  @Test
  public void testGetMainRibRoutesWhenEmptyRib() {
    Multiset<Row> actual =
        getMainRibRoutes(
            ImmutableTable.of(),
            ImmutableMultimap.of("n1", DEFAULT_VRF_NAME),
            null,
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER,
            PrefixMatchType.EXACT);

    assertThat(actual.entrySet(), hasSize(0));
  }

  @Test
  public void testHasNetworkFiltering() {
    FinalMainRib rib =
        FinalMainRib.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.1.1.0/24"))
                .setNextHopInterface("Null")
                .setAdministrativeCost(1)
                .build(),
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("2.2.2.0/24"))
                .setNextHopInterface("Null")
                .setAdministrativeCost(1)
                .build());

    Multiset<Row> actual =
        getMainRibRoutes(
            ImmutableTable.of("n1", DEFAULT_VRF_NAME, rib),
            ImmutableMultimap.of("n1", DEFAULT_VRF_NAME),
            Prefix.create(Ip.parse("2.2.2.0"), 24),
            RoutingProtocolSpecifier.ALL_PROTOCOLS_SPECIFIER,
            PrefixMatchType.EXACT);

    assertThat(actual, hasSize(1));
    assertThat(
        actual.iterator().next().getPrefix(COL_NETWORK), equalTo(Prefix.parse("2.2.2.0/24")));
  }

  @Test
  public void testNoMatchingNodes() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration n1 = cb.setHostname("n1").build();
    nf.vrfBuilder().setOwner(n1).setName(DEFAULT_VRF_NAME).build();

    IBatfish batfish = makeBatfish(n1);

    NetworkSnapshot snapshot =
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot"));
    RoutesQuestion routesQuestion =
        new RoutesQuestion(null, "differentNode", null, null, null, null, null);
    TableAnswerElement answer =
        (TableAnswerElement) new RoutesAnswerer(routesQuestion, batfish).answer(snapshot);
    assertThat(
        answer,
        allOf(
            hasWarnings(contains(RoutesAnswerer.WARNING_NO_MATCHING_NODES)),
            hasRows(emptyIterable())));
  }

  @Test
  public void testNoMatchingVrfs() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration n1 = cb.setHostname("n1").build();
    nf.vrfBuilder().setOwner(n1).setName("v1").build();

    Configuration n2 = cb.setHostname("n2").build();
    nf.vrfBuilder().setOwner(n2).setName("v2").build();

    IBatfish batfish = makeBatfish(n1, n2);

    NetworkSnapshot snapshot =
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot"));
    RoutesQuestion routesQuestion = new RoutesQuestion(null, "n1", "v2", null, null, null, null);
    TableAnswerElement answer =
        (TableAnswerElement) new RoutesAnswerer(routesQuestion, batfish).answer(snapshot);
    assertThat(
        answer,
        allOf(
            hasWarnings(contains(RoutesAnswerer.WARNING_NO_MATCHING_VRFS)),
            hasRows(emptyIterable())));
  }

  @Test
  public void testHasProtocolFiltering() {
    FinalMainRib rib =
        FinalMainRib.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.1.1.0/24"))
                .setNextHopInterface("Null")
                .setAdministrativeCost(1)
                .build(),
            new LocalRoute(ConcreteInterfaceAddress.parse("2.2.2.1/24"), "Null"));

    Multiset<Row> actual =
        getMainRibRoutes(
            ImmutableTable.of("n1", DEFAULT_VRF_NAME, rib),
            ImmutableMultimap.of("n1", DEFAULT_VRF_NAME),
            null,
            new RoutingProtocolSpecifier("static"),
            PrefixMatchType.EXACT);

    assertThat(actual, hasSize(1));
    assertThat(
        actual.iterator().next().getPrefix(COL_NETWORK), equalTo(Prefix.parse("1.1.1.0/24")));
  }

  @Test
  public void testHasVrfFiltering() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("n1")
            .build();
    Vrf defaultVrf = nf.vrfBuilder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    defaultVrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setAdministrativeCost(1)
                .setNetwork(Prefix.parse("1.1.1.0/24"))
                .setNextHopInterface("Null")
                .build()));

    Vrf notDefaultVrf = nf.vrfBuilder().setOwner(c).setName("notDefaultVrf").build();
    notDefaultVrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("2.2.2.0/24"))
                .setNextHopInterface("Null")
                .setAdministrativeCost(1)
                .build()));

    IBatfish batfish = makeBatfish(c);

    NetworkSnapshot snapshot =
        new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot"));
    RoutesQuestion routesQuestion =
        new RoutesQuestion(null, null, "^not.*", null, null, null, null);
    TableAnswerElement answer =
        (TableAnswerElement) new RoutesAnswerer(routesQuestion, batfish).answer(snapshot);
    Multiset<Row> actual = answer.getRows().getData();

    assertThat(actual, hasSize(1));
    assertThat(
        actual.iterator().next().getPrefix(COL_NETWORK), equalTo(Prefix.parse("2.2.2.0/24")));
  }

  @Test
  public void testGetTableMetadataProtocolAll() {
    List<ColumnMetadata> columnMetadata = getTableMetadata(RibProtocol.MAIN).getColumnMetadata();

    assertThat(
        columnMetadata.stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(
            COL_NODE,
            COL_VRF_NAME,
            COL_NETWORK,
            COL_NEXT_HOP,
            COL_NEXT_HOP_IP,
            COL_NEXT_HOP_INTERFACE,
            COL_PROTOCOL,
            COL_METRIC,
            COL_ADMIN_DISTANCE,
            COL_TAG));

    assertThat(
        columnMetadata.stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(
            Schema.NODE,
            Schema.STRING,
            Schema.PREFIX,
            Schema.NEXT_HOP,
            Schema.IP,
            Schema.STRING,
            Schema.STRING,
            Schema.LONG,
            Schema.LONG,
            Schema.LONG));
  }

  @Test
  public void testGetTableMetadataBGP() {
    List<String> expected =
        ImmutableList.of(
            COL_NODE,
            COL_VRF_NAME,
            COL_NETWORK,
            COL_STATUS,
            COL_NEXT_HOP,
            COL_NEXT_HOP_IP,
            COL_NEXT_HOP_INTERFACE,
            COL_PROTOCOL,
            // BGP attributes
            COL_AS_PATH,
            COL_METRIC,
            COL_LOCAL_PREF,
            COL_COMMUNITIES,
            COL_ORIGIN_PROTOCOL,
            COL_ORIGIN_TYPE,
            COL_ORIGINATOR_ID,
            COL_RECEIVED_FROM_IP,
            COL_PATH_ID,
            COL_CLUSTER_LIST,
            COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
            COL_WEIGHT,
            COL_TAG);

    List<ColumnMetadata> columnMetadata = getTableMetadata(RibProtocol.BGP).getColumnMetadata();
    assertThat(
        columnMetadata.stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        equalTo(expected));
  }

  @Test
  public void testGetTableMetadataEvpn() {
    List<String> expected =
        ImmutableList.of(
            COL_NODE,
            COL_VRF_NAME,
            COL_NETWORK,
            COL_STATUS,
            COL_ROUTE_DISTINGUISHER,
            COL_NEXT_HOP,
            COL_NEXT_HOP_IP,
            COL_NEXT_HOP_INTERFACE,
            COL_PROTOCOL,
            // BGP attributes
            COL_AS_PATH,
            COL_METRIC,
            COL_LOCAL_PREF,
            COL_COMMUNITIES,
            COL_ORIGIN_PROTOCOL,
            COL_ORIGIN_TYPE,
            COL_ORIGINATOR_ID,
            COL_PATH_ID,
            COL_CLUSTER_LIST,
            COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
            COL_WEIGHT,
            COL_TAG);

    List<ColumnMetadata> columnMetadata = getTableMetadata(RibProtocol.EVPN).getColumnMetadata();
    assertThat(
        columnMetadata.stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        equalTo(expected));
  }

  @Test
  public void testGetDiffTableMetadataMainRib() {
    List<ColumnMetadata> columnMetadata =
        getDiffTableMetadata(RibProtocol.MAIN).getColumnMetadata();

    assertThat(
        columnMetadata.stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(
            COL_NODE,
            COL_VRF_NAME,
            COL_NETWORK,
            COL_ROUTE_ENTRY_PRESENCE,
            COL_BASE_PREFIX + COL_NEXT_HOP,
            COL_DELTA_PREFIX + COL_NEXT_HOP,
            COL_BASE_PREFIX + COL_NEXT_HOP_IP,
            COL_DELTA_PREFIX + COL_NEXT_HOP_IP,
            COL_BASE_PREFIX + COL_NEXT_HOP_INTERFACE,
            COL_DELTA_PREFIX + COL_NEXT_HOP_INTERFACE,
            COL_BASE_PREFIX + COL_PROTOCOL,
            COL_DELTA_PREFIX + COL_PROTOCOL,
            COL_BASE_PREFIX + COL_METRIC,
            COL_DELTA_PREFIX + COL_METRIC,
            COL_BASE_PREFIX + COL_ADMIN_DISTANCE,
            COL_DELTA_PREFIX + COL_ADMIN_DISTANCE,
            COL_BASE_PREFIX + COL_TAG,
            COL_DELTA_PREFIX + COL_TAG));

    assertThat(
        columnMetadata.stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(
            Schema.NODE,
            Schema.STRING,
            Schema.PREFIX,
            Schema.STRING,
            Schema.NEXT_HOP,
            Schema.NEXT_HOP,
            Schema.IP,
            Schema.IP,
            Schema.STRING,
            Schema.STRING,
            Schema.STRING,
            Schema.STRING,
            Schema.LONG,
            Schema.LONG,
            Schema.LONG,
            Schema.LONG,
            Schema.LONG,
            Schema.LONG));
  }

  @Test
  public void testGetDiffTableMetadataBGP() {
    ImmutableList.Builder<String> expectedBuilder = ImmutableList.builder();
    expectedBuilder.add(
        COL_NODE,
        COL_VRF_NAME,
        COL_NETWORK,
        COL_ROUTE_ENTRY_PRESENCE,
        COL_BASE_PREFIX + COL_STATUS,
        COL_DELTA_PREFIX + COL_STATUS,
        COL_BASE_PREFIX + COL_NEXT_HOP,
        COL_DELTA_PREFIX + COL_NEXT_HOP,
        COL_BASE_PREFIX + COL_NEXT_HOP_IP,
        COL_DELTA_PREFIX + COL_NEXT_HOP_IP,
        COL_BASE_PREFIX + COL_PROTOCOL,
        COL_DELTA_PREFIX + COL_PROTOCOL,
        COL_BASE_PREFIX + COL_AS_PATH,
        COL_DELTA_PREFIX + COL_AS_PATH,
        COL_BASE_PREFIX + COL_METRIC,
        COL_DELTA_PREFIX + COL_METRIC,
        COL_BASE_PREFIX + COL_LOCAL_PREF,
        COL_DELTA_PREFIX + COL_LOCAL_PREF,
        COL_BASE_PREFIX + COL_COMMUNITIES,
        COL_DELTA_PREFIX + COL_COMMUNITIES,
        COL_BASE_PREFIX + COL_ORIGIN_PROTOCOL,
        COL_DELTA_PREFIX + COL_ORIGIN_PROTOCOL,
        COL_BASE_PREFIX + COL_ORIGIN_TYPE,
        COL_DELTA_PREFIX + COL_ORIGIN_TYPE,
        COL_BASE_PREFIX + COL_ORIGINATOR_ID,
        COL_DELTA_PREFIX + COL_ORIGINATOR_ID,
        COL_BASE_PREFIX + COL_RECEIVED_FROM_IP,
        COL_DELTA_PREFIX + COL_RECEIVED_FROM_IP,
        COL_BASE_PREFIX + COL_PATH_ID,
        COL_DELTA_PREFIX + COL_PATH_ID,
        COL_BASE_PREFIX + COL_CLUSTER_LIST,
        COL_DELTA_PREFIX + COL_CLUSTER_LIST,
        COL_BASE_PREFIX + COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
        COL_DELTA_PREFIX + COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
        COL_BASE_PREFIX + COL_WEIGHT,
        COL_DELTA_PREFIX + COL_WEIGHT,
        COL_BASE_PREFIX + COL_TAG,
        COL_DELTA_PREFIX + COL_TAG);

    ImmutableList.Builder<Schema> schemaBuilderList = ImmutableList.builder();
    schemaBuilderList.add(
        Schema.NODE,
        Schema.STRING,
        Schema.PREFIX,
        Schema.STRING,
        Schema.list(Schema.STRING),
        Schema.list(Schema.STRING),
        Schema.NEXT_HOP,
        Schema.NEXT_HOP,
        Schema.IP,
        Schema.IP,
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.LONG,
        Schema.LONG,
        Schema.LONG,
        Schema.LONG,
        Schema.list(Schema.STRING),
        Schema.list(Schema.STRING),
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.IP,
        Schema.IP,
        Schema.INTEGER,
        Schema.INTEGER,
        Schema.set(Schema.LONG),
        Schema.set(Schema.LONG),
        Schema.STRING,
        Schema.STRING,
        Schema.INTEGER,
        Schema.INTEGER,
        Schema.LONG,
        Schema.LONG);

    List<ColumnMetadata> columnMetadata = getDiffTableMetadata(RibProtocol.BGP).getColumnMetadata();
    assertThat(
        columnMetadata.stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        equalTo(expectedBuilder.build()));

    assertThat(
        columnMetadata.stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        equalTo(schemaBuilderList.build()));
  }

  @Test
  public void testGetDiffTableMetadataEvpn() {
    ImmutableList.Builder<String> expectedBuilder = ImmutableList.builder();
    expectedBuilder.add(
        COL_NODE,
        COL_VRF_NAME,
        COL_NETWORK,
        COL_ROUTE_ENTRY_PRESENCE,
        COL_BASE_PREFIX + COL_STATUS,
        COL_DELTA_PREFIX + COL_STATUS,
        COL_BASE_PREFIX + COL_ROUTE_DISTINGUISHER,
        COL_DELTA_PREFIX + COL_ROUTE_DISTINGUISHER,
        COL_BASE_PREFIX + COL_NEXT_HOP,
        COL_DELTA_PREFIX + COL_NEXT_HOP,
        COL_BASE_PREFIX + COL_PROTOCOL,
        COL_DELTA_PREFIX + COL_PROTOCOL,
        COL_BASE_PREFIX + COL_AS_PATH,
        COL_DELTA_PREFIX + COL_AS_PATH,
        COL_BASE_PREFIX + COL_METRIC,
        COL_DELTA_PREFIX + COL_METRIC,
        COL_BASE_PREFIX + COL_LOCAL_PREF,
        COL_DELTA_PREFIX + COL_LOCAL_PREF,
        COL_BASE_PREFIX + COL_COMMUNITIES,
        COL_DELTA_PREFIX + COL_COMMUNITIES,
        COL_BASE_PREFIX + COL_ORIGIN_PROTOCOL,
        COL_DELTA_PREFIX + COL_ORIGIN_PROTOCOL,
        COL_BASE_PREFIX + COL_ORIGIN_TYPE,
        COL_DELTA_PREFIX + COL_ORIGIN_TYPE,
        COL_BASE_PREFIX + COL_ORIGINATOR_ID,
        COL_DELTA_PREFIX + COL_ORIGINATOR_ID,
        COL_BASE_PREFIX + COL_PATH_ID,
        COL_DELTA_PREFIX + COL_PATH_ID,
        COL_BASE_PREFIX + COL_CLUSTER_LIST,
        COL_DELTA_PREFIX + COL_CLUSTER_LIST,
        COL_BASE_PREFIX + COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
        COL_DELTA_PREFIX + COL_TUNNEL_ENCAPSULATION_ATTRIBUTE,
        COL_BASE_PREFIX + COL_WEIGHT,
        COL_DELTA_PREFIX + COL_WEIGHT,
        COL_BASE_PREFIX + COL_TAG,
        COL_DELTA_PREFIX + COL_TAG);

    ImmutableList.Builder<Schema> schemaBuilderList = ImmutableList.builder();
    schemaBuilderList.add(
        Schema.NODE,
        Schema.STRING,
        Schema.PREFIX,
        Schema.STRING,
        Schema.list(Schema.STRING),
        Schema.list(Schema.STRING),
        Schema.STRING,
        Schema.STRING,
        Schema.NEXT_HOP,
        Schema.NEXT_HOP,
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.LONG,
        Schema.LONG,
        Schema.LONG,
        Schema.LONG,
        Schema.list(Schema.STRING),
        Schema.list(Schema.STRING),
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.STRING,
        Schema.INTEGER,
        Schema.INTEGER,
        Schema.set(Schema.LONG),
        Schema.set(Schema.LONG),
        Schema.STRING,
        Schema.STRING,
        Schema.INTEGER,
        Schema.INTEGER,
        Schema.LONG,
        Schema.LONG);

    List<ColumnMetadata> columnMetadata =
        getDiffTableMetadata(RibProtocol.EVPN).getColumnMetadata();
    assertThat(
        columnMetadata.stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        equalTo(expectedBuilder.build()));

    assertThat(
        columnMetadata.stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        equalTo(schemaBuilderList.build()));
  }

  @Test
  public void testSameColumnsDiffAndNonDiff() {
    List<String> nonDiffColumns =
        getTableMetadata(RibProtocol.MAIN).getColumnMetadata().stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList());
    List<String> diffColumns =
        getDiffTableMetadata(RibProtocol.MAIN).getColumnMetadata().stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList());

    ImmutableList.Builder<String> expectedNonDiffColumns = ImmutableList.builder();
    ImmutableList.Builder<String> expectedDiffColumns = ImmutableList.builder();

    // Key columns are not diffed; should be identical in both tables.
    List<String> keyColumns = ImmutableList.of(COL_NODE, COL_VRF_NAME, COL_NETWORK);
    expectedNonDiffColumns.addAll(keyColumns);
    expectedDiffColumns.addAll(keyColumns);

    // Only differential results have a route entry presence column.
    expectedDiffColumns.add(COL_ROUTE_ENTRY_PRESENCE);

    // After key columns, all columns in the non-differential result should correspond to two
    // columns in the differential result (snapshot and reference).
    IntStream.range(keyColumns.size(), nonDiffColumns.size())
        .mapToObj(nonDiffColumns::get)
        .forEach(
            c -> {
              expectedNonDiffColumns.add(c);
              expectedDiffColumns.add(COL_BASE_PREFIX + c);
              expectedDiffColumns.add(COL_DELTA_PREFIX + c);
            });

    assertThat(nonDiffColumns, equalTo(expectedNonDiffColumns.build()));
    assertThat(diffColumns, equalTo(expectedDiffColumns.build()));
  }

  @Test
  public void testSameColumnsDiffAndNonDiffBgp() {
    List<String> nonDiffColumns =
        getTableMetadata(RibProtocol.BGP).getColumnMetadata().stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList());
    List<String> diffColumns =
        getDiffTableMetadata(RibProtocol.BGP).getColumnMetadata().stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList());

    ImmutableList.Builder<String> expectedNonDiffColumns = ImmutableList.builder();
    ImmutableList.Builder<String> expectedDiffColumns = ImmutableList.builder();

    // Key columns are not diffed; should be identical in both tables.
    List<String> keyColumns = ImmutableList.of(COL_NODE, COL_VRF_NAME, COL_NETWORK);
    expectedNonDiffColumns.addAll(keyColumns);
    expectedDiffColumns.addAll(keyColumns);

    // Only differential results have a route entry presence column.
    expectedDiffColumns.add(COL_ROUTE_ENTRY_PRESENCE);

    // After key columns, all columns in the non-differential result should correspond to two
    // columns in the differential result (snapshot and reference).
    // One exception: next hop interface is only present in non-differential for backwards
    // compatibility, and was never added to differential BGP routes.
    Set<String> nonDifferentialOnly = ImmutableSet.of(COL_NEXT_HOP_INTERFACE);
    IntStream.range(keyColumns.size(), nonDiffColumns.size())
        .mapToObj(nonDiffColumns::get)
        .forEach(
            c -> {
              expectedNonDiffColumns.add(c);
              if (!nonDifferentialOnly.contains(c)) {
                expectedDiffColumns.add(COL_BASE_PREFIX + c);
                expectedDiffColumns.add(COL_DELTA_PREFIX + c);
              }
            });

    assertThat(nonDiffColumns, equalTo(expectedNonDiffColumns.build()));
    assertThat(diffColumns, equalTo(expectedDiffColumns.build()));
  }

  @Test
  public void testSameColumnsDiffAndNonDiffEvpn() {
    List<String> nonDiffColumns =
        getTableMetadata(RibProtocol.EVPN).getColumnMetadata().stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList());
    List<String> diffColumns =
        getDiffTableMetadata(RibProtocol.EVPN).getColumnMetadata().stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList());

    ImmutableList.Builder<String> expectedNonDiffColumns = ImmutableList.builder();
    ImmutableList.Builder<String> expectedDiffColumns = ImmutableList.builder();

    // Key columns are not diffed; should be identical in both tables.
    List<String> keyColumns = ImmutableList.of(COL_NODE, COL_VRF_NAME, COL_NETWORK);
    expectedNonDiffColumns.addAll(keyColumns);
    expectedDiffColumns.addAll(keyColumns);

    // Only differential results have a route entry presence column.
    expectedDiffColumns.add(COL_ROUTE_ENTRY_PRESENCE);

    // After key columns, all columns in the non-differential result should correspond to two
    // columns in the differential result (snapshot and reference).
    // Exception: next hop IP and interface columns are only present in non-differential for
    // backwards compatibility, and were never added to differential EVPN routes.
    Set<String> nonDifferentialOnly = ImmutableSet.of(COL_NEXT_HOP_IP, COL_NEXT_HOP_INTERFACE);
    IntStream.range(keyColumns.size(), nonDiffColumns.size())
        .mapToObj(nonDiffColumns::get)
        .forEach(
            c -> {
              expectedNonDiffColumns.add(c);
              if (!nonDifferentialOnly.contains(c)) {
                expectedDiffColumns.add(COL_BASE_PREFIX + c);
                expectedDiffColumns.add(COL_DELTA_PREFIX + c);
              }
            });

    assertThat(nonDiffColumns, equalTo(expectedNonDiffColumns.build()));
    assertThat(diffColumns, equalTo(expectedDiffColumns.build()));
  }

  @Test
  public void testMainRibComparator() {
    List<Node> orderedNodes = ImmutableList.of(new Node("n1"), new Node("n2"));
    List<String> orderedVrfs = ImmutableList.of("vrf1", "vrf2");
    List<Prefix> orderedNetworks =
        ImmutableList.of(Prefix.strict("1.1.1.0/24"), Prefix.strict("2.2.2.0/24"));
    List<Ip> orderedIps = ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"));

    Row.RowBuilder rb = Row.builder(getTableMetadata(RibProtocol.MAIN).toColumnMap());
    List<Row> orderedRows = new ArrayList<>();
    for (Node n : orderedNodes) {
      rb.put(COL_NODE, n);
      for (String vrf : orderedVrfs) {
        rb.put(COL_VRF_NAME, vrf);
        for (Prefix p : orderedNetworks) {
          rb.put(COL_NETWORK, p);
          for (Ip nhip : orderedIps) {
            orderedRows.add(rb.put(COL_NEXT_HOP, NextHopIp.of(nhip)).build());
          }
        }
      }
    }
    for (int i = 0; i < orderedRows.size(); i++) {
      for (int j = 0; j < orderedRows.size(); j++) {
        assertThat(
            Integer.signum(MAIN_RIB_COMPARATOR.compare(orderedRows.get(i), (orderedRows.get(j)))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }

  @Test
  public void testBgpRibComparator() {
    List<Node> orderedNodes = ImmutableList.of(new Node("n1"), new Node("n2"));
    List<String> orderedVrfs = ImmutableList.of("vrf1", "vrf2");
    List<Prefix> orderedNetworks =
        ImmutableList.of(Prefix.strict("1.1.1.0/24"), Prefix.strict("2.2.2.0/24"));
    List<Ip> orderedIps = ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"));
    List<Integer> orderedPathIds = ImmutableList.of(1, 2);

    Row.RowBuilder rb = Row.builder(getTableMetadata(RibProtocol.BGP).toColumnMap());
    List<Row> orderedRows = new ArrayList<>();
    for (Node n : orderedNodes) {
      rb.put(COL_NODE, n);
      for (String vrf : orderedVrfs) {
        rb.put(COL_VRF_NAME, vrf);
        for (Prefix p : orderedNetworks) {
          rb.put(COL_NETWORK, p);
          for (Ip nhip : orderedIps) {
            rb.put(COL_NEXT_HOP, NextHopIp.of(nhip));
            for (Ip receivedFromIp : orderedIps) {
              rb.put(COL_RECEIVED_FROM_IP, receivedFromIp);
              for (int pathId : orderedPathIds) {
                orderedRows.add(rb.put(COL_PATH_ID, pathId).build());
              }
            }
          }
        }
      }
    }
    for (int i = 0; i < orderedRows.size(); i++) {
      for (int j = 0; j < orderedRows.size(); j++) {
        assertThat(
            Integer.signum(BGP_COMPARATOR.compare(orderedRows.get(i), (orderedRows.get(j)))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }

  @Test
  public void testEvpnRibComparator() {
    List<Node> orderedNodes = ImmutableList.of(new Node("n1"), new Node("n2"));
    List<String> orderedVrfs = ImmutableList.of("vrf1", "vrf2");
    List<Prefix> orderedNetworks =
        ImmutableList.of(Prefix.strict("1.1.1.0/24"), Prefix.strict("2.2.2.0/24"));
    List<Ip> orderedIps = ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"));
    List<Integer> orderedPathIds = ImmutableList.of(1, 2);

    Row.RowBuilder rb = Row.builder(getTableMetadata(RibProtocol.EVPN).toColumnMap());
    List<Row> orderedRows = new ArrayList<>();
    for (Node n : orderedNodes) {
      rb.put(COL_NODE, n);
      for (String vrf : orderedVrfs) {
        rb.put(COL_VRF_NAME, vrf);
        for (Prefix p : orderedNetworks) {
          rb.put(COL_NETWORK, p);
          for (Ip nhip : orderedIps) {
            rb.put(COL_NEXT_HOP, NextHopIp.of(nhip));
            for (int pathId : orderedPathIds) {
              orderedRows.add(rb.put(COL_PATH_ID, pathId).build());
            }
          }
        }
      }
    }
    for (int i = 0; i < orderedRows.size(); i++) {
      for (int j = 0; j < orderedRows.size(); j++) {
        assertThat(
            Integer.signum(EVPN_COMPARATOR.compare(orderedRows.get(i), (orderedRows.get(j)))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }

  @Test
  public void testDiffComparator() {
    List<Node> orderedNodes = ImmutableList.of(new Node("n1"), new Node("n2"));
    List<String> orderedVrfs = ImmutableList.of("vrf1", "vrf2");
    List<Prefix> orderedNetworks =
        ImmutableList.of(Prefix.strict("1.1.1.0/24"), Prefix.strict("2.2.2.0/24"));
    List<RouteEntryPresenceStatus> orderedStatuses =
        ImmutableList.of(
            RouteEntryPresenceStatus.CHANGED, RouteEntryPresenceStatus.ONLY_IN_SNAPSHOT);

    // which RIB currently does not matter here because the comparator only relies on columns that
    // all differential results include
    Row.RowBuilder rb = Row.builder(getDiffTableMetadata(RibProtocol.MAIN).toColumnMap());
    List<Row> orderedRows = new ArrayList<>();
    for (Node n : orderedNodes) {
      rb.put(COL_NODE, n);
      for (String vrf : orderedVrfs) {
        rb.put(COL_VRF_NAME, vrf);
        for (Prefix p : orderedNetworks) {
          rb.put(COL_NETWORK, p);
          for (RouteEntryPresenceStatus status : orderedStatuses) {
            orderedRows.add(rb.put(COL_ROUTE_ENTRY_PRESENCE, status).build());
          }
        }
      }
    }
    for (int i = 0; i < orderedRows.size(); i++) {
      for (int j = 0; j < orderedRows.size(); j++) {
        assertThat(
            Integer.signum(DIFF_COMPARATOR.compare(orderedRows.get(i), (orderedRows.get(j)))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }

  /** Run through full pipeline (create question and answerer), */
  @Test
  public void testFullAnswerPipelineAndNumResults() {
    // Setup mock data structures
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    FinalMainRib rib =
        FinalMainRib.of(
            StaticRoute.testBuilder()
                .setAdministrativeCost(1)
                .setNetwork(Prefix.parse("1.1.1.1/32"))
                .setNextHopInterface("Null")
                .build());
    NetworkConfigurations nc = NetworkConfigurations.of(ImmutableMap.of(c.getHostname(), c));

    MockBatfish batfish =
        new MockBatfish(
            nc,
            MockDataPlane.builder()
                .setRibs(ImmutableTable.of(c.getHostname(), vrf.getName(), rib))
                .build());
    AnswerElement el =
        new RoutesAnswerer(new RoutesQuestion(), batfish).answer(batfish.getSnapshot());

    assert el.getSummary() != null;
    assertThat(el.getSummary().getNumResults(), equalTo(1));

    // no results for empty ribs
    batfish = new MockBatfish(nc, MockDataPlane.builder().setRibs(ImmutableTable.of()).build());
    el = new RoutesAnswerer(new RoutesQuestion(), batfish).answer(batfish.getSnapshot());
    assert el.getSummary() != null;
    assertThat(el.getSummary().getNumResults(), equalTo(0));
  }

  @Test
  public void testHasTextDesc() {
    String textDesc = getTableMetadata(RibProtocol.MAIN).getTextDesc();

    assertThat(textDesc, notNullValue());
    assertThat(textDesc, not(emptyString()));
  }

  static class MockTopoloogyProvider extends TopologyProviderTestAdapter {

    public MockTopoloogyProvider(IBatfish batfish) {
      super(batfish);
    }

    @Override
    public L3Adjacencies getInitialL3Adjacencies(NetworkSnapshot snapshot) {
      return GlobalBroadcastNoPointToPoint.instance();
    }
  }

  static class MockBatfish extends IBatfishTestAdapter {

    private final NetworkConfigurations _configs;
    private final DataPlane _dp;

    public MockBatfish(NetworkConfigurations configs, DataPlane dp) {
      _configs = configs;
      _dp = dp;
    }

    @Override
    public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
      assertThat(snapshot, equalTo(getSnapshot()));
      return _configs.getMap().entrySet().stream()
          .collect(toImmutableSortedMap(naturalOrder(), Entry::getKey, Entry::getValue));
    }

    @Override
    public DataPlane loadDataPlane(NetworkSnapshot snapshot) {
      checkArgument(snapshot.equals(getSnapshot()));
      return _dp;
    }

    @Override
    public SpecifierContext specifierContext(NetworkSnapshot snapshot) {
      return MockSpecifierContext.builder().setConfigs(loadConfigurations(snapshot)).build();
    }

    @Override
    public TopologyProvider getTopologyProvider() {
      return new MockTopoloogyProvider(this);
    }
  }

  /** Mock rib that only supports one operation: returning pre-set routes. */
  static class MockRib<R extends AbstractRouteDecorator> implements GenericRib<R> {

    private Set<R> _routes;

    MockRib() {
      _routes = ImmutableSet.of();
    }

    MockRib(Set<R> routes) {
      _routes = routes;
    }

    @Override
    public int comparePreference(R lhs, R rhs) {
      return 0;
    }

    @Override
    public boolean intersectsPrefixSpace(PrefixSpace prefixSpace) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsRoute(AbstractRouteDecorator route) {
      return _routes.contains(route);
    }

    @Override
    public Set<AbstractRoute> getUnannotatedRoutes() {
      return _routes.stream()
          .map(AbstractRouteDecorator::getAbstractRoute)
          .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Set<R> getRoutes(Prefix prefix) {
      return _routes.stream()
          .filter(r -> r.getNetwork().equals(prefix))
          .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Set<R> getRoutes() {
      return _routes;
    }

    @Override
    public Set<R> getBackupRoutes() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<R> longestPrefixMatch(Ip address, ResolutionRestriction<R> restriction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<R> longestPrefixMatch(
        Ip address, int maxPrefixLength, ResolutionRestriction<R> restriction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean mergeRoute(R route) {
      throw new UnsupportedOperationException();
    }
  }
}
