package org.batfish.common.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.util.Optional;
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
            PointToPointInterfaces.createForTesting(ImmutableMap.of(), ImmutableMap.of()));
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
      // Connected except for aPhys<->bPhys.
      L3Adjacencies missingL1 =
          HybridL3Adjacencies.createForTesting(
              ImmutableSet.of(),
              Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(aL3), l2(bL3)))),
              PointToPointInterfaces.createForTesting(
                  ImmutableMap.of(), ImmutableMap.of(aL3, aPhys, bL3, bPhys, bL32, bPhys)));
      assertThat(missingL1.pairedPointToPointL3Interface(aL3), equalTo(Optional.empty()));
      assertThat(missingL1.pairedPointToPointL3Interface(bL3), equalTo(Optional.empty()));
      assertFalse(missingL1.inSamePointToPointDomain(aL3, bL3));
      assertFalse(missingL1.inSamePointToPointDomain(bL3, aL3));
    }

    {
      // Missing the L2 adjacency.
      L3Adjacencies missingL2 =
          HybridL3Adjacencies.createForTesting(
              ImmutableSet.of(),
              Layer2Topology.EMPTY,
              PointToPointInterfaces.createForTesting(
                  ImmutableMap.of(aPhys, bPhys, bPhys, aPhys),
                  ImmutableMap.of(aL3, aPhys, bL3, bPhys, bL32, bPhys)));
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
              PointToPointInterfaces.createForTesting(
                  ImmutableMap.of(aPhys, bPhys, bPhys, aPhys),
                  ImmutableMap.of(aL3, aPhys, bL3, bPhys, bL32, bPhys)));
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
              PointToPointInterfaces.createForTesting(
                  ImmutableMap.of(aPhys, bPhys, bPhys, aPhys),
                  ImmutableMap.of(aL3, aPhys, bL3, bPhys, bL32, bPhys)));
      assertThat(correct.pairedPointToPointL3Interface(aL3), equalTo(Optional.of(bL3)));
      assertThat(correct.pairedPointToPointL3Interface(bL3), equalTo(Optional.of(aL3)));
      assertTrue(correct.inSamePointToPointDomain(aL3, bL3));
      assertTrue(correct.inSamePointToPointDomain(bL3, aL3));
    }
  }

  private static Layer2Node l2(NodeInterfacePair nip) {
    return new Layer2Node(nip.getHostname(), nip.getInterface(), null);
  }

  @Test
  public void testEquals() {
    NodeInterfacePair nip1 = NodeInterfacePair.of("a", "b");
    NodeInterfacePair nip2 = NodeInterfacePair.of("c", "d");
    new EqualsTester()
        .addEqualityGroup(
            HybridL3Adjacencies.create(
                Layer1Topologies.empty(), Layer2Topology.EMPTY, ImmutableMap.of()),
            HybridL3Adjacencies.createForTesting(
                ImmutableSet.of(),
                Layer2Topology.EMPTY,
                PointToPointInterfaces.createForTesting(ImmutableMap.of(), ImmutableMap.of())))
        .addEqualityGroup(
            HybridL3Adjacencies.createForTesting(
                ImmutableSet.of("a"),
                Layer2Topology.EMPTY,
                PointToPointInterfaces.createForTesting(ImmutableMap.of(), ImmutableMap.of())))
        .addEqualityGroup(
            HybridL3Adjacencies.createForTesting(
                ImmutableSet.of("a"),
                Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(nip1)))),
                PointToPointInterfaces.createForTesting(ImmutableMap.of(), ImmutableMap.of())))
        .addEqualityGroup(
            HybridL3Adjacencies.createForTesting(
                ImmutableSet.of("a"),
                Layer2Topology.fromDomains(ImmutableSet.of(ImmutableSet.of(l2(nip1)))),
                PointToPointInterfaces.createForTesting(
                    ImmutableMap.of(nip1, nip2), ImmutableMap.of())))
        .testEquals();
  }
}
