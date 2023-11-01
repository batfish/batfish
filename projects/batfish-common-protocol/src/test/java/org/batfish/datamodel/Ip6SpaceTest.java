package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.Ip6SpaceMatchers.containsIp6;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
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
  public void testIp6SpaceJacksonSerialization() throws IOException {
    Ip6 ip6 = Ip6.parse("1:1:1:1::1");
    Prefix6 p = Prefix6.create(ip6, 64);
    Ip6Space prefixIp6Space = p.toIp6Space();
    for (Ip6Space ip6Space : ImmutableList.of(prefixIp6Space)) {
      String jsonString = BatfishObjectMapper.writePrettyString(ip6Space);
      Ip6Space deserializedIp6Space =
          BatfishObjectMapper.mapper().readValue(jsonString, Ip6Space.class);

      /* Ip6Space should be equal to deserialized version */
      assertThat(ip6Space, equalTo(deserializedIp6Space));
    }
  }
}
