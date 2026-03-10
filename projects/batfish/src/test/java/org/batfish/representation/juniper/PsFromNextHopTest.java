package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.representation.juniper.PsFromNextHop.Hop;
import org.junit.Test;

public class PsFromNextHopTest {
  @Test
  public void testEquals() {
    Hop hop4a = Hop.of(Ip.ZERO);
    Hop hop4b = Hop.of(Ip.FIRST_MULTICAST_IP);
    Hop hop6a = Hop.of(Ip6.ZERO);
    Hop hop6b = Hop.of(Ip6.MAX);
    new EqualsTester()
        .addEqualityGroup(hop4a, Hop.of(Ip.ZERO))
        .addEqualityGroup(hop4b)
        .addEqualityGroup(hop6a)
        .addEqualityGroup(hop6b)
        .testEquals();

    new EqualsTester()
        .addEqualityGroup(new PsFromNextHop(hop4a), new PsFromNextHop(hop4a))
        .addEqualityGroup(new PsFromNextHop(hop4b))
        .testEquals();
  }

  @Test
  public void testToString() {
    Hop hop4 = Hop.of(Ip.FIRST_MULTICAST_IP);
    assertThat(hop4.toString(), equalTo("224.0.0.0"));
    assertThat(Hop.of(Ip6.MAX).toString(), equalTo(Ip6.MAX.toString()));

    assertThat(new PsFromNextHop(hop4).toString(), equalTo("PsFromNextHop{hop=224.0.0.0}"));
  }

  @Test
  public void testSerialization() {
    PsFromNextHop hop = new PsFromNextHop(Hop.of(Ip.FIRST_MULTICAST_IP));
    assertThat(SerializationUtils.clone(hop), equalTo(hop));
  }
}
