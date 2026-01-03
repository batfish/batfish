package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.Ip6SpaceMatchers.containsIp6;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class Ip6WildcardSetIp6SpaceTest {
  private final Ip6Space _ip6Space =
      Ip6WildcardSetIp6Space.builder()
          .including(Ip6Wildcard.parse("1:1:1:0::0/64"), Ip6Wildcard.parse("1:1:2:0::0/64"))
          .excluding(Ip6Wildcard.parse("1:1:1:1::0/64"))
          .build();

  @Test
  public void testContainsIp() {
    assertThat(_ip6Space, containsIp6(Ip6.parse("1:1:1:0::0")));
    assertThat(_ip6Space, not(containsIp6(Ip6.parse("1:1:1:1::0"))));
    assertThat(_ip6Space, containsIp6(Ip6.parse("1:1:2:0::0")));
    assertThat(_ip6Space, not(containsIp6(Ip6.parse("1:1:3:0::0"))));
  }

  @Test
  public void testSerialization() {
    Ip6WildcardSetIp6Space original =
        Ip6WildcardSetIp6Space.builder()
            .including(Ip6Wildcard.parse("2001:db8::/32"))
            .excluding(Ip6Wildcard.parse("2001:db8:1::/48"))
            .build();
    assertThat(BatfishObjectMapper.clone(original, Ip6Space.class), equalTo(original));
    assertThat(SerializationUtils.clone(original), equalTo(original));
  }

  @Test
  public void testEquals() {
    Ip6Wildcard wildcard1 = Ip6Wildcard.parse("2001:db8::/32");
    Ip6Wildcard wildcard2 = Ip6Wildcard.parse("2001:db8:1::/48");
    Ip6Wildcard wildcard3 = Ip6Wildcard.parse("2001:db8:2::/48");

    new EqualsTester()
        .addEqualityGroup(
            Ip6WildcardSetIp6Space.builder().including(wildcard1).build(),
            Ip6WildcardSetIp6Space.builder().including(wildcard1).build())
        .addEqualityGroup(Ip6WildcardSetIp6Space.builder().including(wildcard2).build())
        .addEqualityGroup(
            Ip6WildcardSetIp6Space.builder().including(wildcard1).excluding(wildcard2).build())
        .addEqualityGroup(
            Ip6WildcardSetIp6Space.builder().including(wildcard1).excluding(wildcard3).build())
        .testEquals();
  }
}
