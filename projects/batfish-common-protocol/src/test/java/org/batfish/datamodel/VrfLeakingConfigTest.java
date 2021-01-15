package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.VrfLeakingConfig.BgpLeakConfig;
import org.batfish.datamodel.VrfLeakingConfig.Builder;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.Test;

/** Tests of {@link VrfLeakingConfig} */
public class VrfLeakingConfigTest {

  @Test
  public void testJavaSerialization() {
    VrfLeakingConfig val =
        VrfLeakingConfig.builder()
            .setImportFromVrf("vrf1")
            .setImportPolicy("policy")
            .setBgpLeakConfig(new BgpLeakConfig(ExtendedCommunity.target(1, 2)))
            .build();
    assertThat(SerializationUtils.clone(val), equalTo(val));
  }

  @Test
  public void testJsonSerialization() {
    VrfLeakingConfig val =
        VrfLeakingConfig.builder()
            .setImportFromVrf("vrf1")
            .setImportPolicy("policy")
            .setBgpLeakConfig(new BgpLeakConfig(ExtendedCommunity.target(1, 2)))
            .build();
    assertThat(BatfishObjectMapper.clone(val, VrfLeakingConfig.class), equalTo(val));
  }

  @Test
  public void testEquals() {
    Builder b = VrfLeakingConfig.builder().setImportFromVrf("vrf1").setImportPolicy("policy");
    VrfLeakingConfig val = b.build();
    new EqualsTester()
        .addEqualityGroup(val, val, b.build())
        .addEqualityGroup(b.setImportFromVrf("vrf2").build())
        .addEqualityGroup(b.setImportPolicy("policy2").build())
        .addEqualityGroup(
            b.setBgpLeakConfig(new BgpLeakConfig(ExtendedCommunity.target(1, 2))).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
