package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UseConstantIpTest extends TestCase {
  /**
   * {@link UseConstantIp#getPotentialSourceIps} should return a singleton set of the given constant
   * IP address.
   */
  @Test
  public void testGetPotentialSourceIps() {
    Ip constantIp = Ip.parse("1.2.3.4");
    UseConstantIp useConstantIp = UseConstantIp.create(constantIp);
    Fib dummyFib = MockFib.builder().build();
    Configuration dummyConf = Configuration.builder().setHostname("r1").build();
    assertEquals(
        useConstantIp.getPotentialSourceIps(Ip.parse("5.6.7.8"), dummyFib, dummyConf),
        ImmutableSet.of(constantIp));
  }

  @Test
  public void testEquality() {
    new EqualsTester()
        .addEqualityGroup(
            UseConstantIp.create(Ip.parse("1.1.1.1")), UseConstantIp.create(Ip.parse("1.1.1.1")))
        .addEqualityGroup(UseConstantIp.create(Ip.parse("1.1.1.2")))
        .addEqualityGroup(InferFromFib.instance())
        .testEquals();
  }

  @Test
  public void testToString() {
    assertEquals(UseConstantIp.create(Ip.parse("1.1.1.1")).toString(), "UseConstantIp(1.1.1.1)");
  }
}
