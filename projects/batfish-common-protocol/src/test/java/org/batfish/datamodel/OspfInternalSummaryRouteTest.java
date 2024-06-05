package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link OspfInternalSummaryRoute} */
public class OspfInternalSummaryRouteTest {

  @Test
  public void testEquals() {
    OspfInternalSummaryRoute.Builder builder =
        OspfInternalSummaryRoute.builder().setArea(0).setNetwork(Prefix.ZERO);
    OspfInternalSummaryRoute r = builder.build();
    new EqualsTester()
        .addEqualityGroup(r, builder.build())
        .addEqualityGroup(builder.setNetwork(Prefix.parse("1.1.1.1/32")).build())
        .addEqualityGroup(builder.setAdmin(100).build())
        .addEqualityGroup(builder.setArea(2L).build())
        .addEqualityGroup(builder.setMetric(20L).build())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    OspfInternalSummaryRoute r =
        OspfInternalSummaryRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setAdmin(100)
            .setArea(0)
            .setMetric(1L)
            .build();
    assertThat(SerializationUtils.clone(r), equalTo(r));
  }

  @Test
  public void testJsonSerialization() {
    OspfInternalSummaryRoute r =
        OspfInternalSummaryRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setAdmin(100)
            .setArea(0)
            .setMetric(1L)
            .build();
    assertThat(BatfishObjectMapper.clone(r, OspfInternalSummaryRoute.class), equalTo(r));
  }

  @Test
  public void testToBuilder() {
    OspfInternalSummaryRoute r =
        OspfInternalSummaryRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAdmin(100)
            .setArea(2L)
            .setMetric(1L)
            .build();
    assertThat(r.toBuilder().build(), equalTo(r));
  }
}
