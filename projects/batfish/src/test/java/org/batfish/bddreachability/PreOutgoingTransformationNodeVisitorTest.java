package org.batfish.bddreachability;

import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutInterfaceDeliveredToSubnet;
import org.batfish.z3.state.PreOutInterfaceExitsNetwork;
import org.batfish.z3.state.PreOutInterfaceInsufficientInfo;
import org.batfish.z3.state.PreOutInterfaceNeighborUnreachable;
import org.junit.Test;

/** Tests for {@link PreOutgoingTransformationNodeVisitor}. */
public final class PreOutgoingTransformationNodeVisitorTest {

  @Test
  public void testPreOutgoingTransformationStates() {
    String node = "node";
    String iface = "iface";

    assertEquals(
        new PreOutEdge(node, iface, "", "").accept(PreOutgoingTransformationNodeVisitor.INSTANCE),
        new NodeInterfacePair(node, iface));
    assertEquals(
        new PreOutInterfaceDeliveredToSubnet(node, iface)
            .accept(PreOutgoingTransformationNodeVisitor.INSTANCE),
        new NodeInterfacePair(node, iface));
    assertEquals(
        new PreOutInterfaceExitsNetwork(node, iface)
            .accept(PreOutgoingTransformationNodeVisitor.INSTANCE),
        new NodeInterfacePair(node, iface));
    assertEquals(
        new PreOutInterfaceInsufficientInfo(node, iface)
            .accept(PreOutgoingTransformationNodeVisitor.INSTANCE),
        new NodeInterfacePair(node, iface));
    assertEquals(
        new PreOutInterfaceNeighborUnreachable(node, iface)
            .accept(PreOutgoingTransformationNodeVisitor.INSTANCE),
        new NodeInterfacePair(node, iface));
  }
}
