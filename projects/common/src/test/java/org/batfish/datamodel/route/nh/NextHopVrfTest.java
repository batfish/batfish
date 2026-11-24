package org.batfish.datamodel.route.nh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link NextHopVrf}. */
final class NextHopVrfTest {

  @Test
  void testJavaSerialiation() {
    NextHopVrf obj = NextHopVrf.of("foo");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testJacksonSerialization() {
    NextHopVrf obj = NextHopVrf.of("foo");
    assertThat(BatfishObjectMapper.clone(obj, NextHop.class), equalTo(obj));
  }

  @Test
  void testEquals() {
    NextHopVrf obj = NextHopVrf.of("foo");
    new EqualsTester()
        .addEqualityGroup(obj, NextHopVrf.of("foo"))
        .addEqualityGroup(NextHopVrf.of("bar"))
        .testEquals();
  }
}
