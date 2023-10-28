package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class Ip6SpaceTest {
  @Test
  public void testIp6Space() {}

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
