package org.batfish.datamodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests of {@link BgpSessionProperties} */
public class BgpSessionPropertiesTest {
  @Test
  public void testSessionCreation() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    BgpActivePeerConfig p1 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setPeerAddress(ip2)
            .setAdvertiseInactive(true)
            .build();
    BgpActivePeerConfig p2 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip2)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setPeerAddress(ip1)
            .setAdvertiseInactive(false)
            .build();
    BgpSessionProperties session = BgpSessionProperties.from(p1, p2, false);
    assertTrue(session.getAdvertiseInactive());
    assertFalse(session.getAdvertiseExternal());
  }
}
