package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpProcessMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link org.batfish.datamodel.EigrpRoute} */
@RunWith(JUnit4.class)
public class EigrpRouteTest {

  @Test
  public void testEigrpInternalRouteClone() throws IOException {
    EigrpInternalRoute route =
        EigrpInternalRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setEigrpMetric(
                EigrpMetric.builder()
                    .setBandwidth(1000.0)
                    .setDelay(2.0)
                    .setMode(EigrpProcessMode.NAMED)
                    .build())
            .setProcessAsn(1L)
            .build();

    assertThat(BatfishObjectMapper.clone(route, EigrpInternalRoute.class), equalTo(route));
  }

  @Test
  public void testEigrpExternalRouteClone() throws IOException {
    EigrpExternalRoute route =
        EigrpExternalRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setEigrpMetric(
                EigrpMetric.builder()
                    .setBandwidth(1000.0)
                    .setDelay(2.0)
                    .setMode(EigrpProcessMode.NAMED)
                    .build())
            .setProcessAsn(1L)
            .setDestinationAsn(2L)
            .build();

    assertThat(BatfishObjectMapper.clone(route, EigrpExternalRoute.class), equalTo(route));
  }
}
