package org.batfish.datamodel.bgp;

import static org.batfish.datamodel.bgp.VniConfig.importRtPatternForAnyAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.jupiter.api.Test;

/** Tests of {@link Layer3VniConfig} */
class Layer3VniConfigTest {

  @Test
  void testEquals() {
    Layer3VniConfig.Builder builder =
        Layer3VniConfig.builder()
            .setVni(1)
            .setVrf("v")
            .setRouteDistinguisher(RouteDistinguisher.from(65555L, 1))
            .setRouteTarget(ExtendedCommunity.of(0, 1, 1))
            .setImportRouteTarget(importRtPatternForAnyAs(1))
            .setAdvertiseV4Unicast(false);
    Layer3VniConfig vni = builder.build();
    new EqualsTester()
        .addEqualityGroup(vni, vni, builder.build())
        .addEqualityGroup(builder.setVni(2).build())
        .addEqualityGroup(builder.setVrf("v2").build())
        .addEqualityGroup(builder.setRouteDistinguisher(RouteDistinguisher.from(65555L, 2)).build())
        .addEqualityGroup(builder.setRouteTarget(ExtendedCommunity.of(0, 2, 1)).build())
        .addEqualityGroup(builder.setImportRouteTarget(importRtPatternForAnyAs(2)).build())
        .addEqualityGroup(builder.setAdvertiseV4Unicast(true).build())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    Layer3VniConfig vni =
        Layer3VniConfig.builder()
            .setVni(1)
            .setVrf("v")
            .setRouteDistinguisher(RouteDistinguisher.from(65555L, 1))
            .setRouteTarget(ExtendedCommunity.of(0, 1, 1))
            .setImportRouteTarget(importRtPatternForAnyAs(1))
            .setAdvertiseV4Unicast(false)
            .build();
    assertThat(SerializationUtils.clone(vni), equalTo(vni));
  }

  @Test
  void testJsonSerialization() {
    Layer3VniConfig vni =
        Layer3VniConfig.builder()
            .setVni(1)
            .setVrf("v")
            .setRouteDistinguisher(RouteDistinguisher.from(65555L, 1))
            .setRouteTarget(ExtendedCommunity.of(0, 1, 1))
            .setImportRouteTarget(importRtPatternForAnyAs(1))
            .setAdvertiseV4Unicast(false)
            .build();
    assertThat(BatfishObjectMapper.clone(vni, Layer3VniConfig.class), equalTo(vni));
  }

  @Test
  void testImportRtPatternForAnyAs() {
    assertThat(importRtPatternForAnyAs(1), equalTo("^\\d+:1$"));
    assertThat(importRtPatternForAnyAs(2), equalTo("^\\d+:2$"));
  }

  @Test
  void testImportRtPatternForAnyAsTooLow() {
    assertThrows(IllegalArgumentException.class, () -> importRtPatternForAnyAs(-1));
  }

  @Test
  void testImportRtPatternForAnyAsTooHigh() {
    assertThrows(IllegalArgumentException.class, () -> importRtPatternForAnyAs(16777216));
  }

  @Test
  void testInvalidPattern() {
    assertThrows(IllegalArgumentException.class, () -> importRtPatternForAnyAs(16777216));
  }
}
