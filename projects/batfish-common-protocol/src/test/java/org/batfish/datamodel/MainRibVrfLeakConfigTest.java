package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.MainRibVrfLeakConfig.Builder;
import org.junit.Test;

/** Tests of {@link MainRibVrfLeakConfig} */
public class MainRibVrfLeakConfigTest {

  @Test
  public void testJavaSerialization() {
    MainRibVrfLeakConfig val =
        MainRibVrfLeakConfig.builder().setImportFromVrf("vrf1").setImportPolicy("policy").build();
    assertThat(SerializationUtils.clone(val), equalTo(val));
  }

  @Test
  public void testJsonSerialization() {
    MainRibVrfLeakConfig val =
        MainRibVrfLeakConfig.builder().setImportFromVrf("vrf1").setImportPolicy("policy").build();
    assertThat(BatfishObjectMapper.clone(val, MainRibVrfLeakConfig.class), equalTo(val));
  }

  @Test
  public void testEquals() {
    Builder b = MainRibVrfLeakConfig.builder().setImportFromVrf("vrf1").setImportPolicy("policy");
    MainRibVrfLeakConfig val = b.build();
    new EqualsTester()
        .addEqualityGroup(val, b.build())
        .addEqualityGroup(b.setImportFromVrf("vrf2").build())
        .addEqualityGroup(b.setImportPolicy("policy2").build())
        .testEquals();
  }
}
