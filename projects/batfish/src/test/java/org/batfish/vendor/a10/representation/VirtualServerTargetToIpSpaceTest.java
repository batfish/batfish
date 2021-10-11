package org.batfish.vendor.a10.representation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link VirtualServerTargetToIpSpace}. */
public class VirtualServerTargetToIpSpaceTest {
  @Test
  public void testVisitAddress() {
    Ip ip = Ip.parse("10.10.10.10");
    VirtualServerTargetAddress vsta = new VirtualServerTargetAddress(ip);
    assertThat(VirtualServerTargetToIpSpace.INSTANCE.visit(vsta), equalTo(ip.toIpSpace()));
  }
}
