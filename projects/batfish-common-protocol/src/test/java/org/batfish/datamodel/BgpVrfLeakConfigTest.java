package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.Test;

/** Tests of {@link BgpVrfLeakConfig} */
public class BgpVrfLeakConfigTest {

  @Test
  public void testJavaSerialization() {
    BgpVrfLeakConfig val =
        BgpVrfLeakConfig.builder()
            .setImportFromVrf("vrf1")
            .setImportPolicy("policy")
            .setAdmin(5)
            .setAttachRouteTargets(ExtendedCommunity.target(1, 2))
            .setWeight(3)
            .build();
    assertThat(SerializationUtils.clone(val), equalTo(val));
  }

  @Test
  public void testJsonSerialization() {
    BgpVrfLeakConfig val =
        BgpVrfLeakConfig.builder()
            .setImportFromVrf("vrf1")
            .setImportPolicy("policy")
            .setAdmin(5)
            .setAttachRouteTargets(ExtendedCommunity.target(1, 2))
            .setWeight(3)
            .build();
    assertThat(BatfishObjectMapper.clone(val, BgpVrfLeakConfig.class), equalTo(val));
  }

  @Test
  public void testEquals() {
    BgpVrfLeakConfig.Builder b =
        BgpVrfLeakConfig.builder()
            .setImportFromVrf("vrf1")
            .setImportPolicy("policy")
            .setAdmin(1)
            .setWeight(2);
    BgpVrfLeakConfig val = b.build();
    new EqualsTester()
        .addEqualityGroup(val, b.build())
        .addEqualityGroup(b.setImportFromVrf("vrf2").build())
        .addEqualityGroup(b.setImportPolicy("policy2").build())
        .addEqualityGroup(b.setAdmin(10).build())
        .addEqualityGroup(b.setAttachRouteTargets(ExtendedCommunity.target(1, 2)).build())
        .addEqualityGroup(b.setWeight(20).build())
        .testEquals();
  }
}
