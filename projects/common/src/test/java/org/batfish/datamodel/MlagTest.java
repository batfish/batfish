package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link Mlag} */
final class MlagTest {

  @Test
  void testEquals() {
    Mlag.Builder b =
        Mlag.builder()
            .setId("id")
            .setLocalInterface("Vlan1")
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setPeerInterface("Eth1");
    new EqualsTester()
        .addEqualityGroup(b.build(), b.build())
        .addEqualityGroup(new Object())
        .addEqualityGroup(b.setId("id2").build())
        .addEqualityGroup(b.setPeerInterface("Eth2").build())
        .addEqualityGroup(b.setLocalInterface("Vlan2").build())
        .addEqualityGroup(b.setPeerAddress(Ip.parse("2.2.2.2")).build())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    Mlag m =
        Mlag.builder()
            .setId("ID")
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setLocalInterface("Ethernet1")
            .build();

    assertThat(SerializationUtils.clone(m), equalTo(m));
  }

  @Test
  void testJsonSerialization() {
    Mlag m =
        Mlag.builder()
            .setId("ID")
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setLocalInterface("Ethernet1")
            .build();

    assertThat(BatfishObjectMapper.clone(m, Mlag.class), equalTo(m));
  }
}
