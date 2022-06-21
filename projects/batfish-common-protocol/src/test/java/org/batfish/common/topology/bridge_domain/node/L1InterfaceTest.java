package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Test of {@link L1Interface}. */
public final class L1InterfaceTest {

  @Test
  public void testEquals() {
    L1Interface obj = new L1Interface(NodeInterfacePair.of("a", "b"));
    new EqualsTester()
        .addEqualityGroup(obj, new L1Interface(NodeInterfacePair.of("a", "b")))
        .addEqualityGroup(new L1Interface(NodeInterfacePair.of("a", "c")))
        .testEquals();
  }
}
