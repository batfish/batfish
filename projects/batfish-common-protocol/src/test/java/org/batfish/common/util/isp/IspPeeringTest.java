package org.batfish.common.util.isp;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class IspPeeringTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new IspPeering(1, 2), new IspPeering(1, 2), new IspPeering(2, 1))
        .addEqualityGroup(new IspPeering(10, 2))
        .addEqualityGroup(new IspPeering(1, 20))
        .testEquals();
  }
}
