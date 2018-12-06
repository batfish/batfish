package org.batfish.datamodel;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.junit.Test;

/** Test for {@link OspfExternalType1Route} */
public class OspfExternalType1RouteTest {

  @Test
  public void testEquals() {

    OspfExternalType2Route.Builder b =
        OspfExternalRoute.builder()
            .setNetwork(new Prefix(new Ip("1.1.1.1"), 32))
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
}
