package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link FibForward}. */
public final class FibForwardTest {

  @Test
  public void testEquals() {
    FibForward fibForward = FibForward.of(Ip.parse("1.1.1.1"), "eth0");

    new EqualsTester()
        .addEqualityGroup(fibForward, fibForward, FibForward.of(Ip.parse("1.1.1.1"), "eth0"))
        .addEqualityGroup(FibForward.of(Ip.parse("1.1.1.2"), "eth0"))
        .addEqualityGroup(FibForward.of(Ip.parse("1.1.1.1"), "eth1"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    FibForward fibForward = FibForward.of(Ip.parse("1.1.1.1"), "eth0");

    assertEquals(fibForward, SerializationUtils.clone(fibForward));
  }
}
