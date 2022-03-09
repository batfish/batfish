package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Test of {@link DisconnectedL3Interface}. */
public final class DisconnectedL3InterfaceTest {

  @Test
  public void testEquals() {
    DisconnectedL3Interface obj = new DisconnectedL3Interface(NodeInterfacePair.of("a", "b"));
    new EqualsTester()
        .addEqualityGroup(obj, new DisconnectedL3Interface(NodeInterfacePair.of("a", "b")))
        .addEqualityGroup(new DisconnectedL3Interface(NodeInterfacePair.of("a", "c")))
        .testEquals();
  }
}
