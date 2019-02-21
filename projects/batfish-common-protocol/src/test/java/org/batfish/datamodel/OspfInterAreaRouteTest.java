package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests of {@link OspfInterAreaRoute} */
public class OspfInterAreaRouteTest {

  @Test
  public void testToBuilder() {
    OspfInternalRoute r =
        OspfInternalRoute.builder()
            .setProtocol(RoutingProtocol.OSPF_IA)
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setMetric(1L)
            .setArea(2L)
            .build();
    assertThat(r.toBuilder().build(), equalTo(r));
    assertThat(r.toBuilder().build() instanceof OspfInterAreaRoute, equalTo(true));
  }
}
