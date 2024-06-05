package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link NextHopSelf}. */
public final class NextHopSelfTest {

  @Test
  public void testJacksonSerialization() {
    NextHopSelf obj = NextHopSelf.instance();
    assertThat(BatfishObjectMapper.clone(obj, NextHopResult.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    NextHopSelf obj = NextHopSelf.instance();
    new EqualsTester().addEqualityGroup(obj, NextHopSelf.instance()).testEquals();
  }
}
