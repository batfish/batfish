package org.batfish.datamodel.bgp.community;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link Community} */
class CommunityTest {
  @Test
  void testJsonDeserialization() throws IOException {
    assertThat(
        BatfishObjectMapper.mapper().readValue("1", Community.class),
        equalTo(StandardCommunity.of(1)));
    assertThat(
        BatfishObjectMapper.mapper().readValue("65555", Community.class),
        equalTo(StandardCommunity.of(65555)));
    assertThat(
        BatfishObjectMapper.mapper().readValue("\"1:1\"", Community.class),
        equalTo(StandardCommunity.of(1, 1)));
    assertThat(
        BatfishObjectMapper.mapper().readValue("\"1:1:1\"", Community.class),
        equalTo(ExtendedCommunity.of(1, 1, 1)));
    assertThat(
        BatfishObjectMapper.mapper().readValue("\"large:1:1:1\"", Community.class),
        equalTo(LargeCommunity.of(1, 1, 1)));
  }
}
