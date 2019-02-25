package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.junit.Test;

/** Test for {@link OspfExternalType1Route} */
public class OspfExternalType1RouteTest {

  @Test
  public void testEquals() {

    OspfExternalType2Route.Builder b =
        OspfExternalRoute.builder()
            .setNetwork(Prefix.create(Ip.parse("1.1.1.1"), 32))
            .setNextHopIp(Ip.ZERO)
            .setAdmin(1)
            .setMetric(1)
            .setLsaMetric(1)
            .setArea(1)
            .setCostToAdvertiser(1)
            .setAdvertiser("")
            .setOspfMetricType(OspfMetricType.E1);

    OspfExternalRoute r1 = b.build();
    OspfExternalRoute r1DiffObj = b.build();
    OspfExternalRoute r2 = b.setCostToAdvertiser(2).build();
    OspfExternalRoute r2t2 = b.setOspfMetricType(OspfMetricType.E2).build();

    new EqualsTester()
        .addEqualityGroup(r1, r1DiffObj)
        .addEqualityGroup(r2)
        .addEqualityGroup(r2t2)
        .testEquals();
  }

  @Test
  public void testToBuilder() {
    OspfExternalRoute r =
        OspfExternalRoute.builder()
            .setOspfMetricType(OspfMetricType.E1)
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setMetric(1L)
            .setLsaMetric(2L)
            .setCostToAdvertiser(3L)
            .setAdvertiser("advertiser")
            .setArea(4L)
            .build();
    assertThat(r.toBuilder().build(), equalTo(r));
    assertThat(r.toBuilder().build() instanceof OspfExternalType1Route, equalTo(true));
  }
}
