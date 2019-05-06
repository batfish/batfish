package org.batfish.dataplane.topology;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.Collections;
import java.util.SortedMap;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfNeighborConfigId;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.OspfSessionProperties;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.ospf.OspfTopology.EdgeId;
import org.batfish.datamodel.ospf.OspfTopologyUtils;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * A more end-to-end test of {@link OspfTopologyUtils} and computation of OSPF topology using a full
 * network (and {@link TopologyProvider})
 */
public class OspfTopologyTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  /*

   Simple network to test OSPF session establishment (single area 0, p2p links).

     r2 1.1.1.1/31
     +
     |
     |
     | .0/31 - ospf passive
     |
     +
    r1 +------------+r3  1.1.1.3/31
     +  .2/31 - active
     |
     | .4/31 - shutdown
     |
     |
     +
    r4 1.1.1.5/31

  */
  private SortedMap<String, Configuration> getBaseNetwork() {

    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setBandwidth(10e6).setOspfCost(1);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    OspfProcess.Builder opb = nf.ospfProcessBuilder().setProcessId("1");
    OspfArea.Builder oab = nf.ospfAreaBuilder().setNumber(0).setNonStub();

    // R1
    Configuration c1 = cb.setHostname("r1").build();
    Vrf vrf1 = vb.setOwner(c1).build();
    OspfProcess proc1 = opb.setVrf(vrf1).build();
    Interface i12 =
        ib.setAddress(new InterfaceAddress("1.1.1.0/31"))
            .setVrf(vrf1)
            .setOwner(c1)
            .setOspfEnabled(true)
            .setOspfPassive(true)
            .setOspfProcess("1")
            .setName("i12")
            .build();

    Interface i13 =
        ib.setAddress(new InterfaceAddress("1.1.1.2/31"))
            .setVrf(vrf1)
            .setOwner(c1)
            .setOspfEnabled(true)
            .setOspfPassive(false)
            .setOspfProcess("1")
            .setName("i13")
            .build();

    Interface i14 =
        ib.setAddress(new InterfaceAddress("1.1.1.4/31"))
            .setVrf(vrf1)
            .setOwner(c1)
            .setOspfEnabled(true)
            .setOspfPassive(false)
            .setOspfProcess("1")
            .setActive(false)
            .setName("i14")
            .build();
    oab.addInterfaces(ImmutableList.of(i12.getName(), i13.getName(), i14.getName()))
        .setOspfProcess(proc1)
        .build();

    ib.setActive(true);

    // R2
    Configuration c2 = cb.setHostname("r2").build();
    Vrf vrf2 = vb.setOwner(c2).build();
    OspfProcess proc2 = opb.setVrf(vrf2).build();
    Interface i21 =
        ib.setAddress(new InterfaceAddress("1.1.1.1/31"))
            .setVrf(vrf2)
            .setOwner(c2)
            .setOspfEnabled(true)
            .setOspfPassive(false)
            .setOspfProcess("1")
            .setName("i21")
            .build();
    oab.setInterfaces(Collections.singleton(i21.getName())).setOspfProcess(proc2).build();

    // R3
    Configuration c3 = cb.setHostname("r3").build();
    Vrf vrf3 = vb.setOwner(c3).build();
    OspfProcess proc3 = opb.setVrf(vrf3).build();
    Interface i31 =
        ib.setAddress(new InterfaceAddress("1.1.1.3/31"))
            .setVrf(vrf3)
            .setOwner(c3)
            .setOspfEnabled(true)
            .setOspfPassive(false)
            .setOspfProcess("1")
            .setName("i31")
            .build();
    oab.setInterfaces(Collections.singleton(i31.getName())).setOspfProcess(proc3).build();

    // R4
    Configuration c4 = cb.setHostname("r4").build();
    Vrf vrf4 = vb.setOwner(c4).build();
    OspfProcess proc4 = opb.setVrf(vrf4).build();
    Interface i41 =
        ib.setAddress(new InterfaceAddress("1.1.1.4/31"))
            .setVrf(vrf4)
            .setOwner(c4)
            .setOspfEnabled(true)
            .setOspfPassive(false)
            .setOspfProcess("1")
            .setName("i41")
            .build();
    oab.setInterfaces(Collections.singleton(i41.getName())).setOspfProcess(proc4).build();
    return ImmutableSortedMap.of("r1", c1, "r2", c2, "r3", c3, "r4", c4);
  }

  @Test
  public void testOspfTopology() throws IOException {
    SortedMap<String, Configuration> configs = getBaseNetwork();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, folder);

    // Force init configs, because test batfish does not post-process configs
    OspfTopologyUtils.initNeighborConfigs(NetworkConfigurations.of(configs));

    OspfTopology topology =
        batfish.getTopologyProvider().getInitialOspfTopology(batfish.getNetworkSnapshot());

    // Active neighbor relationship
    final OspfNeighborConfigId r1i13 =
        new OspfNeighborConfigId("r1", Configuration.DEFAULT_VRF_NAME, "1", "i13");
    final OspfNeighborConfigId r3i31 =
        new OspfNeighborConfigId("r3", Configuration.DEFAULT_VRF_NAME, "1", "i31");
    assertThat(topology.neighbors(r1i13), equalTo(ImmutableSet.of(r3i31)));
    assertThat(topology.neighbors(r3i31), equalTo(ImmutableSet.of(r1i13)));
    assertThat(
        topology.getSession(OspfTopology.makeEdge(r1i13, r3i31)).get(),
        equalTo(
            new OspfSessionProperties(0, new IpLink(Ip.parse("1.1.1.2"), Ip.parse("1.1.1.3")))));
    assertThat(
        topology.getSession(OspfTopology.makeEdge(r3i31, r1i13)).get(),
        equalTo(
            new OspfSessionProperties(0, new IpLink(Ip.parse("1.1.1.3"), Ip.parse("1.1.1.2")))));

    // Everyone else has no neighbors
    assertThat(
        topology.neighbors(
            new OspfNeighborConfigId("r1", Configuration.DEFAULT_VRF_NAME, "p", "i12")),
        empty());
    assertThat(
        topology.neighbors(
            new OspfNeighborConfigId("r1", Configuration.DEFAULT_VRF_NAME, "p", "i14")),
        empty());
    assertThat(
        topology.neighbors(
            new OspfNeighborConfigId("r2", Configuration.DEFAULT_VRF_NAME, "p", "i21")),
        empty());
    assertThat(
        topology.neighbors(
            new OspfNeighborConfigId("r4", Configuration.DEFAULT_VRF_NAME, "p", "i41")),
        empty());
  }

  @Test
  public void testEdgeIdJsonSerialization() throws IOException {
    final OspfNeighborConfigId r1i13 =
        new OspfNeighborConfigId("r1", Configuration.DEFAULT_VRF_NAME, "1", "i13");
    final OspfNeighborConfigId r3i31 =
        new OspfNeighborConfigId("r3", Configuration.DEFAULT_VRF_NAME, "1", "i31");
    EdgeId edge = OspfTopology.makeEdge(r1i13, r3i31);
    assertThat(BatfishObjectMapper.clone(edge, EdgeId.class), equalTo(edge));
  }
}
