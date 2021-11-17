package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.Test;

public class Bgpv4ToEvpnVrfLeakConfigTest {
  @Test
  public void testJavaSerialization() {
    Bgpv4ToEvpnVrfLeakConfig val =
        Bgpv4ToEvpnVrfLeakConfig.builder()
            .setImportFromVrf("vrf1")
            .setSrcVrfRouteDistinguisher(RouteDistinguisher.from(0, 1L))
            .setAttachRouteTargets(ExtendedCommunity.target(1, 2))
            .build();
    assertThat(SerializationUtils.clone(val), equalTo(val));
  }

  @Test
  public void testJsonSerialization() {
    Bgpv4ToEvpnVrfLeakConfig val =
        Bgpv4ToEvpnVrfLeakConfig.builder()
            .setImportFromVrf("vrf1")
            .setSrcVrfRouteDistinguisher(RouteDistinguisher.from(0, 1L))
            .setAttachRouteTargets(ExtendedCommunity.target(1, 2))
            .build();
    assertThat(BatfishObjectMapper.clone(val, Bgpv4ToEvpnVrfLeakConfig.class), equalTo(val));
  }

  @Test
  public void testEquals() {
    Bgpv4ToEvpnVrfLeakConfig.Builder b =
        Bgpv4ToEvpnVrfLeakConfig.builder()
            .setImportFromVrf("vrf1")
            .setSrcVrfRouteDistinguisher(RouteDistinguisher.from(0L, 1));
    Bgpv4ToEvpnVrfLeakConfig val = b.build();
    new EqualsTester()
        .addEqualityGroup(val, b.build())
        .addEqualityGroup(b.setImportFromVrf("vrf2").build())
        .addEqualityGroup(b.setSrcVrfRouteDistinguisher(RouteDistinguisher.from(2, 3L)).build())
        .addEqualityGroup(b.setAttachRouteTargets(ExtendedCommunity.target(1, 2)).build())
        .testEquals();
  }
}
