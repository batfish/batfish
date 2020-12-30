package org.batfish.datamodel;

import static org.junit.Assert.assertThat;

import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.eigrp.EigrpMetricVersion;
import org.batfish.datamodel.eigrp.WideMetric;
import org.hamcrest.Matchers;
import org.junit.Test;

public class EigrpInternalRouteTest {

  @Test
  public void testToBuilder() {
    EigrpInternalRoute r =
        EigrpInternalRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setEigrpMetricVersion(EigrpMetricVersion.V1)
            .setEigrpMetric(
                WideMetric.builder()
                    .setValues(EigrpMetricValues.builder().setBandwidth(1E8).setDelay(1D).build())
                    .build())
            .setProcessAsn(2L)
            .build();
    assertThat(r.toBuilder().build(), Matchers.equalTo(r));
  }
}
