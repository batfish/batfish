package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.common.topology.bridge_domain.node.BridgeDomain.Id;
import org.junit.Test;

/** Test of {@link VlanAwareBridgeDomain}. */
public final class VlanAwareBridgeDomainTest {

  @Test
  public void testEquals() {
    VlanAwareBridgeDomain obj = new VlanAwareBridgeDomain(Id.of("a", "b"));
    new EqualsTester()
        .addEqualityGroup(obj, new VlanAwareBridgeDomain(Id.of("a", "b")))
        .addEqualityGroup(new VlanAwareBridgeDomain(Id.of("a", "c")))
        .testEquals();
  }
}
