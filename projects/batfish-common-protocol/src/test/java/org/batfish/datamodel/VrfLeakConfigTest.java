package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class VrfLeakConfigTest {
  private static final VrfLeakConfig BGP_LEAK_CONFIG =
      VrfLeakConfig.builder(true)
          .addBgpVrfLeakConfig(
              BgpVrfLeakConfig.builder()
                  .setImportFromVrf("v")
                  .setImportPolicy("p")
                  .setAdmin(0)
                  .setWeight(0)
                  .build())
          .build();
  private static final VrfLeakConfig MAIN_RIB_LEAK_CONFIG =
      VrfLeakConfig.builder(false)
          .addMainRibVrfLeakConfig(
              MainRibVrfLeakConfig.builder().setImportFromVrf("v").setImportPolicy("p").build())
          .build();

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(BGP_LEAK_CONFIG), equalTo(BGP_LEAK_CONFIG));
    assertThat(SerializationUtils.clone(MAIN_RIB_LEAK_CONFIG), equalTo(MAIN_RIB_LEAK_CONFIG));
  }

  @Test
  public void testJsonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(BGP_LEAK_CONFIG, VrfLeakConfig.class), equalTo(BGP_LEAK_CONFIG));
    assertThat(
        BatfishObjectMapper.clone(MAIN_RIB_LEAK_CONFIG, VrfLeakConfig.class),
        equalTo(MAIN_RIB_LEAK_CONFIG));
  }

  @Test
  public void testEquals() {
    VrfLeakConfig.Builder bgpBuilder = VrfLeakConfig.builder(true);
    VrfLeakConfig.Builder mainRibBuilder = VrfLeakConfig.builder(false);
    VrfLeakConfig bgp = bgpBuilder.build();
    VrfLeakConfig mainRib = mainRibBuilder.build();
    new EqualsTester()
        .addEqualityGroup(bgp, bgp, bgpBuilder.build())
        .addEqualityGroup(mainRib, mainRib, mainRibBuilder.build())
        .addEqualityGroup(
            bgpBuilder.addBgpVrfLeakConfig(
                BgpVrfLeakConfig.builder()
                    .setImportFromVrf("v")
                    .setImportPolicy("p")
                    .setAdmin(0)
                    .setWeight(0)
                    .build()))
        .addEqualityGroup(
            mainRibBuilder.addMainRibVrfLeakConfig(
                MainRibVrfLeakConfig.builder().setImportFromVrf("v").setImportPolicy("p").build()))
        .testEquals();
  }
}
