package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Test of {@link L3BridgedInterface}. */
public final class L3BridgedInterfaceTest {

  @Test
  public void testEquals() {
    L3BridgedInterface obj = new L3BridgedInterface(NodeInterfacePair.of("a", "b"));
    new EqualsTester()
        .addEqualityGroup(obj, new L3BridgedInterface(NodeInterfacePair.of("a", "b")))
        .addEqualityGroup(new L3BridgedInterface(NodeInterfacePair.of("a", "c")))
        .testEquals();
  }
}
