package org.batfish.common.util.isp;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class IspBgpUnnumberedTest {

  @Test
  public void testEquals() {
    // test variation of fields unique to unnumbered peers -- the inherited fields are tested in the
    // parent class
    new EqualsTester()
        .addEqualityGroup(
            new IspBgpUnnumberedPeer("iface", 1L, 2L, false),
            new IspBgpUnnumberedPeer("iface", 1L, 2L, false))
        .addEqualityGroup(new IspBgpUnnumberedPeer("other", 1L, 2L, false))
        .testEquals();
  }
}
