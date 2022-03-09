package org.batfish.common.topology.bridge_domain.node;

import static org.batfish.common.topology.bridge_domain.node.BridgeDomain.newNonVlanAwareBridge;
import static org.batfish.common.topology.bridge_domain.node.BridgeDomain.newVlanAwareBridge;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Test of {@link BridgeDomain}. */
public final class BridgeDomainTest {

  @Test
  public void testEquals() {
    BridgeDomain obj = newNonVlanAwareBridge("a", "b");
    new EqualsTester()
        .addEqualityGroup(obj, newNonVlanAwareBridge("a", "b"))
        .addEqualityGroup(newNonVlanAwareBridge("c", "b"))
        .addEqualityGroup(newNonVlanAwareBridge("a", "c"))
        .addEqualityGroup(newVlanAwareBridge("a"))
        .testEquals();
  }
}
