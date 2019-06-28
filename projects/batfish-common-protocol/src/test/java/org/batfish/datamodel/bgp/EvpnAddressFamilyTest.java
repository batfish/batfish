package org.batfish.datamodel.bgp;

import static org.batfish.datamodel.bgp.EvpnAddressFamily.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.EvpnAddressFamily.Builder;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.Test;

/** Tests of {@link EvpnAddressFamily} */
public class EvpnAddressFamilyTest {
  @Test
  public void testEquals() {
    Builder builder =
        builder()
            .setL2Vnis(ImmutableSet.of())
            .setL3Vnis(ImmutableSet.of())
            .setPropagateUnmatched(false);
    EvpnAddressFamily af = builder.build();
    new EqualsTester()
        .addEqualityGroup(af, af, builder.build())
        .addEqualityGroup(
            builder
                .setL2Vnis(
                    ImmutableSet.of(
                        Layer2VniConfig.builder()
                            .setVni(1)
                            .setVrf("v")
                            .setRouteDistinguisher(RouteDistinguisher.from(1L, 1))
                            .setRouteTarget(ExtendedCommunity.of(0, 1, 1))
                            .build()))
                .build())
        .addEqualityGroup(
            builder
                .setL3Vnis(
                    ImmutableSet.of(
                        Layer3VniConfig.builder()
                            .setVni(1)
                            .setVrf("v")
                            .setRouteDistinguisher(RouteDistinguisher.from(1L, 1))
                            .setRouteTarget(ExtendedCommunity.of(0, 1, 1))
                            .setImportRouteTarget(VniConfig.importRtPatternForAnyAs(1))
                            .setAdvertiseV4Unicast(false)
                            .build()))
                .build())
        .addEqualityGroup(builder.setPropagateUnmatched(true).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    EvpnAddressFamily af =
        builder()
            .setL2Vnis(ImmutableSet.of())
            .setL3Vnis(ImmutableSet.of())
            .setPropagateUnmatched(true)
            .build();
    assertThat(SerializationUtils.clone(af), equalTo(af));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    Builder builder =
        builder()
            .setL2Vnis(ImmutableSet.of())
            .setL3Vnis(ImmutableSet.of())
            .setPropagateUnmatched(true);
    EvpnAddressFamily af1 = builder.build();
    assertThat(BatfishObjectMapper.clone(af1, EvpnAddressFamily.class), equalTo(af1));
    EvpnAddressFamily af2 = builder.setPropagateUnmatched(false).build();
    assertThat(BatfishObjectMapper.clone(af2, EvpnAddressFamily.class), equalTo(af2));
  }
}
