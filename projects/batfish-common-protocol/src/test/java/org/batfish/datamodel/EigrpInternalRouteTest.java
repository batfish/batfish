package org.batfish.datamodel;

import static org.junit.Assert.assertThat;

import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpProcessMode;
import org.hamcrest.Matchers;
import org.junit.Test;

public class EigrpInternalRouteTest {

  @Test
  public void testToBuilder() {
    EigrpInternalRoute r =
        EigrpInternalRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setMetric(1L)
            .setEigrpMetric(
                EigrpMetric.builder()
                    .setBandwidth(1E8)
                    .setDelay(1D)
                    .setMode(EigrpProcessMode.CLASSIC)
                    .build())
            .setProcessAsn(2L)
            .build();
    assertThat(r.toBuilder().build(), Matchers.equalTo(r));
  }
}
