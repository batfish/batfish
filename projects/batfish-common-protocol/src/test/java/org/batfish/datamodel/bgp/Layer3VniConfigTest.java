package org.batfish.datamodel.bgp;

import static org.batfish.datamodel.bgp.VniConfig.importRtPatternForAnyAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link Layer3VniConfig} */
public class Layer3VniConfigTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testEquals() {
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
  public void testJavaSerialization() {
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
  public void testJsonSerialization() {
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
  public void testImportRtPatternForAnyAs() {
    assertThat(importRtPatternForAnyAs(1), equalTo("^\\d+:1$"));
    assertThat(importRtPatternForAnyAs(2), equalTo("^\\d+:2$"));
  }

  @Test
  public void testImportRtPatternForAnyAsTooLow() {
    _thrown.expect(IllegalArgumentException.class);
    importRtPatternForAnyAs(-1);
  }

  @Test
  public void testImportRtPatternForAnyAsTooHigh() {
    _thrown.expect(IllegalArgumentException.class);
    importRtPatternForAnyAs(16777216);
  }

  @Test
  public void testInvalidPattern() {
    _thrown.expect(IllegalArgumentException.class);
    importRtPatternForAnyAs(16777216);
  }
}
