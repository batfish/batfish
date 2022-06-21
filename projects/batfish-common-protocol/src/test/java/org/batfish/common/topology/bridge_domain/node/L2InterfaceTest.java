package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Test of {@link L2Interface}. */
public final class L2InterfaceTest {

  @Test
  public void testEquals() {
    L2Interface obj = new L2Interface(NodeInterfacePair.of("a", "b"));
    new EqualsTester()
        .addEqualityGroup(obj, new L2Interface(NodeInterfacePair.of("a", "b")))
        .addEqualityGroup(new L2Interface(NodeInterfacePair.of("a", "c")))
        .testEquals();
  }
}
