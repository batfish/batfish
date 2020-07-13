package org.batfish.representation.palo_alto;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link IpPrefix} */
public class IpPrefixTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testParseInvalidPrefixBits() {
    thrown.expect(IllegalArgumentException.class);
    IpPrefix.parse("1.2.3.4/33");
  }

  @Test
  public void testParseInvalidChars() {
    thrown.expect(IllegalArgumentException.class);
    IpPrefix.parse("1.A.3.4/33");
  }

  @Test
  public void testParseInvalidHostBits() {
    thrown.expect(IllegalArgumentException.class);
    IpPrefix.parse("1.2.3.256/32");
  }

  @Test
  public void testPreserveBaseIpAddress() {
    IpPrefix ipPrefix = IpPrefix.parse("1.2.3.4/24");

    // Make sure we preserve pre-canonicalized form of prefix ip
    assertThat(ipPrefix.getIp(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(ipPrefix.getPrefix(), equalTo(Prefix.parse("1.2.3.0/24")));
  }
}
