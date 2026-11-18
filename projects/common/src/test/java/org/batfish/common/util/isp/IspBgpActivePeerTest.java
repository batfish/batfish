package org.batfish.common.util.isp;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

class IspBgpActivePeerTest {

  @Test
  void testEquals() {
    // test variation of fields unique to unnumbered peers -- the inherited fields are tested in the
    // parent class
    new EqualsTester()
        .addEqualityGroup(
            new IspBgpActivePeer(Ip.ZERO, Ip.FIRST_MULTICAST_IP, 1L, 2L, false),
            new IspBgpActivePeer(Ip.ZERO, Ip.FIRST_MULTICAST_IP, 1L, 2L, false))
        .addEqualityGroup(new IspBgpActivePeer(Ip.MAX, Ip.FIRST_MULTICAST_IP, 1L, 2L, false))
        .addEqualityGroup(new IspBgpActivePeer(Ip.ZERO, Ip.MAX, 1L, 2L, false))
        .testEquals();
  }
}
