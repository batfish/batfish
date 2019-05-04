package org.batfish.dataplane.ibdp.schedule;

import static org.batfish.common.topology.TopologyUtil.computeIpNodeOwners;
import static org.batfish.datamodel.bgp.BgpTopologyUtils.initBgpTopology;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.batfish.common.topology.TopologyUtil;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.ospf.OspfTopologyUtils;
import org.batfish.dataplane.ibdp.Node;
import org.batfish.dataplane.ibdp.TestUtils;
import org.batfish.dataplane.ibdp.TopologyContext;
import org.batfish.dataplane.ibdp.schedule.NodeColoredSchedule.Coloring;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/** Tests for {@link org.batfish.dataplane.ibdp.schedule.NodeColoredSchedule} */
@RunWith(Parameterized.class)
public class NodeColoredScheduleTest {

  private ImmutableSortedMap<String, Configuration> _configurations;

  @Parameters
  public static Collection<Object[]> data() {
    return ImmutableList.copyOf(
        new Coloring[][] {{Coloring.SATURATION}, {Coloring.GREEDY}, {Coloring.RANDOM}});
  }

  @Parameter public Coloring _coloring;

  @Before
  public void setup() {
    // Init BGP processes
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Vrf.Builder vb = nf.vrfBuilder();
    Interface.Builder ib = nf.interfaceBuilder().setOspfEnabled(true).setOspfProcess("1");
    BgpProcess.Builder pb = nf.bgpProcessBuilder();
    BgpActivePeerConfig.Builder nb = nf.bgpNeighborBuilder();
    OspfProcess.Builder ob = nf.ospfProcessBuilder().setProcessId("1").setReferenceBandwidth(1e8);
    OspfArea.Builder ospfArea = nf.ospfAreaBuilder().setNumber(0);

    Configuration r1 = cb.setHostname("r1").build();
    Vrf vrf1 = vb.setOwner(r1).build();
    final int networkBits = 30;
    final Ip R1_IP = Ip.parse("1.1.1.1");
    final Ip R2_IP = Ip.parse("1.1.1.2");
    // Interface
    Interface i1 =
        ib.setOwner(r1).setVrf(vrf1).setAddress(new InterfaceAddress(R1_IP, networkBits)).build();
    // Make OSPF process and areas
    OspfArea r1ospfArea = ospfArea.setInterfaces(ImmutableSet.of(i1.getName())).build();
    ob.setVrf(vrf1).setAreas(ImmutableSortedMap.of(0L, r1ospfArea)).build();
    i1.setOspfArea(r1ospfArea);
    // BGP process and neighbor
    BgpProcess r1Proc = pb.setRouterId(R1_IP).setVrf(vrf1).build();
    nb.setRemoteAs(2L)
        .setPeerAddress(R2_IP)
        .setBgpProcess(r1Proc)
        .setLocalAs(1L)
        .setLocalIp(R1_IP)
        .build();

    Configuration r2 = cb.setHostname("r2").build();
    Vrf vrf2 = vb.setOwner(r2).build();
    // Interface
    Interface i2 =
        ib.setOwner(r2).setVrf(vrf2).setAddress(new InterfaceAddress(R2_IP, networkBits)).build();
    // Make OSPF process and areas
    OspfArea r2ospfArea = ospfArea.setInterfaces(ImmutableSet.of(i2.getName())).build();
    ob.setVrf(vrf2).setAreas(ImmutableSortedMap.of(0L, r2ospfArea)).build();
    // BGP process and neighbor
    BgpProcess r2Proc = pb.setRouterId(R2_IP).setVrf(vrf2).build();
    nb.setRemoteAs(1L)
        .setPeerAddress(R1_IP)
        .setBgpProcess(r2Proc)
        .setLocalAs(2L)
        .setRouteReflectorClient(true)
        .setLocalIp(R2_IP)
        .build();

    _configurations =
        new ImmutableSortedMap.Builder<String, Configuration>(String::compareTo)
            .put(r1.getHostname(), r1)
            .put(r2.getHostname(), r2)
            .build();
  }

  @Test
  public void testSingleNodeColoring() {
    Node n = TestUtils.makeIosRouter("r1");
    Map<String, Node> nodes = ImmutableMap.of("r1", n);
    Map<String, Configuration> configs = ImmutableMap.of("r1", n.getConfiguration());
    BgpTopology bgpTopology = initBgpTopology(configs, computeIpNodeOwners(configs, false), false);
    NodeColoredSchedule schedule =
        new NodeColoredSchedule(
            nodes, _coloring, TopologyContext.builder().setBgpTopology(bgpTopology).build());

    assertThat(schedule.hasNext(), is(true));
    assertThat(schedule.next(), equalTo(nodes));
    assertThat(schedule.hasNext(), is(false));
  }

  @Test
  public void testTwoNodeColoringNoAdjacencies() {
    Map<String, Node> nodes =
        ImmutableMap.of("r1", TestUtils.makeIosRouter("r1"), "r2", TestUtils.makeIosRouter("r2"));

    Map<String, Configuration> configs =
        nodes.entrySet().stream()
            .collect(
                ImmutableMap.toImmutableMap(Entry::getKey, e -> e.getValue().getConfiguration()));
    BgpTopology bgpTopology = initBgpTopology(configs, computeIpNodeOwners(configs, false), false);
    NodeColoredSchedule schedule =
        new NodeColoredSchedule(
            nodes, _coloring, TopologyContext.builder().setBgpTopology(bgpTopology).build());

    // Expect both nodes to have the same color because there is no edge between them
    assertThat(schedule.hasNext(), is(true));
    assertThat(schedule.next(), allOf(hasKey("r1"), hasKey("r2")));
    assertThat(schedule.hasNext(), is(false));
  }

  @Test
  public void testTwoNodesConnectedDirectlyViaBGP() {

    BgpTopology bgpTopology =
        initBgpTopology(_configurations, computeIpNodeOwners(_configurations, false), false);
    ImmutableMap<String, Node> nodes =
        _configurations.entrySet().stream()
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> new Node(e.getValue())));

    // Note empty OSPF topology
    NodeColoredSchedule schedule =
        new NodeColoredSchedule(
            nodes, _coloring, TopologyContext.builder().setBgpTopology(bgpTopology).build());
    ImmutableList<Map<String, Node>> coloredNodes =
        ImmutableList.copyOf(schedule.getAllRemaining());
    // 2 colors because of edge in the BGP topology
    assertThat(coloredNodes, hasSize(2));
  }

  @Test
  public void testNodeColoringWithOspfEdge() {
    ImmutableMap<String, Node> nodes =
        _configurations.entrySet().stream()
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> new Node(e.getValue())));
    OspfTopologyUtils.initNeighborConfigs(NetworkConfigurations.of(_configurations));
    OspfTopology ospfTopology =
        OspfTopologyUtils.computeOspfTopology(
            NetworkConfigurations.of(_configurations),
            TopologyUtil.synthesizeL3Topology(_configurations));

    NodeColoredSchedule schedule =
        // Note empty BGP topology
        new NodeColoredSchedule(
            nodes, _coloring, TopologyContext.builder().setOspfTopology(ospfTopology).build());
    ImmutableList<Map<String, Node>> coloredNodes =
        ImmutableList.copyOf(schedule.getAllRemaining());
    // 2 colors because of edge in the OSPF topology
    assertThat(coloredNodes, hasSize(2));
  }
}
