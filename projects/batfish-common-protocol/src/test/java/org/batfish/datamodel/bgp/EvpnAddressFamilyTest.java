package org.batfish.datamodel.bgp;

import static org.batfish.datamodel.bgp.EvpnAddressFamily.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
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
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder().setSendCommunity(true).build())
                .build())
        .addEqualityGroup(builder.setExportPolicy("export").build())
        .addEqualityGroup(builder.setImportPolicy("import").build())
        .addEqualityGroup(builder.setExportPolicySources(ImmutableSortedSet.of("foo")).build())
        .addEqualityGroup(builder.setImportPolicySources(ImmutableSortedSet.of("bar")).build())
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
        .addEqualityGroup(builder.setRouteReflectorClient(true))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    EvpnAddressFamily af =
        builder()
            .setAddressFamilyCapabilities(
                AddressFamilyCapabilities.builder().setSendCommunity(true).build())
            .setExportPolicy("export")
            .setImportPolicy("import")
            .setExportPolicySources(ImmutableSortedSet.of("foo"))
            .setImportPolicySources(ImmutableSortedSet.of("bar"))
            .setL2Vnis(ImmutableSet.of())
            .setL3Vnis(ImmutableSet.of())
            .setRouteReflectorClient(true)
            .setPropagateUnmatched(true)
            .build();
    assertThat(SerializationUtils.clone(af), equalTo(af));
  }

  @Test
  public void testJsonSerialization() {
    Builder builder =
        builder()
            .setAddressFamilyCapabilities(
                AddressFamilyCapabilities.builder().setSendCommunity(true).build())
            .setExportPolicy("export")
            .setImportPolicy("import")
            .setExportPolicySources(ImmutableSortedSet.of("foo"))
            .setImportPolicySources(ImmutableSortedSet.of("bar"))
            .setL2Vnis(ImmutableSet.of())
            .setL3Vnis(ImmutableSet.of())
            .setRouteReflectorClient(true)
            .setPropagateUnmatched(true);
    EvpnAddressFamily af1 = builder.build();
    assertThat(BatfishObjectMapper.clone(af1, EvpnAddressFamily.class), equalTo(af1));
    EvpnAddressFamily af2 = builder.setPropagateUnmatched(false).build();
    assertThat(BatfishObjectMapper.clone(af2, EvpnAddressFamily.class), equalTo(af2));
  }
}
