package org.batfish.datamodel.vendor_family.cumulus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;
import org.junit.Test;

/** Test of {@link InterfaceClagSettings} */
public final class InterfaceClagSettingsTest {

  @Test
  public void testEquals() {
    InterfaceClagSettings.Builder builder = InterfaceClagSettings.builder();
    InterfaceClagSettings c1 = builder.build();

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(c1, c1, builder.build())
        .addEqualityGroup(builder.setBackupIp(Ip.ZERO).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setBackupIpVrf("foo").build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setPeerIp(Ip.ZERO).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setPriority(5).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setSysMac(MacAddress.of(5L)).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setPeerLinkLocal(true))
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    InterfaceClagSettings c =
        InterfaceClagSettings.builder()
            .setBackupIp(Ip.ZERO)
            .setBackupIpVrf("foo")
            .setPeerIp(Ip.ZERO)
            .setPeerLinkLocal(true)
            .setPriority(5)
            .setSysMac(MacAddress.of(5L))
            .build();

    assertThat(BatfishObjectMapper.clone(c, InterfaceClagSettings.class), equalTo(c));
  }

  @Test
  public void testJavaSerialization() {
    InterfaceClagSettings c =
        InterfaceClagSettings.builder()
            .setBackupIp(Ip.ZERO)
            .setBackupIpVrf("foo")
            .setPeerIp(Ip.ZERO)
            .setPeerLinkLocal(true)
            .setPriority(5)
            .setSysMac(MacAddress.of(5L))
            .build();

    assertThat(SerializationUtils.clone(c), equalTo(c));
  }
}
