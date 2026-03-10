package org.batfish.common.topology.broadcast;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public class PhysicalInterfaceTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new PhysicalInterface(NodeInterfacePair.of("h", "i")),
            new PhysicalInterface(NodeInterfacePair.of("h", "i")))
        .addEqualityGroup(new PhysicalInterface(NodeInterfacePair.of("h", "i2")))
        .testEquals();
  }
}
