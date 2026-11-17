package org.batfish.representation.frr;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

/** Tests for {@link org.batfish.representation.frr.BgpNeighborSourceInterface} */
class BgpNeighborSourceInterfaceTest {

  @Test
  void testEquals() {
    BgpNeighborSourceInterface bgpNeighborSourceInterface = new BgpNeighborSourceInterface("lo");

    new EqualsTester()
        .addEqualityGroup(bgpNeighborSourceInterface, new BgpNeighborSourceInterface("lo"))
        .addEqualityGroup(new BgpNeighborSourceInterface("iface"))
        .testEquals();
  }
}
