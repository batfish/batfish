package org.batfish.question.edges;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.edges.EdgesAnswerer.COL_AS_NUMBER;
import static org.batfish.question.edges.EdgesAnswerer.COL_INTERFACE;
import static org.batfish.question.edges.EdgesAnswerer.COL_IP;
import static org.batfish.question.edges.EdgesAnswerer.COL_IPS;
import static org.batfish.question.edges.EdgesAnswerer.COL_NODE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_AS_NUMBER;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_INTERFACE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_IP;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_IPS;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_NODE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_VLAN;
import static org.batfish.question.edges.EdgesAnswerer.COL_VLAN;
import static org.batfish.question.edges.EdgesAnswerer.eigrpEdgeToRow;
import static org.batfish.question.edges.EdgesAnswerer.getBgpEdgeRow;
import static org.batfish.question.edges.EdgesAnswerer.getBgpEdges;
import static org.batfish.question.edges.EdgesAnswerer.getEigrpEdges;
import static org.batfish.question.edges.EdgesAnswerer.getIsisEdges;
import static org.batfish.question.edges.EdgesAnswerer.getLayer1Edges;
import static org.batfish.question.edges.EdgesAnswerer.getLayer2Edges;
import static org.batfish.question.edges.EdgesAnswerer.getLayer3Edges;
import static org.batfish.question.edges.EdgesAnswerer.getOspfEdgeRow;
import static org.batfish.question.edges.EdgesAnswerer.getOspfEdges;
import static org.batfish.question.edges.EdgesAnswerer.getRipEdgeRow;
import static org.batfish.question.edges.EdgesAnswerer.getRipEdges;
import static org.batfish.question.edges.EdgesAnswerer.getTableMetadata;
import static org.batfish.question.edges.EdgesAnswerer.isisEdgeToRow;
import static org.batfish.question.edges.EdgesAnswerer.layer1EdgeToRow;
import static org.batfish.question.edges.EdgesAnswerer.layer2EdgeToRow;
import static org.batfish.question.edges.EdgesAnswerer.layer3EdgeToRow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.NetworkBuilder;
import com.google.common.graph.ValueGraphBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Pair;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Node;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Edge;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.EdgeType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RipNeighbor;
import org.batfish.datamodel.RipProcess;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.eigrp.EigrpEdge;
import org.batfish.datamodel.eigrp.EigrpInterface;
import org.batfish.datamodel.isis.IsisEdge;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.isis.IsisNode;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link EdgesAnswerer} */
public class EdgesAnswererTest {
  private Configuration _host1;
  private Configuration _host2;
  private Topology _topology;
  private Map<String, Configuration> _configurations;
  private Set<String> _includeNodes;
  private Set<String> _includeRemoteNodes;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    _host1 = cb.setHostname("host1").build();
    _host1.setInterfaces(
        ImmutableSortedMap.of(
            "int1",
            Interface.builder()
                .setName("int1")
                .setAddress(new InterfaceAddress(new Ip("1.1.1.1"), 24))
                .build()));

    _host2 = cb.setHostname("host2").build();
    _host2.setInterfaces(
        ImmutableSortedMap.of(
            "int2",
            Interface.builder()
                .setName("int2")
                .setAddress(new InterfaceAddress(new Ip("2.2.2.2"), 24))
                .build()));

    _configurations = ImmutableSortedMap.of("host1", _host1, "host2", _host2);
    _includeNodes = ImmutableSortedSet.of("host1", "host2");
    _includeRemoteNodes = ImmutableSortedSet.of("host1", "host2");

