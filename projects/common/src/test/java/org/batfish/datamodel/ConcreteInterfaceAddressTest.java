package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import java.util.Optional;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link ConcreteInterfaceAddress} */
class ConcreteInterfaceAddressTest {

  @Test
  void testEquals() {
    ConcreteInterfaceAddress cia = ConcreteInterfaceAddress.parse("1.1.1.1/24");
    new EqualsTester()
        .addEqualityGroup(cia, cia, ConcreteInterfaceAddress.parse("1.1.1.1/24"))
        .addEqualityGroup(ConcreteInterfaceAddress.parse("1.1.1.2/24"))
        .addEqualityGroup(ConcreteInterfaceAddress.parse("1.1.1.1/23"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testParse() {
    assertThat(
        ConcreteInterfaceAddress.parse("1.1.1.1/24"),
        equalTo(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 24)));
  }

  @Test
  void testParseNetworkAddress() {
    assertThat(
        ConcreteInterfaceAddress.parse("192.0.2.0/24").getIp(), equalTo(Ip.parse("192.0.2.0")));
  }

  @Test
  void testParseBroadcastAddress() {
    assertThat(
        ConcreteInterfaceAddress.parse("192.0.2.255/24").getIp(), equalTo(Ip.parse("192.0.2.255")));
  }

  @Test
  void testTryParse() {
    assertThat(
        ConcreteInterfaceAddress.tryParse("1.1.1.1/24"),
        equalTo(Optional.of(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 24))));
    assertThat(ConcreteInterfaceAddress.tryParse("garbade"), equalTo(Optional.empty()));
  }

  @Test
  void testJavaSerialization() {
    ConcreteInterfaceAddress cia = ConcreteInterfaceAddress.parse("1.1.1.1/24");
    assertThat(SerializationUtils.clone(cia), equalTo(cia));
  }

  @Test
  void testJsonSerialization() {
    ConcreteInterfaceAddress cia = ConcreteInterfaceAddress.parse("1.1.1.1/24");
    assertThat(BatfishObjectMapper.clone(cia, ConcreteInterfaceAddress.class), equalTo(cia));
  }

  @Test
  void testInvalidNumBitsLow() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 0));
  }

  @Test
  void testInvalidNumBitsHigh() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 33));
  }

  @Test
  void testGetPrefix() {
    assertThat(
        ConcreteInterfaceAddress.parse("1.1.1.55/24").getPrefix(),
        equalTo(Prefix.parse("1.1.1.0/24")));
  }
}
