package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.junit.Test;

/** Test for {@link OspfExternalType2Route} */
public class OspfExternalType2RouteTest {

  @Test
  public void testEquals() {
    OspfExternalType2Route.Builder b =
        OspfExternalRoute.builder()
            .setNetwork(Ip.parse("1.1.1.1").toPrefix())
            .setNextHop(NextHopDiscard.instance())
            .setAdmin(1)
            .setMetric(1)
            .setLsaMetric(1)
            .setArea(1)
            .setCostToAdvertiser(1)
            .setAdvertiser("")
            .setOspfMetricType(OspfMetricType.E2);

    OspfExternalRoute r1 = b.build();
    OspfExternalRoute r1DiffObj = b.build();
    OspfExternalRoute r2 = b.setCostToAdvertiser(2).build();
    OspfExternalRoute r2t1 = b.setOspfMetricType(OspfMetricType.E1).build();

    new EqualsTester()
        .addEqualityGroup(r1, r1DiffObj)
        .addEqualityGroup(r2)
        .addEqualityGroup(r2t1)
        .testEquals();
  }

  @Test
  public void testToBuilder() {
    OspfExternalRoute r =
        OspfExternalRoute.builder()
            .setOspfMetricType(OspfMetricType.E2)
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setMetric(1L)
            .setLsaMetric(2L)
            .setCostToAdvertiser(3L)
            .setAdvertiser("advertiser")
            .setArea(4L)
            .build();
    assertThat(r.toBuilder().build(), equalTo(r));
    assertThat(r.toBuilder().build() instanceof OspfExternalType2Route, equalTo(true));
  }
}
