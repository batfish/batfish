package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class PrefixIpSpaceTest {

  @Test
  public void testOfIp() {
    Prefix prefix = Prefix.strict("1.0.0.0/32");
    assertEquals(PrefixIpSpace.of(prefix), new IpIpSpace(Ip.parse("1.0.0.0")));
  }

  @Test
  public void testOfPrefix() {
    Prefix prefix = Prefix.strict("1.0.0.0/8");
    assertEquals(PrefixIpSpace.of(prefix), new PrefixIpSpace(prefix));
  }
}
