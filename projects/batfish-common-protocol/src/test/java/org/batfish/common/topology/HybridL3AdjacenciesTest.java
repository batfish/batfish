package org.batfish.common.topology;

import static org.batfish.common.topology.HybridL3Adjacencies.computeL3ToPhysical;
import static org.batfish.common.topology.HybridL3Adjacencies.computePhysicalPointToPoint;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.util.Optional;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public class HybridL3AdjacenciesTest {

  @Test
  public void testInSameBroadcastDomain() {
    L3Adjacencies adjacencies =
        HybridL3Adjacencies.createForTesting(
            ImmutableSet.of("a", "b"),
            Layer2Topology.fromDomains(
                ImmutableSet.of(
                    ImmutableSet.of(
                        new Layer2Node("a", "i", null), new Layer2Node("b", "i2", null)))),
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMultimap.of());
    // Both have L1 info, but edge in L2.
    assertTrue(
        adjacencies.inSameBroadcastDomain(
            NodeInterfacePair.of("a", "i"), NodeInterfacePair.of("b", "i2")));
    // Both have L1 info, but edge not in L2.
    assertFalse(
        adjacencies.inSameBroadcastDomain(
            NodeInterfacePair.of("a", "i3"), NodeInterfacePair.of("b", "i2")));
    // Only first node has L1, but since second node does not they are adjacent.
    assertTrue(
        adjacencies.inSameBroadcastDomain(
            NodeInterfacePair.of("a", "i3"), NodeInterfacePair.of("c", "i")));
    // Only second node has L1, but since first node does not they are adjacent.
    assertTrue(
        adjacencies.inSameBroadcastDomain(
            NodeInterfacePair.of("c", "i"), NodeInterfacePair.of("a", "i3")));
    // Since c has no info, even true if the interface is in L2 topology.
    assertTrue(
        adjacencies.inSameBroadcastDomain(
            NodeInterfacePair.of("c", "i"), NodeInterfacePair.of("a", "i")));
  }

  @Test
  public void testPairedPointToPointL3Interface() {
    NodeInterfacePair aL3 = NodeInterfacePair.of("a", "l3");
    NodeInterfacePair aPhys = NodeInterfacePair.of("a", "phys");
    NodeInterfacePair bL3 = NodeInterfacePair.of("b", "l3");
    NodeInterfacePair bL32 = NodeInterfacePair.of("b", "l32");
    NodeInterfacePair bPhys = NodeInterfacePair.of("b", "phys");

    {
      // Connected except for al3->physical. So should only work 1 way
      // (this does not meet construction invariants, just for testing).
      L3Adjacencies missingAL3 =
          HybridL3Adjacencies.createForTesting(
              ImmutableSet.of(),
              Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(aL3), l2(bL3)))),
              ImmutableMap.of(aPhys, bPhys, bPhys, aPhys),
              ImmutableMap.of(bL3, bPhys, bL32, bPhys), // no aL3->aPhys
              ImmutableMultimap.of(aPhys, aL3, bPhys, bL3, bPhys, bL32));
      assertThat(missingAL3.pairedPointToPointL3Interface(aL3), equalTo(Optional.empty()));
      assertThat(missingAL3.pairedPointToPointL3Interface(bL3), equalTo(Optional.of(aL3)));
      assertFalse(missingAL3.inSamePointToPointDomain(aL3, bL3));
      assertFalse(missingAL3.inSamePointToPointDomain(bL3, aL3));
    }

    {
      // Connected except for aPhys<->bPhys.
      L3Adjacencies missingL1 =
          HybridL3Adjacencies.createForTesting(
              ImmutableSet.of(),
              Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(aL3), l2(bL3)))),
              ImmutableMap.of(),
              ImmutableMap.of(aL3, aPhys, bL3, bPhys, bL32, bPhys),
              ImmutableMultimap.of(aPhys, aL3, bPhys, bL3, bPhys, bL32));
      assertThat(missingL1.pairedPointToPointL3Interface(aL3), equalTo(Optional.empty()));
      assertThat(missingL1.pairedPointToPointL3Interface(bL3), equalTo(Optional.empty()));
      assertFalse(missingL1.inSamePointToPointDomain(aL3, bL3));
      assertFalse(missingL1.inSamePointToPointDomain(bL3, aL3));
    }

    {
      // Connected except for bPhys->bL3. So works from bL3, but not from aL3
      L3Adjacencies missingBL3 =
          HybridL3Adjacencies.createForTesting(
              ImmutableSet.of(),
              Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(aL3), l2(bL3)))),
              ImmutableMap.of(aPhys, bPhys, bPhys, aPhys),
              ImmutableMap.of(aL3, aPhys, bL3, bPhys, bL32, bPhys),
              ImmutableMultimap.of(aPhys, aL3, bPhys, bL32));
      assertThat(missingBL3.pairedPointToPointL3Interface(aL3), equalTo(Optional.empty()));
      assertThat(missingBL3.pairedPointToPointL3Interface(bL3), equalTo(Optional.of(aL3)));
      assertFalse(missingBL3.inSamePointToPointDomain(aL3, bL3));
      assertFalse(missingBL3.inSamePointToPointDomain(bL3, aL3));
    }

    {
      // Missing the L2 adjacency.
      L3Adjacencies missingL2 =
          HybridL3Adjacencies.createForTesting(
              ImmutableSet.of(),
              Layer2Topology.EMPTY,
              ImmutableMap.of(aPhys, bPhys, bPhys, aPhys),
              ImmutableMap.of(aL3, aPhys, bL3, bPhys, bL32, bPhys),
              ImmutableMultimap.of(aPhys, aL3, bPhys, bL3, bPhys, bL32));
      assertThat(missingL2.pairedPointToPointL3Interface(aL3), equalTo(Optional.empty()));
      assertThat(missingL2.pairedPointToPointL3Interface(bL3), equalTo(Optional.empty()));
      assertFalse(missingL2.inSamePointToPointDomain(aL3, bL3));
      assertFalse(missingL2.inSamePointToPointDomain(bL3, aL3));
    }

    {
      // Too many neighbors for aL3->bL3, but works in reverse.
      L3Adjacencies notP2P =
          HybridL3Adjacencies.createForTesting(
              ImmutableSet.of(),
              Layer2Topology.fromDomains(
                  ImmutableSet.of(ImmutableSet.of(l2(aL3), l2(bL3), l2(bL32)))),
              ImmutableMap.of(aPhys, bPhys, bPhys, aPhys),
              ImmutableMap.of(aL3, aPhys, bL3, bPhys, bL32, bPhys),
              ImmutableMultimap.of(aPhys, aL3, bPhys, bL3, bPhys, bL32));
      assertThat(notP2P.pairedPointToPointL3Interface(aL3), equalTo(Optional.empty()));
      assertThat(notP2P.pairedPointToPointL3Interface(bL3), equalTo(Optional.of(aL3)));
      assertFalse(notP2P.inSamePointToPointDomain(aL3, bL3));
      assertFalse(notP2P.inSamePointToPointDomain(bL3, aL3));
    }

    {
      // Actually correctly configured. aL3 <> aPhys <> bPhys <> [bL3,bL32], but L2 only allows bL3.
      L3Adjacencies correct =
          HybridL3Adjacencies.createForTesting(
              ImmutableSet.of(),
              Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(aL3), l2(bL3)))),
              ImmutableMap.of(aPhys, bPhys, bPhys, aPhys),
              ImmutableMap.of(aL3, aPhys, bL3, bPhys, bL32, bPhys),
              ImmutableMultimap.of(aPhys, aL3, bPhys, bL3, bPhys, bL32));
      assertThat(correct.pairedPointToPointL3Interface(aL3), equalTo(Optional.of(bL3)));
      assertThat(correct.pairedPointToPointL3Interface(bL3), equalTo(Optional.of(aL3)));
      assertTrue(correct.inSamePointToPointDomain(aL3, bL3));
      assertTrue(correct.inSamePointToPointDomain(bL3, aL3));
    }
  }

  @Test
  public void testComputePhysicalPointToPoint() {
    NodeInterfacePair nip1 = NodeInterfacePair.of("a", "b");
    NodeInterfacePair nip2 = NodeInterfacePair.of("c", "d");
    NodeInterfacePair nip3 = NodeInterfacePair.of("e", "f");
    // One-sided
    assertThat(
        computePhysicalPointToPoint(new Layer1Topology(new Layer1Edge(l1(nip1), l1(nip2)))),
        equalTo(ImmutableMap.of(nip1, nip2, nip2, nip1)));
    // Bidir
    assertThat(
        computePhysicalPointToPoint(
            new Layer1Topology(
                new Layer1Edge(l1(nip1), l1(nip2)), new Layer1Edge(l1(nip2), l1(nip1)))),
        equalTo(ImmutableMap.of(nip1, nip2, nip2, nip1)));
    // 3 nodes
    assertThat(
        computePhysicalPointToPoint(
            new Layer1Topology(
                new Layer1Edge(l1(nip1), l1(nip2)), new Layer1Edge(l1(nip2), l1(nip3)))),
        equalTo(ImmutableMap.of()));
  }

  private static Layer1Node l1(NodeInterfacePair nip) {
    return new Layer1Node(nip.getHostname(), nip.getInterface());
  }

  private static Layer2Node l2(NodeInterfacePair nip) {
    return new Layer2Node(nip.getHostname(), nip.getInterface(), null);
  }

  @Test
  public void testEquals() {
    NodeInterfacePair nip1 = NodeInterfacePair.of("a", "b");
    NodeInterfacePair nip2 = NodeInterfacePair.of("c", "d");
    NodeInterfacePair nip3 = NodeInterfacePair.of("e", "f");
    NodeInterfacePair nip4 = NodeInterfacePair.of("g", "h");
    new EqualsTester()
        .addEqualityGroup(
            HybridL3Adjacencies.create(
                Layer1Topology.EMPTY,
                Layer1Topology.EMPTY,
                Layer2Topology.EMPTY,
                ImmutableMap.of()),
            HybridL3Adjacencies.createForTesting(
                ImmutableSet.of(),
                Layer2Topology.EMPTY,
                ImmutableMap.of(),
                ImmutableMap.of(),
                ImmutableMultimap.of()))
        .addEqualityGroup(
            HybridL3Adjacencies.createForTesting(
                ImmutableSet.of("a"),
                Layer2Topology.EMPTY,
                ImmutableMap.of(),
                ImmutableMap.of(),
                ImmutableMultimap.of()))
        .addEqualityGroup(
            HybridL3Adjacencies.createForTesting(
                ImmutableSet.of("a"),
                Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(nip1)))),
                ImmutableMap.of(),
                ImmutableMap.of(),
                ImmutableMultimap.of()))
        .addEqualityGroup(
            HybridL3Adjacencies.createForTesting(
                ImmutableSet.of("a"),
                Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(nip1)))),
                ImmutableMap.of(nip1, nip2),
                ImmutableMap.of(),
                ImmutableMultimap.of()))
        .addEqualityGroup(
            HybridL3Adjacencies.createForTesting(
                ImmutableSet.of("a"),
                Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(nip1)))),
                ImmutableMap.of(nip1, nip2),
                ImmutableMap.of(nip3, nip4),
                ImmutableMultimap.of()))
        .addEqualityGroup(
            HybridL3Adjacencies.createForTesting(
                ImmutableSet.of("a"),
                Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(nip1)))),
                ImmutableMap.of(nip1, nip2),
                ImmutableMap.of(nip3, nip4),
                ImmutableMultimap.of(nip4, nip1)))
        .testEquals();
  }

  @Test
  public void testComputeL3ToPhysical() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    // Inactive interface, so not valid.
    nf.interfaceBuilder()
        .setOwner(c)
        .setActive(false)
        .setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1")))
        .setSwitchport(false)
        .setType(InterfaceType.PHYSICAL)
        .build();
    // Active, but switchport is true, so no dice.
    nf.interfaceBuilder()
        .setOwner(c)
        .setActive(true)
        .setSwitchport(true)
        .setType(InterfaceType.PHYSICAL)
        .build();
    Interface physical =
        nf.interfaceBuilder()
            .setOwner(c)
            .setActive(true)
            .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
            .setSwitchport(false)
            .setType(InterfaceType.PHYSICAL)
            .build();
    Interface lla =
        nf.interfaceBuilder()
            .setOwner(c)
            .setActive(true)
            .setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1")))
            .setSwitchport(false)
            .setType(InterfaceType.PHYSICAL)
            .build();
    Interface agg =
        nf.interfaceBuilder()
            .setOwner(c)
            .setActive(true)
            .setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1")))
            .setSwitchport(false)
            .setType(InterfaceType.AGGREGATED)
            .build();
    Interface aggChild =
        nf.interfaceBuilder()
            .setOwner(c)
            .setActive(true)
            .setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1")))
            .setSwitchport(false)
            .setType(InterfaceType.AGGREGATE_CHILD)
            .setDependencies(ImmutableSet.of(new Dependency(agg.getName(), DependencyType.BIND)))
            .build();
    // A virtual interface like a VLAN has no connection
    nf.interfaceBuilder()
        .setOwner(c)
        .setActive(true)
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setSwitchport(false)
        .setType(InterfaceType.VLAN)
        .build();
    assertThat(
        computeL3ToPhysical(ImmutableMap.of(c.getHostname(), c)),
        equalTo(
            ImmutableMap.of(
                NodeInterfacePair.of(physical),
                NodeInterfacePair.of(physical),
                NodeInterfacePair.of(lla),
                NodeInterfacePair.of(lla),
                NodeInterfacePair.of(agg),
                NodeInterfacePair.of(agg),
                NodeInterfacePair.of(aggChild),
                NodeInterfacePair.of(agg))));
  }
}
