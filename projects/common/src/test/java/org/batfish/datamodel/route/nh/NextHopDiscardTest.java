package org.batfish.datamodel.route.nh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link NextHopDiscard}. */
public final class NextHopDiscardTest {

  @Test
  public void testJavaSerialiation() {
    NextHopDiscard obj = NextHopDiscard.instance();
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    NextHopDiscard obj = NextHopDiscard.instance();
    assertThat(BatfishObjectMapper.clone(obj, NextHop.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    NextHopDiscard obj = NextHopDiscard.instance();
    new EqualsTester().addEqualityGroup(obj, NextHopDiscard.instance()).testEquals();
  }
}