    // Sending an  edge from host1 to host2 in layer 3
    _topology = new Topology(ImmutableSortedSet.of(Edge.of("host1", "int1", "host2", "int2")));
  }

  @Test
  public void testGetEigrpEdges() {
    MutableNetwork<EigrpInterface, EigrpEdge> eigrpTopology =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();

    EigrpInterface eigrpInterface1 = new EigrpInterface("host1", "int1", "vrf1");
    EigrpInterface eigrpInterface2 = new EigrpInterface("host2", "int2", "vrf2");

    eigrpTopology.addEdge(
        eigrpInterface1, eigrpInterface2, new EigrpEdge(eigrpInterface1, eigrpInterface2));

    Multiset<Row> rows = getEigrpEdges(_includeNodes, _includeRemoteNodes, eigrpTopology);

    assertThat(
        rows,
        contains(
            allOf(
                hasColumn(
                    COL_INTERFACE,
                    equalTo(new NodeInterfacePair("host1", "int1")),
                    Schema.INTERFACE),
                hasColumn(
                    COL_REMOTE_INTERFACE,
                    equalTo(new NodeInterfacePair("host2", "int2")),
                    Schema.INTERFACE))));
  }

  @Test
  public void testGetBgpEdges() {
    BgpProcess bgp1 = new BgpProcess();
    bgp1.setRouterId(new Ip("1.1.1.1"));
    BgpActivePeerConfig peer1 =
        BgpActivePeerConfig.builder().setLocalIp(new Ip("1.1.1.1")).setLocalAs(1L).build();
    bgp1.getActiveNeighbors().put(new Prefix(new Ip("2.2.2.2"), 24), peer1);
    BgpPeerConfigId neighborId1 =
        new BgpPeerConfigId("host1", "vrf1", new Prefix(new Ip("2.2.2.2"), 24), false);

    BgpProcess bgp2 = new BgpProcess();
    bgp2.setRouterId(new Ip("2.2.2.2"));
    BgpActivePeerConfig peer2 =
        BgpActivePeerConfig.builder().setLocalIp(new Ip("2.2.2.2")).setLocalAs(2L).build();
    bgp2.getActiveNeighbors().put(new Prefix(new Ip("1.1.1.1"), 24), peer2);
    BgpPeerConfigId neighborId2 =
        new BgpPeerConfigId("host2", "vrf2", new Prefix(new Ip("1.1.1.1"), 24), false);

    Vrf vrf1 = new Vrf("vrf1");
    vrf1.setBgpProcess(bgp1);
    Vrf vrf2 = new Vrf("vrf2");
    vrf2.setBgpProcess(bgp2);

    _host1.setVrfs(ImmutableSortedMap.of("vrf1", vrf1));
    _host2.setVrfs(ImmutableSortedMap.of("vrf2", vrf2));

    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    bgpTopology.putEdgeValue(neighborId1, neighborId2, BgpSessionProperties.from(peer1, peer2));

    Multiset<Row> rows =
        getBgpEdges(_configurations, _includeNodes, _includeRemoteNodes, bgpTopology);

    assertThat(
        rows,
        contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node("host1")), Schema.NODE),
                hasColumn(COL_IP, equalTo(new Ip("1.1.1.1")), Schema.IP),
                hasColumn(COL_AS_NUMBER, equalTo("1"), Schema.STRING),
                hasColumn(COL_REMOTE_NODE, equalTo(new Node("host2")), Schema.NODE),
                hasColumn(COL_REMOTE_IP, equalTo(new Ip("2.2.2.2")), Schema.IP),
                hasColumn(COL_REMOTE_AS_NUMBER, equalTo("2"), Schema.STRING))));
  }

  @Test
  public void testGetOspfEdges() {
    OspfProcess ospf1 = OspfProcess.builder().setReferenceBandwidth(1e8).build();
    OspfProcess ospf2 = OspfProcess.builder().setReferenceBandwidth(1e8).build();

    NetworkFactory nf = new NetworkFactory();
    OspfArea.builder(nf).setNumber(1L).setOspfProcess(ospf1).addInterface("int1").build();
    OspfArea.builder(nf).setNumber(1L).setOspfProcess(ospf2).addInterface("int2").build();

    Vrf vrf1 = new Vrf("vrf1");
    vrf1.setOspfProcess(ospf1);
    Vrf vrf2 = new Vrf("vrf2");
    vrf2.setOspfProcess(ospf2);

    _host1.setVrfs(ImmutableSortedMap.of("vrf1", vrf1));
    _host2.setVrfs(ImmutableSortedMap.of("vrf2", vrf2));
    _host1.getAllInterfaces().get("int1").setVrf(vrf1);
    _host2.getAllInterfaces().get("int2").setVrf(vrf2);

    Multiset<Row> rows =
        getOspfEdges(_configurations, _includeNodes, _includeRemoteNodes, _topology);
    assertThat(
        rows,
        contains(
            allOf(
                hasColumn(
                    COL_INTERFACE,
                    equalTo(new NodeInterfacePair("host1", "int1")),
                    Schema.INTERFACE),
                hasColumn(
                    COL_REMOTE_INTERFACE,
                    equalTo(new NodeInterfacePair("host2", "int2")),
                    Schema.INTERFACE))));
  }

  @Test
  public void testGetRipEdges() {
    RipProcess rip1 = new RipProcess();
    RipProcess rip2 = new RipProcess();
    RipNeighbor ripNeighbor1 = new RipNeighbor(new Pair<>(new Ip("1.1.1.1"), new Ip("2.2.2.2")));
    RipNeighbor ripNeighbor2 = new RipNeighbor(new Pair<>(new Ip("2.2.2.2"), new Ip("1.1.1.1")));
    rip1.setRipNeighbors(
        ImmutableSortedMap.of(new Pair<>(new Ip("1.1.1.1"), new Ip("2.2.2.2")), ripNeighbor1));
    rip2.setRipNeighbors(
        ImmutableSortedMap.of(new Pair<>(new Ip("2.2.2.2"), new Ip("1.1.1.1")), ripNeighbor2));
    ripNeighbor1.setOwner(_host1);
    ripNeighbor2.setOwner(_host2);
    ripNeighbor1.setIface(_host1.getAllInterfaces().get("int1"));
    ripNeighbor2.setIface(_host2.getAllInterfaces().get("int2"));
    ripNeighbor1.setRemoteRipNeighbor(ripNeighbor2);
    ripNeighbor2.setRemoteRipNeighbor(ripNeighbor1);

    Vrf vrf1 = new Vrf("vrf1");
    Vrf vrf2 = new Vrf("vrf2");

    vrf1.setRipProcess(rip1);
    vrf2.setRipProcess(rip2);

    _host1.setVrfs(ImmutableSortedMap.of("vrf1", vrf1));
    _host2.setVrfs(ImmutableSortedMap.of("vrf2", vrf2));

    Multiset<Row> rows = getRipEdges(_configurations, _includeNodes, _includeRemoteNodes);

    assertThat(
        rows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(
                    hasColumn(
                        COL_INTERFACE,
                        equalTo(new NodeInterfacePair("host1", "int1")),
                        Schema.INTERFACE),
                    hasColumn(
                        COL_REMOTE_INTERFACE,
                        equalTo(new NodeInterfacePair("host2", "int2")),
                        Schema.INTERFACE)),
                allOf(
                    hasColumn(
                        COL_INTERFACE,
                        equalTo(new NodeInterfacePair("host2", "int2")),
                        Schema.INTERFACE),
                    hasColumn(
                        COL_REMOTE_INTERFACE,
                        equalTo(new NodeInterfacePair("host1", "int1")),
                        Schema.INTERFACE)))));
  }

  @Test
  public void testGetIsisEdges() {
    IsisNode node1 = new IsisNode("host1", "int1");
    IsisNode node2 = new IsisNode("host2", "int2");

    IsisEdge edge = new IsisEdge(IsisLevel.LEVEL_1, node1, node2);

    MutableNetwork<IsisNode, IsisEdge> isisTopology =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    isisTopology.addEdge(node1, node2, edge);

    Multiset<Row> rows = getIsisEdges(_includeNodes, _includeRemoteNodes, isisTopology);
    assertThat(
        rows,
        contains(
            allOf(
                hasColumn(
                    COL_INTERFACE,
                    equalTo(new NodeInterfacePair("host1", "int1")),
                    Schema.INTERFACE),
                hasColumn(
                    COL_REMOTE_INTERFACE,
                    equalTo(new NodeInterfacePair("host2", "int2")),
                    Schema.INTERFACE))));
  }

  @Test
  public void testGetLayer1Edges() {
    Layer1Node layer1Node1 = new Layer1Node("host1", "int1");
    Layer1Node layer1Node2 = new Layer1Node("host2", "int2");

    Multiset<Row> rows =
        getLayer1Edges(
            _includeNodes,
            _includeRemoteNodes,
            new Layer1Topology(ImmutableSortedSet.of(new Layer1Edge(layer1Node1, layer1Node2))));
    assertThat(
        rows,
        contains(
            allOf(
                hasColumn(
                    COL_INTERFACE,
                    equalTo(new NodeInterfacePair("host1", "int1")),
                    Schema.INTERFACE),
                hasColumn(
                    COL_REMOTE_INTERFACE,
                    equalTo(new NodeInterfacePair("host2", "int2")),
                    Schema.INTERFACE))));
  }

  @Test
  public void testGetLayer2Edges() {
    Layer1Node layer1Node1 = new Layer1Node("host1", "int1");
    Layer1Node layer1Node2 = new Layer1Node("host2", "int2");

    Multiset<Row> rows =
        getLayer2Edges(
            _includeNodes,
            _includeRemoteNodes,
            new Layer2Topology(
                ImmutableSortedSet.of(new Layer2Edge(layer1Node1, 1, layer1Node2, 2, 12))));
    assertThat(
        rows,
        contains(
            allOf(
                hasColumn(
                    COL_INTERFACE,
                    equalTo(new NodeInterfacePair("host1", "int1")),
                    Schema.INTERFACE),
                hasColumn(COL_VLAN, equalTo("1"), Schema.STRING),
                hasColumn(
                    COL_REMOTE_INTERFACE,
                    equalTo(new NodeInterfacePair("host2", "int2")),
                    Schema.INTERFACE),
                hasColumn(COL_REMOTE_VLAN, equalTo("2"), Schema.STRING))));
  }

  @Test
  public void testGetLayer3Edges() {
    Topology layer3Topology =
        new Topology(ImmutableSortedSet.of(Edge.of("host1", "int1", "host2", "int2")));

    Multiset<Row> rows =
        getLayer3Edges(_configurations, _includeNodes, _includeRemoteNodes, layer3Topology);

    assertThat(
        rows,
        contains(
            allOf(
                hasColumn(
                    COL_INTERFACE,
                    equalTo(new NodeInterfacePair("host1", "int1")),
                    Schema.INTERFACE),
                hasColumn(
                    COL_IPS, equalTo(ImmutableSet.of(new Ip("1.1.1.1"))), Schema.set(Schema.IP)),
                hasColumn(
                    COL_REMOTE_INTERFACE,
                    equalTo(new NodeInterfacePair("host2", "int2")),
                    Schema.INTERFACE),
                hasColumn(
                    COL_REMOTE_IPS,
                    equalTo(ImmutableSet.of(new Ip("2.2.2.2"))),
                    Schema.set(Schema.IP)))));
  }

  @Test
  public void testEigrpToRow() {
    EigrpEdge testEdge =
        new EigrpEdge(
            new EigrpInterface("host1", "int1", "vrf1"),
            new EigrpInterface("host2", "int2", "vrf2"));
    Row row = eigrpEdgeToRow(testEdge);

    assertThat(
        row,
        allOf(
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE)));
  }

  @Test
  public void testOspfToRow() {
    Row row = getOspfEdgeRow("host1", "int1", "host2", "int2");

    assertThat(
        row,
        allOf(
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE)));
  }

  @Test
  public void testIsisToRow() {
    IsisEdge testEdge =
        new IsisEdge(
            IsisLevel.LEVEL_1, new IsisNode("host1", "int1"), new IsisNode("host2", "int2"));
    Row row = isisEdgeToRow(testEdge);

    assertThat(
        row,
        allOf(
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE)));
  }

  @Test
  public void testRipToRow() {
    Row row = getRipEdgeRow("host1", "int1", "host2", "int2");

    assertThat(
        row,
        allOf(
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE)));
  }

  @Test
  public void testBgpToRow() {
    Row row = getBgpEdgeRow("host1", new Ip("1.1.1.1"), 1L, "host2", new Ip("2.2.2.2"), 2L);
    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("host1")), Schema.NODE),
            hasColumn(COL_IP, equalTo(new Ip("1.1.1.1")), Schema.IP),
            hasColumn(COL_AS_NUMBER, equalTo("1"), Schema.STRING),
            hasColumn(COL_REMOTE_NODE, equalTo(new Node("host2")), Schema.NODE),
            hasColumn(COL_REMOTE_IP, equalTo(new Ip("2.2.2.2")), Schema.IP),
            hasColumn(COL_REMOTE_AS_NUMBER, equalTo("2"), Schema.STRING)));
  }

  @Test
  public void testLayer1ToRow() {
    Row row = layer1EdgeToRow(new Layer1Edge("host1", "int1", "host2", "int2"));
    assertThat(
        row,
        allOf(
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE)));
  }

  @Test
  public void testLayer2ToRow() {
    Row row = layer2EdgeToRow(new Layer2Edge("host1", "int1", 1, "host2", "int2", 2, 12));
    assertThat(
        row,
        allOf(
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(COL_VLAN, equalTo("1"), Schema.STRING),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE),
            hasColumn(COL_REMOTE_VLAN, equalTo("2"), Schema.STRING)));
  }

  @Test
  public void testLayer3ToRow() {
    Map<String, Configuration> configurationMap =
        ImmutableSortedMap.of("host1", _host1, "host2", _host2);
    Row row = layer3EdgeToRow(configurationMap, Edge.of("host1", "int1", "host2", "int2"));
    assertThat(
        row,
        allOf(
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(COL_IPS, equalTo(ImmutableSet.of(new Ip("1.1.1.1"))), Schema.set(Schema.IP)),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE),
            hasColumn(
                COL_REMOTE_IPS,
                equalTo(ImmutableSet.of(new Ip("2.2.2.2"))),
                Schema.set(Schema.IP))));
  }

  @Test
  public void testTableMetadataLayer3() {
    List<ColumnMetadata> columnMetadata = getTableMetadata(EdgeType.LAYER3).getColumnMetadata();
    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(COL_INTERFACE, COL_IPS, COL_REMOTE_INTERFACE, COL_REMOTE_IPS));

    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(Schema.INTERFACE, Schema.set(Schema.IP), Schema.INTERFACE, Schema.set(Schema.IP)));
  }

  @Test
  public void testTableMetadataLayer2() {
    List<ColumnMetadata> columnMetadata = getTableMetadata(EdgeType.LAYER2).getColumnMetadata();
    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(COL_INTERFACE, COL_VLAN, COL_REMOTE_INTERFACE, COL_REMOTE_VLAN));

    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(Schema.INTERFACE, Schema.STRING, Schema.INTERFACE, Schema.STRING));
  }

  @Test
  public void testTableMetadataBgp() {
    List<ColumnMetadata> columnMetadata = getTableMetadata(EdgeType.BGP).getColumnMetadata();
    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(
            COL_NODE, COL_IP, COL_AS_NUMBER, COL_REMOTE_NODE, COL_REMOTE_IP, COL_REMOTE_AS_NUMBER));

    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(Schema.NODE, Schema.IP, Schema.STRING, Schema.NODE, Schema.IP, Schema.STRING));
  }

  @Test
  public void testTableMetadataOthers() {
    List<ColumnMetadata> columnMetadata = getTableMetadata(EdgeType.OSPF).getColumnMetadata();
    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(COL_INTERFACE, COL_REMOTE_INTERFACE));

    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(Schema.INTERFACE, Schema.INTERFACE));
  }
}
