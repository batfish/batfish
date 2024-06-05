package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.junit.Test;

/** Tests of {@link OspfInterAreaRoute} */
public class OspfInterAreaRouteTest {

  @Test
  public void testEquals() {
    OspfInterAreaRoute.Builder builder =
        OspfInterAreaRoute.builder()
            .setArea(0)
            .setNextHop(NextHopDiscard.instance())
            .setNetwork(Prefix.ZERO);
    OspfInterAreaRoute r = builder.build();
    new EqualsTester()
        .addEqualityGroup(r, r, builder.build())
        .addEqualityGroup(builder.setNetwork(Prefix.parse("1.1.1.1/32")).build())
        .addEqualityGroup(builder.setAdmin(100).build())
        .addEqualityGroup(builder.setArea(2L).build())
        .addEqualityGroup(builder.setMetric(20L).build())
        .addEqualityGroup(
            builder.setNextHop(NextHopInterface.of("e0", Ip.parse("8.8.8.8"))).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    OspfInterAreaRoute r =
        OspfInterAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .build();
    assertThat(SerializationUtils.clone(r), equalTo(r));
  }

  @Test
  public void testJsonSerialization() {
    OspfInterAreaRoute r =
        OspfInterAreaRoute.builder()
            .setArea(0)
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .build();
    assertThat(BatfishObjectMapper.clone(r, OspfInterAreaRoute.class), equalTo(r));
  }

  @Test
  public void testToBuilder() {
    OspfInterAreaRoute r =
        OspfInterAreaRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setMetric(1L)
            .setArea(2L)
            .build();
    assertThat(r.toBuilder().build(), equalTo(r));
  }
}
