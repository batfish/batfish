package org.batfish.datamodel;

import static org.batfish.datamodel.Ip.getBitAtPosition;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link Ip}. */
@RunWith(JUnit4.class)
public class IpTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void numSubnetBitsToSubnetLong() {
    // Test the boundaries (0 and 32) as well as a representative sample of intermediate values.
    assertThat(Ip.numSubnetBitsToSubnetLong(0), equalTo(0L));
    assertThat(Ip.numSubnetBitsToSubnetLong(4), equalTo(0xF0000000L));
    assertThat(Ip.numSubnetBitsToSubnetLong(17), equalTo(0xFFFF8000L));
    assertThat(Ip.numSubnetBitsToSubnetLong(31), equalTo(0xFFFFFFFEL));
    assertThat(Ip.numSubnetBitsToSubnetLong(32), equalTo(0xFFFFFFFFL));
  }

  @Test
  public void testGetBitAtPosition() {
    assertThat(getBitAtPosition(0L, 0), is(false));
    assertThat(getBitAtPosition(0L, 31), is(false));
    assertThat(getBitAtPosition(1L, 31), is(true));
    assertThat(getBitAtPosition(0xFF000000L, 7), is(true));
    assertThat(getBitAtPosition(0xFF000000L, 8), is(false));
    for (int i = 0; i < 32; i++) {
      assertThat(getBitAtPosition(Ip.MAX.asLong(), i), equalTo(true));
    }
  }

  @Test
  public void testGetBitAtInvalidTooHighPosition() {
    _thrown.expect(IllegalArgumentException.class);
    getBitAtPosition(0L, Prefix.MAX_PREFIX_LENGTH);
  }

  @Test
  public void testGetBitAtInvalidNegativePosition() {
    _thrown.expect(IllegalArgumentException.class);
    getBitAtPosition(0L, -1);
  }

  @Test
  public void testContainsIp() {
    IpSpace ipSpace = Ip.parse("1.1.1.1").toIpSpace();
    assertThat(ipSpace, containsIp(Ip.parse("1.1.1.1")));
    assertThat(ipSpace, not(containsIp(Ip.parse("1.2.3.4"))));
  }

  @Test
  public void testComplement() {
    IpSpace ipSpace = Ip.parse("1.1.1.1").toIpSpace().complement();
    assertThat(ipSpace, not(containsIp(Ip.parse("1.1.1.1"))));
    assertThat(ipSpace, containsIp(Ip.parse("1.2.3.4")));
  }

  @Test
  public void testInvalidIp() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Invalid IPv4 address: 1.1.1.256. 256 is an invalid octet");
    Ip.parse("1.1.1.256");
  }

  @Test
  public void testCreateInvalidIp() {
    _thrown.expect(IllegalArgumentException.class);
    Ip.create(1L << 32);
  }

  @Test
  public void testIsValidNetmask1sLeading() {
    assertTrue(Ip.parse("0.0.0.0").isValidNetmask1sLeading());
    assertTrue(Ip.parse("255.255.255.255").isValidNetmask1sLeading());
    assertTrue(Ip.parse("255.128.0.0").isValidNetmask1sLeading());
    assertFalse(Ip.parse("0.0.1.255").isValidNetmask1sLeading());
    assertFalse(Ip.parse("255.0.1.0").isValidNetmask1sLeading());
  }
}
