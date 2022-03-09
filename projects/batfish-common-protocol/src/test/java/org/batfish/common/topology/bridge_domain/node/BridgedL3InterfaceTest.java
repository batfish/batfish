package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Test of {@link BridgedL3Interface}. */
public final class BridgedL3InterfaceTest {

  @Test
  public void testEquals() {
    BridgedL3Interface obj = new BridgedL3Interface(NodeInterfacePair.of("a", "b"));
    new EqualsTester()
        .addEqualityGroup(obj, new BridgedL3Interface(NodeInterfacePair.of("a", "b")))
        .addEqualityGroup(new BridgedL3Interface(NodeInterfacePair.of("a", "c")))
        .testEquals();
  }
}
