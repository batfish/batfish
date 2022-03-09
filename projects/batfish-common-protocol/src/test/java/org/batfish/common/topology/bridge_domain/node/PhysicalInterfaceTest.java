package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Test of {@link PhysicalInterface}. */
public final class PhysicalInterfaceTest {

  @Test
  public void testEquals() {
    PhysicalInterface obj = new PhysicalInterface(NodeInterfacePair.of("a", "b"));
    new EqualsTester()
        .addEqualityGroup(obj, new PhysicalInterface(NodeInterfacePair.of("a", "b")))
        .addEqualityGroup(new PhysicalInterface(NodeInterfacePair.of("a", "c")))
        .testEquals();
  }
}
