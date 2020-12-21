package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.VrfLeakingConfig.Builder;
import org.junit.Test;

/** Tests of {@link VrfLeakingConfig} */
public class VrfLeakingConfigTest {

  @Test
  public void testJavaSerialization() {
    VrfLeakingConfig val =
        VrfLeakingConfig.builder()
            .setImportFromVrf("vrf1")
            .setImportPolicy("policy")
            .setLeakAsBgp(false)
            .build();
    assertThat(SerializationUtils.clone(val), equalTo(val));
  }

  @Test
  public void testEquals() {
    Builder b =
        VrfLeakingConfig.builder()
            .setImportFromVrf("vrf1")
            .setImportPolicy("policy")
            .setLeakAsBgp(false);
    VrfLeakingConfig val = b.build();
    new EqualsTester()
        .addEqualityGroup(val, val, b.build())
        .addEqualityGroup(b.setImportFromVrf("vrf2").build())
        .addEqualityGroup(b.setImportPolicy("policy2").build())
        .addEqualityGroup(b.setLeakAsBgp(true).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
