package org.batfish.datamodel;

import static org.batfish.datamodel.Ip.getBitAtPosition;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link Ip}. */
@RunWith(JUnit4.class)
public class IpTest {
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
  public void testContainsIp() {
    IpSpace ipSpace = new Ip("1.1.1.1").toIpSpace();
    assertThat(ipSpace, containsIp(new Ip("1.1.1.1")));
    assertThat(ipSpace, not(containsIp(new Ip("1.2.3.4"))));
  }

  @Test
  public void testComplement() {
    IpSpace ipSpace = new Ip("1.1.1.1").toIpSpace().complement();
    assertThat(ipSpace, not(containsIp(new Ip("1.1.1.1"))));
    assertThat(ipSpace, containsIp(new Ip("1.2.3.4")));
  }
}
