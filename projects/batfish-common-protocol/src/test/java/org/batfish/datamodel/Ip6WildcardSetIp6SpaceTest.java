package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.Ip6SpaceMatchers.containsIp6;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

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
}
