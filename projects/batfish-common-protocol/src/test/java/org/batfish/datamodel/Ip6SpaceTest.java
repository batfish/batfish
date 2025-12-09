package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.Ip6SpaceMatchers.containsIp6;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class Ip6SpaceTest {
  @Test
  public void testIp6Space() {
    Ip6WildcardSetIp6Space any =
        Ip6WildcardSetIp6Space.builder().including(Ip6Wildcard.ANY).build();
    Ip6WildcardSetIp6Space justMax =
        Ip6WildcardSetIp6Space.builder().including(new Ip6Wildcard(Ip6.MAX)).build();
    Ip6WildcardSetIp6Space anyExceptMax =
        Ip6WildcardSetIp6Space.builder()
            .including(Ip6Wildcard.ANY)
            .excluding(new Ip6Wildcard(Ip6.MAX))
            .build();
    Ip6WildcardSetIp6Space none1 = Ip6WildcardSetIp6Space.builder().build();
    Ip6WildcardSetIp6Space none2 =
        Ip6WildcardSetIp6Space.builder()
            .including(Ip6Wildcard.ANY)
            .excluding(Ip6Wildcard.ANY)
            .build();
    Ip6WildcardSetIp6Space someButNotMax =
        Ip6WildcardSetIp6Space.builder().including(Ip6Wildcard.parse("1:2:3:4::1")).build();

    assertThat(any, containsIp6(Ip6.MAX));
    assertThat(justMax, containsIp6(Ip6.MAX));
    assertThat(anyExceptMax, not(containsIp6(Ip6.MAX)));
    assertThat(none1, not(containsIp6(Ip6.MAX)));
    assertThat(none2, not(containsIp6(Ip6.MAX)));
    assertThat(someButNotMax, not(containsIp6(Ip6.MAX)));
  }

  @Test
  public void testIp6SpaceJacksonSerialization() {
    for (Ip6Space ip6Space :
        ImmutableList.of(
            Prefix6.parse("1:1:1:1::1/64").toIp6Space(),
            new Ip6SpaceReference("s", "d"),
            new Ip6SpaceReference("s", null))) {
      Ip6Space deserializedIp6Space = BatfishObjectMapper.clone(ip6Space, Ip6Space.class);
      assertThat(ip6Space, equalTo(deserializedIp6Space));
      assertThat(ip6Space, equalTo(SerializationUtils.clone(ip6Space)));
    }
  }
}
