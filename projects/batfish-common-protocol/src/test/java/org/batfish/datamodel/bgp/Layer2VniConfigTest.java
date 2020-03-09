package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.Test;

/** Tests of {@link Layer2VniConfig} */
public class Layer2VniConfigTest {
  @Test
  public void testEquals() {

    Layer2VniConfig.Builder builder =
        Layer2VniConfig.builder()
            .setVni(1)
            .setVrf("v")
            .setRouteDistinguisher(RouteDistinguisher.from(65555L, 1))
            .setRouteTarget(ExtendedCommunity.of(0, 1, 1));
    Layer2VniConfig vni = builder.build();
    new EqualsTester()
        .addEqualityGroup(vni, vni, builder.build())
        .addEqualityGroup(builder.setVrf("v2").build())
        .addEqualityGroup(builder.setRouteDistinguisher(RouteDistinguisher.from(65555L, 2)).build())
        .addEqualityGroup(builder.setRouteTarget(ExtendedCommunity.of(0, 2, 2)).build())
        .addEqualityGroup(builder.setImportRouteTarget("^1:1$").build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    Layer2VniConfig vni =
        Layer2VniConfig.builder()
            .setVni(1)
            .setVrf("v")
            .setRouteDistinguisher(RouteDistinguisher.from(65555L, 1))
            .setRouteTarget(ExtendedCommunity.of(0, 1, 1))
            .build();
    assertThat(SerializationUtils.clone(vni), equalTo(vni));
  }

  @Test
  public void testJsonSerialization() {
    Layer2VniConfig vni =
        Layer2VniConfig.builder()
            .setVni(1)
            .setVrf("v")
            .setRouteDistinguisher(RouteDistinguisher.from(65555L, 1))
            .setRouteTarget(ExtendedCommunity.of(0, 1, 1))
            .build();
    assertThat(BatfishObjectMapper.clone(vni, Layer2VniConfig.class), equalTo(vni));
  }
}
