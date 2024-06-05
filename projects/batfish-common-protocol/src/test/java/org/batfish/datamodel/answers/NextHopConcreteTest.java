package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.junit.Test;

/** Test of {@link NextHopConcrete}. */
public final class NextHopConcreteTest {

  @Test
  public void testJacksonSerialization() {
    NextHopConcrete obj = new NextHopConcrete(NextHopIp.of(Ip.parse("1.1.1.1")));
    assertThat(BatfishObjectMapper.clone(obj, NextHopResult.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    NextHopConcrete obj1 = new NextHopConcrete(NextHopIp.of(Ip.parse("1.1.1.1")));
    NextHopConcrete obj2 = new NextHopConcrete(NextHopIp.of(Ip.parse("1.1.1.1")));
    NextHopConcrete obj3 = new NextHopConcrete(NextHopIp.of(Ip.parse("0.1.1.1")));

    new EqualsTester().addEqualityGroup(obj1, obj2).addEqualityGroup(obj3).testEquals();
  }
}
