package org.batfish.question.bgpsessionstatus;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.bgp.BgpTopologyUtils.initBgpTopology;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.COL_REMOTE_INTERFACE;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.COL_ESTABLISHED_STATUS;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.COL_LOCAL_AS;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.COL_LOCAL_INTERFACE;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.COL_LOCAL_IP;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.COL_NODE;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.COL_REMOTE_AS;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.COL_REMOTE_IP;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.COL_REMOTE_NODE;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.COL_SESSION_TYPE;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.COL_VRF;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.createMetadata;
import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.getRows;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.topology.Layer2Edge;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.SessionStatus;
import org.hamcrest.Matcher;
import org.junit.Test;

/** Tests of {@link BgpSessionStatusAnswerer} */
public class BgpSessionStatusAnswererTest {

  /*
  Setup for all tests:
  node1 peers with node2.
  node1 has an active BGP peer from 1.1.1.1 to 2.2.2.2 (AS 1 to AS 2).
  node2 has an active BGP peer from 2.2.2.2 to 1.1.1.1 (AS 2 to AS 1).
  node3 has an active BGP peer from 3.3.3.3 to 3.3.3.4 (AS 3 to AS 3).
  node4 has a dynamic BGP peer from 3.3.3.4 to 3.3.3.0/30 (AS 3 to AS 3).
  This results in four rows, represented by ROW_1, ROW_2, ROW_3, and ROW_4.
   */

  private final Map<String, Configuration> _configurations;
  private final ValueGraph<BgpPeerConfigId, BgpSessionProperties> _topology;

  private static final Ip IP1 = Ip.parse("1.1.1.1");
  private static final Ip IP2 = Ip.parse("2.2.2.2");
  private static final Ip IP3 = Ip.parse("3.3.3.3");
  private static final Ip IP4 = Ip.parse("3.3.3.4");
  private static final Ip UNNUM_IP = Ip.parse("169.254.0.1");

  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final String NODE3 = "node3";
  private static final String NODE4 = "node4";
  private static final Set<String> ALL_NODES = ImmutableSet.of(NODE1, NODE2, NODE3, NODE4);

  private static final Map<String, ColumnMetadata> METADATA_MAP =
      createMetadata(new BgpSessionStatusQuestion()).toColumnMap();

