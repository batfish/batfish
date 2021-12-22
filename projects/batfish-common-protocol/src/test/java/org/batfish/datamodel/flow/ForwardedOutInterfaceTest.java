package org.batfish.datamodel.flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link ForwardedOutInterface}. */
public final class ForwardedOutInterfaceTest {

  @Test
  public void testJacksonSerialization() {
    ForwardedOutInterface obj = ForwardedOutInterface.of(Ip.parse("1.1.1.1"), "a");
    assertThat(BatfishObjectMapper.clone(obj, ForwardingDetail.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    ForwardedOutInterface obj = ForwardedOutInterface.of(Ip.parse("1.1.1.1"), "a");
    new EqualsTester()
        .addEqualityGroup(obj, ForwardedOutInterface.of(Ip.parse("1.1.1.1"), "a"))
        .addEqualityGroup(ForwardedOutInterface.of(Ip.parse("2.2.2.2"), "a"))
        .addEqualityGroup(ForwardedOutInterface.of(Ip.parse("1.1.1.1"), "b"))
        .testEquals();
  }
}
