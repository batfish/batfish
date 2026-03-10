package org.batfish.common.util.isp;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class IspBgpPeerTest {

  @Test
  public void testEquals() {
    // test via unnumbered child class
    new EqualsTester()
        .addEqualityGroup(
            new IspBgpUnnumberedPeer("iface", 1L, 2L, false),
            new IspBgpUnnumberedPeer("iface", 1L, 2L, false))
        .addEqualityGroup(new IspBgpUnnumberedPeer("iface", 9L, 2L, false))
        .addEqualityGroup(new IspBgpUnnumberedPeer("iface", 1L, 9L, false))
        .addEqualityGroup(new IspBgpUnnumberedPeer("iface", 1L, 2L, true))
        .testEquals();
  }
}
