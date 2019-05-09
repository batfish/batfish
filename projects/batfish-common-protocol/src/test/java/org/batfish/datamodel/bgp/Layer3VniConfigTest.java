package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.Test;

/** Tests of {@link Layer3VniConfig} */
public class Layer3VniConfigTest {
  @Test
  public void testEquals() {
    Layer3VniConfig vni =
        new Layer3VniConfig(
            1, "v", RouteDistinguisher.from(65555L, 1), ExtendedCommunity.of(0, 1, 1), false);
    new EqualsTester()
        .addEqualityGroup(
            vni,
            vni,
            new Layer3VniConfig(
                1, "v", RouteDistinguisher.from(65555L, 1), ExtendedCommunity.of(0, 1, 1), false))
        .addEqualityGroup(
            new Layer3VniConfig(
                2, "v", RouteDistinguisher.from(65555L, 1), ExtendedCommunity.of(0, 1, 1), false))
        .addEqualityGroup(
            new Layer3VniConfig(
                1, "v2", RouteDistinguisher.from(65555L, 1), ExtendedCommunity.of(0, 1, 1), false))
        .addEqualityGroup(
            new Layer3VniConfig(
                1, "v", RouteDistinguisher.from(65555L, 2), ExtendedCommunity.of(0, 1, 1), false))
        .addEqualityGroup(
            new Layer3VniConfig(
                1, "v", RouteDistinguisher.from(65555L, 0), ExtendedCommunity.of(0, 2, 1), false))
        .addEqualityGroup(
            new Layer3VniConfig(
                1, "v", RouteDistinguisher.from(65555L, 0), ExtendedCommunity.of(0, 1, 1), true))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    Layer3VniConfig vni =
        new Layer3VniConfig(
            1, "v", RouteDistinguisher.from(65555L, 1), ExtendedCommunity.of(0, 1, 1), false);
    assertThat(SerializationUtils.clone(vni), equalTo(vni));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    Layer3VniConfig vni =
        new Layer3VniConfig(
            1, "v", RouteDistinguisher.from(65555L, 1), ExtendedCommunity.of(0, 1, 1), false);
    assertThat(BatfishObjectMapper.clone(vni, Layer3VniConfig.class), equalTo(vni));
  }
}
