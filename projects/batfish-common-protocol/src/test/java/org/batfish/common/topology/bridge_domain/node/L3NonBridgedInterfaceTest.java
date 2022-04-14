package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Test of {@link L3NonBridgedInterface}. */
public final class L3NonBridgedInterfaceTest {

  @Test
  public void testEquals() {
    L3NonBridgedInterface obj = new L3NonBridgedInterface(NodeInterfacePair.of("a", "b"));
    new EqualsTester()
        .addEqualityGroup(obj, new L3NonBridgedInterface(NodeInterfacePair.of("a", "b")))
        .addEqualityGroup(new L3NonBridgedInterface(NodeInterfacePair.of("a", "c")))
        .testEquals();
  }
}
