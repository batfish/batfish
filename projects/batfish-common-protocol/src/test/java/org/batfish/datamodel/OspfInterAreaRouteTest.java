package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link OspfInterAreaRoute} */
public class OspfInterAreaRouteTest {

  @Test
  public void testEquals() {
    OspfInterAreaRoute.Builder builder =
        OspfInterAreaRoute.builder().setArea(0).setNetwork(Prefix.ZERO);
    OspfInterAreaRoute r = builder.build();
    new EqualsTester()
        .addEqualityGroup(r, r, builder.build())
        .addEqualityGroup(builder.setNetwork(Prefix.parse("1.1.1.1/32")).build())
        .addEqualityGroup(builder.setAdmin(1000).build())
        .addEqualityGroup(builder.setArea(2L).build())
        .addEqualityGroup(builder.setMetric(20L).build())
        .addEqualityGroup(builder.setNextHopIp(Ip.parse("8.8.8.8")).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    OspfInterAreaRoute r = OspfInterAreaRoute.builder().setArea(0).setNetwork(Prefix.ZERO).build();
    assertThat(SerializationUtils.clone(r), equalTo(r));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    OspfInterAreaRoute r = OspfInterAreaRoute.builder().setArea(0).setNetwork(Prefix.ZERO).build();
    assertThat(BatfishObjectMapper.clone(r, OspfInterAreaRoute.class), equalTo(r));
  }

  @Test
  public void testToBuilder() {
    OspfInterAreaRoute r =
        OspfInterAreaRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setMetric(1L)
            .setArea(2L)
            .build();
    assertThat(r.toBuilder().build(), equalTo(r));
  }
}
