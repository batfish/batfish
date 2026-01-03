package org.batfish.datamodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Test for {@link AbstractIp6SpaceContainsIp}. */
public class AbstractIp6SpaceContainsIpTest {
  private static final Ip6 IP1 = Ip6.parse("1:1:1:1::1");
  private static final Ip6 IP2 = Ip6.parse("2:2:2:2::2");

  private static AbstractIp6SpaceContainsIp containsIp(Ip6 ip6) {
    return new AbstractIp6SpaceContainsIp(ip6) {
      @Override
      public Boolean visitIp6SpaceReference(Ip6SpaceReference ip6SpaceReference) {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Test
  public void testVisitPrefixIpSpace() {
    PrefixIp6Space ip6Space = (PrefixIp6Space) Prefix6.create(IP1, 31).toIp6Space();
    assertTrue(containsIp(IP1).visitPrefixIp6Space(ip6Space));
    assertFalse(containsIp(IP2).visitPrefixIp6Space(ip6Space));
  }
}
