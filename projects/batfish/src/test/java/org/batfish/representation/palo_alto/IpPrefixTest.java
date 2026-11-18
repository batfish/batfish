package org.batfish.representation.palo_alto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.jupiter.api.Test;

/** Tests for {@link IpPrefix} */
class IpPrefixTest {

  @Test
  void testParseInvalidPrefixBits() {
    assertThrows(IllegalArgumentException.class, () -> IpPrefix.parse("1.2.3.4/33"));
  }

  @Test
  void testParseInvalidChars() {
    assertThrows(IllegalArgumentException.class, () -> IpPrefix.parse("1.A.3.4/33"));
  }

  @Test
  void testParseInvalidHostBits() {
    assertThrows(IllegalArgumentException.class, () -> IpPrefix.parse("1.2.3.256/32"));
  }

  @Test
  void testPreserveBaseIpAddress() {
    IpPrefix ipPrefix = IpPrefix.parse("1.2.3.4/24");

    // Make sure we preserve pre-canonicalized form of prefix ip
    assertThat(ipPrefix.getIp(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(ipPrefix.getPrefix(), equalTo(Prefix.parse("1.2.3.0/24")));
  }
}
