package org.batfish.vendor.a10.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

/** Test of {@link VirtualServerTargetToIpSpace}. */
class VirtualServerTargetToIpSpaceTest {
  @Test
  void testVisitAddress() {
    Ip ip = Ip.parse("10.10.10.10");
    VirtualServerTargetAddress vsta = new VirtualServerTargetAddress(ip);
    assertThat(VirtualServerTargetToIpSpace.INSTANCE.visit(vsta), equalTo(ip.toIpSpace()));
  }
}
