package org.batfish.vendor.a10.representation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link ServerTargetToIp}. */
public class ServerTargetToIpTest {
  @Test
  public void testVisitAddress() {
    Ip ip = Ip.parse("10.10.10.10");
    ServerTargetAddress sta = new ServerTargetAddress(ip);
    assertThat(ServerTargetToIp.INSTANCE.visit(sta), equalTo(ip));
  }
}
