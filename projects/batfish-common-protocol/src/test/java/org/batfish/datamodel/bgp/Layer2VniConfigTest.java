package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.Test;

/** Tests of {@link Layer2VniConfig} */
public class Layer2VniConfigTest {
  @Test
  public void testEquals() {

    Layer2VniConfig vni =
        new Layer2VniConfig(
            1, "v", RouteDistinguisher.from(65555L, 1), ExtendedCommunity.of(0, 1, 1));
    new EqualsTester()
        .addEqualityGroup(
            vni,
            vni,
            new Layer2VniConfig(
                1, "v", RouteDistinguisher.from(65555L, 1), ExtendedCommunity.of(0, 1, 1)))
        .addEqualityGroup(
            new Layer2VniConfig(
                1, "v2", RouteDistinguisher.from(65555L, 2), ExtendedCommunity.of(0, 1, 1)))
        .addEqualityGroup(
            new Layer2VniConfig(
                1, "v", RouteDistinguisher.from(65555L, 2), ExtendedCommunity.of(0, 2, 1)))
        .addEqualityGroup(
            new Layer2VniConfig(
                1, "v", RouteDistinguisher.from(65555L, 0), ExtendedCommunity.of(0, 1, 1)))
        .addEqualityGroup(
            new Layer2VniConfig(
                1, "v", RouteDistinguisher.from(65555L, 0), ExtendedCommunity.of(0, 1, 2)))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    Layer2VniConfig vni =
        new Layer2VniConfig(
            1, "v", RouteDistinguisher.from(65555L, 1), ExtendedCommunity.of(0, 1, 1));
    assertThat(SerializationUtils.clone(vni), equalTo(vni));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    Layer2VniConfig vni =
        new Layer2VniConfig(
            1, "v", RouteDistinguisher.from(65555L, 1), ExtendedCommunity.of(0, 1, 1));
    assertThat(BatfishObjectMapper.clone(vni, Layer2VniConfig.class), equalTo(vni));
  }
}
