package org.batfish.datamodel;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.Optional;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
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
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Prefix6.parse("1:1:1:1::1/64").toIp6Space(),
            Prefix6.parse("1:1:1:1::1/64").toIp6Space())
        .addEqualityGroup(Prefix6.parse("1:1:1:2::1/64"))
        .testEquals();
  }

  @Test
  public void testTryParse() {
    for (String s :
        ImmutableList.of(
            "abcd",
            "1.1.1.1",
            "1:1:1:1::1",
            "1:1:1:1::1/-1",
            "1:1:1:1::1/129",
            "11111:1:1:1::1/64",
            "fffg:1:1:1::1/64")) {
      assertThat(Prefix6.tryParse(s), equalTo(Optional.empty()));
    }
  }

  @Test
  public void testSerialization() {
    PrefixIp6Space original = new PrefixIp6Space(Prefix6.parse("2001:db8::/32"));
    assertThat(BatfishObjectMapper.clone(original, Ip6Space.class), equalTo(original));
    assertThat(SerializationUtils.clone(original), equalTo(original));
  }
}
