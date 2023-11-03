package org.batfish.datamodel;

import static junit.framework.TestCase.assertEquals;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class PrefixIp6SpaceTest {
  @Test
  public void testCompareSameClass() {
    PrefixIp6Space prefixIp6Space1 = new PrefixIp6Space(new Prefix6(Ip6.parse("1:1:1:1::1"), 64));
    PrefixIp6Space prefixIp6Space2 = new PrefixIp6Space(new Prefix6(Ip6.parse("1:1:1:1::1"), 64));
    PrefixIp6Space prefixIp6Space3 = new PrefixIp6Space(new Prefix6(Ip6.parse("1:1:1:2::1"), 64));

    assertEquals(0, prefixIp6Space1.compareSameClass(prefixIp6Space2));
    assertEquals(-1, prefixIp6Space1.compareSameClass(prefixIp6Space3));
  }

  @Test
  public void testExprEquals() {
    PrefixIp6Space prefixIp6Space1 = new PrefixIp6Space(new Prefix6(Ip6.parse("1:1:1:1::1"), 64));
    PrefixIp6Space prefixIp6Space2 = new PrefixIp6Space(new Prefix6(Ip6.parse("1:1:1:1::1"), 64));
    PrefixIp6Space prefixIp6Space3 = new PrefixIp6Space(new Prefix6(Ip6.parse("1:1:1:2::1"), 64));

    new EqualsTester()
        .addEqualityGroup(prefixIp6Space1, prefixIp6Space2)
        .addEqualityGroup(prefixIp6Space3)
        .testEquals();
  }
}
