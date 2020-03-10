package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.eigrp.WideMetric;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link org.batfish.datamodel.EigrpRoute} */
@RunWith(JUnit4.class)
public class EigrpRouteTest {

  @Test
  public void testEigrpInternalRouteClone() {
    EigrpInternalRoute route =
        EigrpInternalRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setEigrpMetric(
                WideMetric.builder()
                    .setValues(EigrpMetricValues.builder().setBandwidth(1000).setDelay(2).build())
                    .build())
            .setProcessAsn(1L)
            .build();

    assertThat(BatfishObjectMapper.clone(route, EigrpInternalRoute.class), equalTo(route));
  }

  @Test
  public void testEigrpExternalRouteClone() {
    EigrpExternalRoute route =
        EigrpExternalRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setEigrpMetric(
                WideMetric.builder()
                    .setValues(EigrpMetricValues.builder().setBandwidth(1000).setDelay(2).build())
                    .build())
            .setProcessAsn(1L)
            .setDestinationAsn(2L)
            .build();

    assertThat(BatfishObjectMapper.clone(route, EigrpExternalRoute.class), equalTo(route));
  }
}
