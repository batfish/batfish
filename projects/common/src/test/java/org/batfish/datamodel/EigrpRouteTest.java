package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.eigrp.EigrpMetricVersion;
import org.batfish.datamodel.eigrp.WideMetric;
import org.junit.jupiter.api.Test;

/** Tests of {@link org.batfish.datamodel.EigrpRoute} */
class EigrpRouteTest {

  @Test
  void testEigrpInternalRouteClone() {
    EigrpInternalRoute route =
        EigrpInternalRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setEigrpMetric(
                WideMetric.builder()
                    .setValues(EigrpMetricValues.builder().setBandwidth(1000).setDelay(2).build())
                    .build())
            .setEigrpMetricVersion(EigrpMetricVersion.V1)
            .setProcessAsn(1L)
            .build();

    assertThat(BatfishObjectMapper.clone(route, EigrpInternalRoute.class), equalTo(route));
  }

  @Test
  void testEigrpExternalRouteClone() {
    EigrpExternalRoute route =
        EigrpExternalRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setEigrpMetric(
                WideMetric.builder()
                    .setValues(EigrpMetricValues.builder().setBandwidth(1000).setDelay(2).build())
                    .build())
            .setEigrpMetricVersion(EigrpMetricVersion.V1)
            .setProcessAsn(1L)
            .setDestinationAsn(2L)
            .build();

    assertThat(BatfishObjectMapper.clone(route, EigrpExternalRoute.class), equalTo(route));
  }
}