  private static final Row ROW_1 =
      Row.builder()
          .put(COL_ESTABLISHED_STATUS, SessionStatus.ESTABLISHED)
          .put(COL_LOCAL_INTERFACE, null)
          .put(COL_LOCAL_IP, IP1)
          .put(COL_LOCAL_AS, 1L)
          .put(COL_NODE, new Node(NODE1))
          .put(COL_REMOTE_AS, LongSpace.of(2L).toString())
          .put(COL_REMOTE_NODE, new Node(NODE2))
          .put(COL_REMOTE_INTERFACE, null)
          .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, IP2))
          .put(COL_SESSION_TYPE, SessionType.EBGP_SINGLEHOP)
          .put(COL_VRF, "vrf")
          .build();

  private static final Row ROW_2 =
      Row.builder()
          .put(COL_ESTABLISHED_STATUS, SessionStatus.ESTABLISHED)
          .put(COL_LOCAL_INTERFACE, null)
          .put(COL_LOCAL_IP, IP2)
          .put(COL_LOCAL_AS, 2L)
          .put(COL_NODE, new Node(NODE2))
          .put(COL_REMOTE_AS, LongSpace.of(1L).toString())
          .put(COL_REMOTE_NODE, new Node(NODE1))
          .put(COL_REMOTE_INTERFACE, null)
          .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, IP1))
          .put(COL_SESSION_TYPE, SessionType.EBGP_SINGLEHOP)
          .put(COL_VRF, "vrf")
          .build();

  private static final Row ROW_3 =
      Row.builder()
          .put(COL_ESTABLISHED_STATUS, SessionStatus.ESTABLISHED)
          .put(COL_LOCAL_INTERFACE, null)
          .put(COL_LOCAL_IP, IP3)
          .put(COL_LOCAL_AS, 3L)
          .put(COL_NODE, new Node(NODE3))
          .put(COL_REMOTE_AS, LongSpace.of(3L).toString())
          .put(COL_REMOTE_NODE, new Node(NODE4))
          .put(COL_REMOTE_INTERFACE, null)
          .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, IP4))
          .put(COL_SESSION_TYPE, SessionType.IBGP)
          .put(COL_VRF, "vrf")
          .build();

  private static final Row ROW_4 =
      Row.builder()
          .put(COL_ESTABLISHED_STATUS, SessionStatus.ESTABLISHED)
          .put(COL_LOCAL_INTERFACE, null)
          .put(COL_LOCAL_IP, IP4)
          .put(COL_LOCAL_AS, 3L)
          .put(COL_NODE, new Node(NODE4))
          .put(COL_REMOTE_AS, LongSpace.of(3L).toString())
          .put(COL_REMOTE_NODE, new Node(NODE3))
          .put(COL_REMOTE_INTERFACE, null)
          .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, IP3))
          .put(COL_SESSION_TYPE, SessionType.IBGP)
          .put(COL_VRF, "vrf")
          .build();

  public BgpSessionStatusAnswererTest() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration node1 = createConfiguration(cb, NODE1, IP1, IP2, 1L, 2L);
    Configuration node2 = createConfiguration(cb, NODE2, IP2, IP1, 2L, 1L);
    Configuration node3 = createConfiguration(cb, NODE3, IP3, IP4, 3L, 3L);
    Configuration node4 =
        createConfigurationWithDynamicSession(
            cb, IP4, Prefix.create(IP3, 24), ImmutableList.of(3L));

    _configurations = ImmutableSortedMap.of(NODE1, node1, NODE2, node2, NODE3, node3, NODE4, node4);

    Map<Ip, Set<String>> ipOwners =
        ImmutableMap.of(
            IP1,
            ImmutableSet.of(NODE1),
            IP2,
            ImmutableSet.of(NODE2),
            IP3,
            ImmutableSet.of(NODE3),
            IP4,
            ImmutableSet.of(NODE4));
    _topology = initBgpTopology(_configurations, ipOwners, false, null).getGraph();
  }

  private static Configuration createConfiguration(
      Configuration.Builder cb,
      String nodeName,
      Ip localIp,
      Ip remoteIp,
      Long localAs,
      Long remoteAs) {

    Configuration node = cb.setHostname(nodeName).build();
    Interface iface = new Interface("iface", node, InterfaceType.PHYSICAL);
    iface.setAllAddresses(
        ImmutableList.of(new InterfaceAddress(localIp, Ip.numSubnetBitsToSubnetMask(32))));

    BgpActivePeerConfig peerConfig =
        BgpActivePeerConfig.builder()
            .setLocalAs(localAs)
            .setRemoteAs(remoteAs)
            .setLocalIp(localIp)
            .setPeerAddress(remoteIp)
            .build();

    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.setNeighbors(ImmutableSortedMap.of(Prefix.create(remoteIp, 32), peerConfig));

    Vrf vrf1 = new Vrf("vrf");
    vrf1.setBgpProcess(bgpProcess);

    node.setVrfs(ImmutableMap.of("vrf", vrf1));
    node.setInterfaces(ImmutableSortedMap.of("iface", iface));
    return node;
  }

  private static Configuration createConfigurationWithDynamicSession(
      Configuration.Builder cb, Ip localIp, Prefix remotePrefix, List<Long> remoteAsList) {

    Configuration node = cb.setHostname(NODE4).build();
    Interface iface = new Interface("iface", node, InterfaceType.PHYSICAL);
    iface.setAllAddresses(
        ImmutableList.of(new InterfaceAddress(localIp, Ip.numSubnetBitsToSubnetMask(32))));

    BgpPassivePeerConfig peerConfig =
        BgpPassivePeerConfig.builder()
            .setLocalAs(3L)
            .setRemoteAsns(LongSpace.builder().includingAll(remoteAsList).build())
            .setLocalIp(localIp)
            .setPeerPrefix(remotePrefix)
            .build();

    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.setPassiveNeighbors(ImmutableSortedMap.of(remotePrefix, peerConfig));

    Vrf vrf1 = new Vrf("vrf");
    vrf1.setBgpProcess(bgpProcess);

    node.setVrfs(ImmutableMap.of("vrf", vrf1));
    node.setInterfaces(ImmutableSortedMap.of("iface", iface));
    return node;
  }

  @Test
  public void testAnswer() {
    BgpSessionStatusQuestion q = new BgpSessionStatusQuestion();
    List<Row> rows =
        getRows(
            q,
            _configurations,
            ALL_NODES,
            ALL_NODES,
            METADATA_MAP,
            ImmutableSet.of(),
            _topology,
            _topology);
    assertThat(rows, contains(ROW_1, ROW_2, ROW_3, ROW_4));
  }

  @Test
  public void testLimitNodes() {
    BgpSessionStatusQuestion q = new BgpSessionStatusQuestion(NODE1, null, null, null);
    List<Row> rows =
        getRows(
            q,
            _configurations,
            ImmutableSet.of(NODE1),
            ALL_NODES,
            METADATA_MAP,
            ImmutableSet.of(),
            _topology,
            _topology);
    assertThat(rows, contains(ROW_1));
  }

  @Test
  public void testLimitRemoteNodes() {
    BgpSessionStatusQuestion q = new BgpSessionStatusQuestion(null, NODE1, null, null);
    List<Row> rows =
        getRows(
            q,
            _configurations,
            ALL_NODES,
            ImmutableSet.of(NODE1),
            METADATA_MAP,
            ImmutableSet.of(),
            _topology,
            _topology);
    assertThat(rows, contains(ROW_2));
  }

  @Test
  public void testLimitStatus() {
    // Both sessions have status UNIQUE_MATCH
    BgpSessionStatusQuestion q =
        new BgpSessionStatusQuestion(null, null, SessionStatus.ESTABLISHED.name(), null);
    List<Row> rows =
        getRows(
            q,
            _configurations,
            ALL_NODES,
            ALL_NODES,
            METADATA_MAP,
            ImmutableSet.of(),
            _topology,
            _topology);
    assertThat(rows, contains(ROW_1, ROW_2, ROW_3, ROW_4));

    q = new BgpSessionStatusQuestion(null, null, SessionStatus.NOT_COMPATIBLE.name(), null);
    rows =
        getRows(
            q,
            _configurations,
            ALL_NODES,
            ALL_NODES,
            METADATA_MAP,
            ImmutableSet.of(),
            _topology,
            _topology);
    assertThat(rows, empty());
  }

  @Test
  public void testLimitType() {
    // Session between nodes 1 and 2 has type EBGP_SINGLEHOP
    BgpSessionStatusQuestion q =
        new BgpSessionStatusQuestion(null, null, null, SessionType.EBGP_SINGLEHOP.name());
    List<Row> rows =
        getRows(
            q,
            _configurations,
            ALL_NODES,
            ALL_NODES,
            METADATA_MAP,
            ImmutableSet.of(),
            _topology,
            _topology);
    assertThat(rows, contains(ROW_1, ROW_2));

    // Session between nodes 3 and 4 has type IBGP
    q = new BgpSessionStatusQuestion(null, null, null, SessionType.IBGP.name());
    rows =
        getRows(
            q,
            _configurations,
            ALL_NODES,
            ALL_NODES,
            METADATA_MAP,
            ImmutableSet.of(),
            _topology,
            _topology);
    assertThat(rows, containsInAnyOrder(ROW_3, ROW_4));
  }

  @Test
  public void testUnnumbered() {
    // Create a pair of configs with an unnumbered session
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(NODE1)
            .build();
    Configuration c2 =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(NODE2)
            .build();
    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).setName(DEFAULT_VRF_NAME).build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).setName(DEFAULT_VRF_NAME).build();
    BgpProcess proc1 = nf.bgpProcessBuilder().setVrf(vrf1).build(); // .setRouterId()
    BgpProcess proc2 = nf.bgpProcessBuilder().setVrf(vrf2).build(); // .setRouterId()

    // Build interfaces and unnumbered peers
    String i1Name = "iface1";
    String i2Name = "iface2";
    nf.interfaceBuilder().setOwner(c1).setName(i1Name).setVrf(vrf1).build();
    nf.interfaceBuilder().setOwner(c2).setName(i2Name).setVrf(vrf1).build();
    BgpUnnumberedPeerConfig.Builder peerBuilder =
        BgpUnnumberedPeerConfig.builder().setLocalAs(1L).setRemoteAs(1L).setLocalIp(UNNUM_IP);
    proc1.setInterfaceNeighbors(
        ImmutableSortedMap.of(i1Name, peerBuilder.setPeerInterface(i1Name).build()));
    proc2.setInterfaceNeighbors(
        ImmutableSortedMap.of(i2Name, peerBuilder.setPeerInterface(i2Name).build()));

    // Build layer 2 topology with an edge between the two nodes' interfaces
    Layer2Topology layer2Topology =
        Layer2Topology.fromEdges(
            ImmutableSet.of(new Layer2Edge(NODE1, i1Name, null, NODE2, i2Name, null, null)));

    // Build BGP topology with layer 2 topology taken into account
    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        initBgpTopology(
                ImmutableMap.of(NODE1, c1, NODE2, c2), ImmutableMap.of(), false, layer2Topology)
            .getGraph();

    // Check answer. Should see two rows, one for each peer
    List<Row> rows =
        getRows(
            new BgpSessionStatusQuestion(),
            ImmutableMap.of(NODE1, c1, NODE2, c2),
            ALL_NODES,
            ALL_NODES,
            METADATA_MAP,
            ImmutableSet.of(),
            bgpTopology,
            bgpTopology);

    // Attributes both peers have in common
    Matcher<Row> commonMatchers =
        allOf(
            hasColumn(COL_VRF, DEFAULT_VRF_NAME, Schema.STRING),
            hasColumn(COL_LOCAL_IP, nullValue(), Schema.IP),
            hasColumn(COL_REMOTE_IP, nullValue(), Schema.IP),
            hasColumn(COL_LOCAL_AS, 1L, Schema.LONG),
            hasColumn(COL_REMOTE_AS, 1L, Schema.LONG),
            hasColumn(COL_SESSION_TYPE, SessionType.IBGP_UNNUMBERED.name(), Schema.STRING),
            hasColumn(COL_ESTABLISHED_STATUS, SessionStatus.ESTABLISHED.name(), Schema.STRING));
    NodeInterfacePair nip1 = new NodeInterfacePair(NODE1, i1Name);
    NodeInterfacePair nip2 = new NodeInterfacePair(NODE2, i2Name);
    assertThat(
        rows,
        containsInAnyOrder(
            allOf(
                // NODE1:iface1 -> NODE2:iface2
                commonMatchers,
                hasColumn(COL_NODE, new Node(NODE1), Schema.NODE),
                hasColumn(COL_LOCAL_INTERFACE, nip1, Schema.INTERFACE),
                hasColumn(COL_REMOTE_NODE, new Node(NODE2), Schema.NODE),
                hasColumn(COL_REMOTE_INTERFACE, nip2, Schema.INTERFACE)),
            allOf(
                // NODE2:iface2 -> NODE1:iface1
                commonMatchers,
                hasColumn(COL_NODE, new Node(NODE2), Schema.NODE),
                hasColumn(COL_LOCAL_INTERFACE, nip2, Schema.INTERFACE),
                hasColumn(COL_REMOTE_NODE, new Node(NODE1), Schema.NODE),
                hasColumn(COL_REMOTE_INTERFACE, nip1, Schema.INTERFACE))));
  }
}
