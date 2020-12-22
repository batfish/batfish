package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.ExprAclLine.REJECT_ALL;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.TunnelConfiguration;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TunnelTopologyTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  public SortedMap<String, Configuration> twoNodeNetwork(boolean withBlockingAcl) {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder();
    Interface.Builder ib = nf.interfaceBuilder();
    cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1;
    Configuration c2;
    c1 = cb.setHostname("c1").build();
    c2 = cb.setHostname("c2").build();
    Ip underlayIp1 = Ip.parse("4.4.4.1");
    Ip underlayIp2 = Ip.parse("4.4.4.2");
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("1.1.1.2");
    int subnetMask = 24;

    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).build();
    ib.setOwner(c1)
        .setAddress(ConcreteInterfaceAddress.create(ip1, subnetMask))
        .setType(InterfaceType.TUNNEL)
        .setTunnelConfig(
            TunnelConfiguration.builder()
                .setSourceAddress(underlayIp1)
                .setDestinationAddress(underlayIp2)
                .build())
        .setName("t1")
        .setVrf(vrf1)
        .build();
    ib.setOwner(c1)
        .setAddress(ConcreteInterfaceAddress.create(underlayIp1, subnetMask))
        .setType(InterfaceType.PHYSICAL)
        .setIncomingFilter(
            withBlockingAcl
                ? IpAccessList.builder()
                    .setName("REJECT_ALL")
                    .setLines(REJECT_ALL)
                    .setOwner(c1)
                    .build()
                : IpAccessList.builder()
                    .setName("ALLOW_GRE_REJECT_REST")
                    .setLines(
                        ExprAclLine.acceptingHeaderSpace(
                            HeaderSpace.builder().setIpProtocols(IpProtocol.GRE).build()),
                        REJECT_ALL)
                    .build())
        .setName("u1")
        .setVrf(vrf1)
        .build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).build();
    ib.setOwner(c2)
        .setAddress(ConcreteInterfaceAddress.create(ip2, subnetMask))
        .setType(InterfaceType.TUNNEL)
        .setTunnelConfig(
            TunnelConfiguration.builder()
                .setSourceAddress(underlayIp2)
                .setDestinationAddress(underlayIp1)
                .build())
        .setName("t2")
        .setVrf(vrf2)
        .build();
    ib.setOwner(c2)
        .setAddress(ConcreteInterfaceAddress.create(underlayIp2, subnetMask))
        .setType(InterfaceType.PHYSICAL)
        .setName("u2")
        .setVrf(vrf2)
        .build();

    return ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
  }

  /**
   * Tests that when we check reachability between two tunnels, we do it using the GRE protocol. Do
   * so by inserting an ACL between 2 physical interfaces that blocks GRE traffic. Note that the
   * tunnel link should be reflected in L3 edges.
   */
  @Test
  public void testPruneTunnelTopologyAclBlocks() throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(twoNodeNetwork(true), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    Topology topology = batfish.getTopologyProvider().getLayer3Topology(batfish.getSnapshot());
    assertThat(
        topology.getEdges(),
        not(hasItem(new Edge(NodeInterfacePair.of("c1", "t1"), NodeInterfacePair.of("c2", "t2")))));
  }

  /**
   * Tests that when we check reachability between two tunnels, we do it using the GRE protocol. Do
   * so by inserting an ACL between 2 physical interfaces that allows only GRE traffic. Note that
   * the tunnel link should be reflected in L3 edges.
   */
  @Test
  public void testPruneTunnelTopologyNoACL() throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(twoNodeNetwork(false), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    Topology topology = batfish.getTopologyProvider().getLayer3Topology(batfish.getSnapshot());
    assertThat(
        topology.getEdges(),
        hasItem(new Edge(NodeInterfacePair.of("c1", "t1"), NodeInterfacePair.of("c2", "t2"))));
  }
}
