package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.common.topology.bridge_domain.node.BridgeDomain.Id;
import org.junit.Test;

/** Test of {@link NonVlanAwareBridgeDomain}. */
public final class NonVlanAwareBridgeDomainTest {

  @Test
  public void testEquals() {
    NonVlanAwareBridgeDomain obj = new NonVlanAwareBridgeDomain(Id.of("a", "b"));
    new EqualsTester()
        .addEqualityGroup(obj, new NonVlanAwareBridgeDomain(Id.of("a", "b")))
        .addEqualityGroup(new NonVlanAwareBridgeDomain(Id.of("a", "c")))
        .addEqualityGroup(new NonVlanAwareBridgeDomain(Id.of("d", "b")))
        .testEquals();
  }
}
