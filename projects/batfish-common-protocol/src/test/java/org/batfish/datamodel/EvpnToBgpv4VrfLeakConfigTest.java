package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class EvpnToBgpv4VrfLeakConfigTest {

  @Test
  public void testJavaSerialization() {
    EvpnToBgpv4VrfLeakConfig val =
        EvpnToBgpv4VrfLeakConfig.builder().setImportFromVrf("v").setImportPolicy("p").build();
    assertThat(SerializationUtils.clone(val), equalTo(val));
  }

  @Test
  public void testJsonSerialization() {
    EvpnToBgpv4VrfLeakConfig val =
        EvpnToBgpv4VrfLeakConfig.builder().setImportFromVrf("v").setImportPolicy("p").build();
    assertThat(BatfishObjectMapper.clone(val, EvpnToBgpv4VrfLeakConfig.class), equalTo(val));
  }

  @Test
  public void testEquals() {
    EvpnToBgpv4VrfLeakConfig.Builder b = EvpnToBgpv4VrfLeakConfig.builder().setImportFromVrf("v1");
    EvpnToBgpv4VrfLeakConfig val = b.build();
    new EqualsTester()
        .addEqualityGroup(val, b.build())
        .addEqualityGroup(b.setImportFromVrf("v2").build())
        .addEqualityGroup(b.setImportPolicy("p").build())
        .testEquals();
  }
}
