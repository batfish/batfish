package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.batfish.common.topology.bridge_domain.node.BridgeDomain.Id;
import org.junit.Test;

/** Test of {@link BridgeDomain}. */
public final class BridgeDomainTest {

  @Test
  public void testIdEquals() {
    Id obj = Id.of("a", "b");
    new EqualsTester()
        .addEqualityGroup(obj, Id.of("a", "b"))
        .addEqualityGroup(Id.of("c", "b"))
        .addEqualityGroup(Id.of("a", "d"))
        .testEquals();
  }
}
