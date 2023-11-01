package org.batfish.datamodel;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;

public class PrefixIp6SpaceTest {
  @Test
  public void testCompareSameClass() {
    PrefixIp6Space prefixIp6Space1 =
        new PrefixIp6Space(Prefix6.create(Ip6.parse("1:1:1:1::1"), 64));
    PrefixIp6Space prefixIp6Space2 =
        new PrefixIp6Space(Prefix6.create(Ip6.parse("1:1:1:1::1"), 64));
    PrefixIp6Space prefixIp6Space3 =
        new PrefixIp6Space(Prefix6.create(Ip6.parse("1:1:1:2::1"), 64));

    assertEquals(0, prefixIp6Space1.compareSameClass(prefixIp6Space2));
    assertEquals(-1, prefixIp6Space1.compareSameClass(prefixIp6Space3));
  }

  @Test
  public void testExprEquals() {
    PrefixIp6Space prefixIp6Space1 =
        new PrefixIp6Space(Prefix6.create(Ip6.parse("1:1:1:1::1"), 64));
    PrefixIp6Space prefixIp6Space2 =
        new PrefixIp6Space(Prefix6.create(Ip6.parse("1:1:1:1::1"), 64));
    PrefixIp6Space prefixIp6Space3 =
        new PrefixIp6Space(Prefix6.create(Ip6.parse("1:1:1:2::1"), 64));

    assertTrue(prefixIp6Space1.exprEquals(prefixIp6Space2));
    assertFalse(prefixIp6Space1.exprEquals(prefixIp6Space3));
  }
}
