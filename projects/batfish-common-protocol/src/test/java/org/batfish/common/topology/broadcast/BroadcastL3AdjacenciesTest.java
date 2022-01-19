package org.batfish.common.topology.broadcast;

import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.junit.Test;

public class BroadcastL3AdjacenciesTest {
  /** A simple test that the code works end-to-end. */
  @Test
  public void testE2e() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 = nf.configurationBuilder().build();
    Interface i1 =
        nf.interfaceBuilder()
            .setOwner(c1)
            .setType(PHYSICAL)
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.1/24"))
            .build();
    NodeInterfacePair n1 = NodeInterfacePair.of(i1);
    Configuration c2 = nf.configurationBuilder().build();
    Interface i2 =
        nf.interfaceBuilder()
            .setOwner(c2)
            .setType(PHYSICAL)
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.2/24"))
            .build();
    NodeInterfacePair n2 = NodeInterfacePair.of(i2);
    Configuration c3 = nf.configurationBuilder().build();
    Interface i3 =
        nf.interfaceBuilder()
            .setOwner(c3)
            .setType(PHYSICAL)
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.3/24"))
            .build();
    NodeInterfacePair n3 = NodeInterfacePair.of(i3);
    {
      // With no L1 topology, all 3 interfaces in same domain but not p2p domain.
      BroadcastL3Adjacencies adjacencies =
          BroadcastL3Adjacencies.create(
              Layer1Topologies.empty(),
              VxlanTopology.EMPTY,
              ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3));
      assertTrue(adjacencies.inSameBroadcastDomain(n1, n2));
      assertTrue(adjacencies.inSameBroadcastDomain(n2, n1));
      assertTrue(adjacencies.inSameBroadcastDomain(n2, n3));
      assertTrue(adjacencies.inSameBroadcastDomain(n3, n1));
      assertFalse(adjacencies.inSamePointToPointDomain(n1, n2));
      assertFalse(adjacencies.inSamePointToPointDomain(n2, n3));
      assertFalse(adjacencies.inSamePointToPointDomain(n3, n1));
    }
    {
      // With L1 topology, only connected interfaces in same domain.
      Layer1Topology physical =
          new Layer1Topology(
              new Layer1Edge(
                  n1.getHostname(), n1.getInterface(), n3.getHostname(), n3.getInterface()));
      BroadcastL3Adjacencies adjacencies =
          BroadcastL3Adjacencies.create(
              new Layer1Topologies(physical, Layer1Topology.EMPTY, physical, physical),
              VxlanTopology.EMPTY,
              ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3));
      assertTrue(adjacencies.inSameBroadcastDomain(n1, n3));
      assertFalse(adjacencies.inSameBroadcastDomain(n2, n3));
      assertFalse(adjacencies.inSameBroadcastDomain(n2, n1));
      assertTrue(adjacencies.inSamePointToPointDomain(n1, n3));
      assertTrue(adjacencies.inSamePointToPointDomain(n3, n1));
      assertFalse(adjacencies.inSamePointToPointDomain(n2, n3));
      assertFalse(adjacencies.inSamePointToPointDomain(n1, n2));
    }
    {
      // Encapsulation vlan honored, even with no L1.
      i1.setEncapsulationVlan(4);
      BroadcastL3Adjacencies adjacencies =
          BroadcastL3Adjacencies.create(
              Layer1Topologies.empty(),
              VxlanTopology.EMPTY,
              ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3));
      assertTrue(adjacencies.inSameBroadcastDomain(n2, n3));
      assertFalse(adjacencies.inSameBroadcastDomain(n1, n2));
      assertFalse(adjacencies.inSameBroadcastDomain(n1, n3));
      assertFalse(adjacencies.inSamePointToPointDomain(n2, n3));
      assertFalse(adjacencies.inSamePointToPointDomain(n1, n2));
    }
  }
}
