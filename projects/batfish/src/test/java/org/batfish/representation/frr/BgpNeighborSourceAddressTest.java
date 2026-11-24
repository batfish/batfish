package org.batfish.representation.frr;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

/** Tests for {@link BgpNeighborSourceAddress} */
class BgpNeighborSourceAddressTest {

  @Test
  void testEquals() {
    BgpNeighborSourceAddress bgpNeighborSourceAddress =
        new BgpNeighborSourceAddress(Ip.parse("1.1.1.1"));

    new EqualsTester()
        .addEqualityGroup(
            bgpNeighborSourceAddress, new BgpNeighborSourceAddress(Ip.parse("1.1.1.1")))
        .addEqualityGroup(new BgpNeighborSourceAddress(Ip.parse("3.3.3.3")))
        .testEquals();
  }
}
