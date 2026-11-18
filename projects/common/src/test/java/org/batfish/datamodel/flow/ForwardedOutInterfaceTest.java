package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

/** Test of {@link ForwardedOutInterface}. */
final class ForwardedOutInterfaceTest {

  @Test
  void testJacksonSerialization() {
    ForwardedOutInterface obj = ForwardedOutInterface.of("a", Ip.parse("1.1.1.1"));
    assertThat(BatfishObjectMapper.clone(obj, ForwardingDetail.class), equalTo(obj));
  }

  @Test
  void testEquals() {
    ForwardedOutInterface obj = ForwardedOutInterface.of("a", Ip.parse("1.1.1.1"));
    new EqualsTester()
        .addEqualityGroup(obj, ForwardedOutInterface.of("a", Ip.parse("1.1.1.1")))
        .addEqualityGroup(ForwardedOutInterface.of("a", Ip.parse("2.2.2.2")))
        .addEqualityGroup(ForwardedOutInterface.of("b", Ip.parse("1.1.1.1")))
        .testEquals();
  }
}
