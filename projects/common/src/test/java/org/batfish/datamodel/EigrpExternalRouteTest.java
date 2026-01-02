package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.eigrp.ClassicMetric;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.eigrp.EigrpMetricVersion;
import org.junit.Test;

public class EigrpExternalRouteTest {

  @Test
  public void testToBuilder() {
    EigrpExternalRoute r =
        EigrpExternalRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setMetric(1L)
            .setEigrpMetricVersion(EigrpMetricVersion.V1)
            .setDestinationAsn(1L)
            .setEigrpMetric(
                ClassicMetric.builder()
                    .setValues(
                        EigrpMetricValues.builder()
                            .setBandwidth((long) 1E8)
                            .setDelay((long) 1D)
                            .build())
                    .build())
            .setProcessAsn(2L)
            .build();
    assertThat(r.toBuilder().build(), equalTo(r));
  }
}
