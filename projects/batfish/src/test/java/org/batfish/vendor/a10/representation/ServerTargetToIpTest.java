package org.batfish.vendor.a10.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

/** Test of {@link ServerTargetToIp}. */
class ServerTargetToIpTest {
  @Test
  void testVisitAddress() {
    Ip ip = Ip.parse("10.10.10.10");
    ServerTargetAddress sta = new ServerTargetAddress(ip);
    assertThat(ServerTargetToIp.INSTANCE.visit(sta), equalTo(ip));
  }
}
