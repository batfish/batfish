package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Test of {@link NonBridgedL3Interface}. */
public final class NonBridgedL3InterfaceTest {

  @Test
  public void testEquals() {
    NonBridgedL3Interface obj = new NonBridgedL3Interface(NodeInterfacePair.of("a", "b"));
    new EqualsTester()
        .addEqualityGroup(obj, new NonBridgedL3Interface(NodeInterfacePair.of("a", "b")))
        .addEqualityGroup(new NonBridgedL3Interface(NodeInterfacePair.of("a", "c")))
        .testEquals();
  }
}
