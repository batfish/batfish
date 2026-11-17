package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link LinkLocalAddress} */
class LinkLocalAddressTest {

  @Test
  void testEquals() {
    Ip ip = Ip.parse("169.254.0.1");
    Ip ip2 = Ip.parse("169.254.0.2");
    LinkLocalAddress lla = LinkLocalAddress.of(ip);
    new EqualsTester()
        .addEqualityGroup(lla, lla, LinkLocalAddress.of(ip))
        .addEqualityGroup(LinkLocalAddress.of(ip2))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    Ip ip = Ip.parse("169.254.0.1");
    LinkLocalAddress lla = LinkLocalAddress.of(ip);
    assertThat(SerializationUtils.clone(lla), equalTo(lla));
  }

  @Test
  void testJsonSerialization() {
    Ip ip = Ip.parse("169.254.0.1");
    LinkLocalAddress lla = LinkLocalAddress.of(ip);
    assertThat(BatfishObjectMapper.clone(lla, LinkLocalAddress.class), equalTo(lla));
  }

  @Test
  void testInvalidIp() {
    assertThrows(IllegalArgumentException.class, () -> LinkLocalAddress.of(Ip.parse("1.1.1.1")));
  }

  @Test
  void testGetPrefix() {
    assertThat(
        LinkLocalAddress.parse("link-local:169.254.44.55").getPrefix(),
        equalTo(Prefix.parse("169.254.0.0/16")));
  }
}
