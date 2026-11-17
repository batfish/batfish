package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link NextHopSelf}. */
final class NextHopSelfTest {

  @Test
  void testJacksonSerialization() {
    NextHopSelf obj = NextHopSelf.instance();
    assertThat(BatfishObjectMapper.clone(obj, NextHopResult.class), equalTo(obj));
  }

  @Test
  void testEquals() {
    NextHopSelf obj = NextHopSelf.instance();
    new EqualsTester().addEqualityGroup(obj, NextHopSelf.instance()).testEquals();
  }
}
