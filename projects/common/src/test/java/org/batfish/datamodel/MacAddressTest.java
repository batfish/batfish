package org.batfish.datamodel;

import static org.batfish.datamodel.MacAddress.asMacAddressString;
import static org.batfish.datamodel.MacAddress.of;
import static org.batfish.datamodel.MacAddress.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

@ParametersAreNonnullByDefault
final class MacAddressTest {

  @Test
  void testAsLong() {
    assertThat(parse("00:00:00:00:00:00").asLong(), equalTo(0L));
    assertThat(parse("01:23:45:Ab:cD:EF").asLong(), equalTo(0x012345ABCDEFL));
    assertThat(parse("FF:FF:FF:FF:FF:FF").asLong(), equalTo(0xFFFFFFFFFFFFL));
  }

  @Test
  void testAsMacAddressString() {
    assertThat(asMacAddressString(0L), equalTo("00:00:00:00:00:00"));
    assertThat(asMacAddressString(0x012345ABCDEFL), equalTo("01:23:45:ab:cd:ef"));
    assertThat(asMacAddressString(0xFFFFFFFFFFFFL), equalTo("ff:ff:ff:ff:ff:ff"));
  }

  @Test
  void testEquals() {
    MacAddress macAddress = of(0L);
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(macAddress, macAddress, of(0L))
        .addEqualityGroup(of(0x012345ABCDEFL))
        .addEqualityGroup(of(0xFFFFFFFFFFFFL))
        .testEquals();
  }

  @Test
  void testJacksonSerialization() {
    MacAddress macAddress = of(5L);

    assertThat(BatfishObjectMapper.clone(macAddress, MacAddress.class), equalTo(macAddress));
  }

  @Test
  void testJavaSerialization() {
    MacAddress macAddress = of(5L);

    assertThat(SerializationUtils.clone(macAddress), equalTo(macAddress));
  }

  @Test
  void testOfInvalidNegative() {
    assertThrows(IllegalArgumentException.class, () -> of(-1L));
  }

  @Test
  void testOfInvalidTooHigh() {
    assertThrows(IllegalArgumentException.class, () -> of(0xFFFFFFFFFFFFFL));
  }

  @Test
  void testOfValid() {
    // Don't crash
    of(0L);
    of(0x012345ABCDEFL);
    of(0xFFFFFFFFFFFFL);
  }

  @Test
  void testParseInvalidBadChar() {
    assertThrows(IllegalArgumentException.class, () -> parse("00:11:22:33:44:GG"));
  }

  @Test
  void testParseInvalidTooLong() {
    assertThrows(IllegalArgumentException.class, () -> parse("00:11:22:33:44:55:66"));
  }

  @Test
  void testParseMisingColons() {
    assertThrows(IllegalArgumentException.class, () -> parse("001122334455"));
  }

  @Test
  void testParseValid() {
    // Don't crash
    parse("00:00:00:00:00:00");
    parse("01:23:45:Ab:cD:EF");
    parse("FF:FF:FF:FF:FF:FF");
  }
}
