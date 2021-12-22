package org.batfish.datamodel.flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link Discarded}. */
public final class DiscardedTest {

  @Test
  public void testJacksonSerialization() {
    Discarded obj = Discarded.instance();
    assertThat(BatfishObjectMapper.clone(obj, ForwardingDetail.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    Discarded obj = Discarded.instance();
    new EqualsTester().addEqualityGroup(obj, Discarded.instance()).testEquals();
  }
}
