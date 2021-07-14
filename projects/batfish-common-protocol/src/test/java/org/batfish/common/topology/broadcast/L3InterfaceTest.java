package org.batfish.common.topology.broadcast;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public class L3InterfaceTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new L3Interface(NodeInterfacePair.of("h", "i")),
            new L3Interface(NodeInterfacePair.of("h", "i")))
        .addEqualityGroup(new L3Interface(NodeInterfacePair.of("h", "i2")))
        .addEqualityGroup(new PhysicalInterface(NodeInterfacePair.of("h", "i")))
        .testEquals();
  }
}
