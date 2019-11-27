package org.batfish.representation.cumulus;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests for {@link org.batfish.representation.cumulus.BgpNeighborSourceInterface} */
public class BgpNeighborSourceInterfaceTest {

  @Test
  public void testEquals() {
    BgpNeighborSourceInterface bgpNeighborSourceInterface = new BgpNeighborSourceInterface("lo");

    new EqualsTester()
        .addEqualityGroup(bgpNeighborSourceInterface, new BgpNeighborSourceInterface("lo"))
        .addEqualityGroup(new BgpNeighborSourceInterface("iface"))
        .testEquals();
  }
}
