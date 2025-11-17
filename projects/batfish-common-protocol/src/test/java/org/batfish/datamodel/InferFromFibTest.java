package org.batfish.datamodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class InferFromFibTest extends TestCase {
  /**
   * {@link InferFromFib#getPotentialSourceIps} should dynamically resolve source IP based on FIB
   * and interface configurations.
   */
  @Test
  public void testGetPotentialSourceIps() {
    String iface1 = "iface1";

    ImmutableList<AbstractRoute> resolutionSteps =
        ImmutableList.of(new ConnectedRoute(Prefix.parse("2.2.2.2/31"), iface1));

    FibEntry fibEntry = new FibEntry(FibForward.of(Ip.parse("1.1.1.1"), iface1), resolutionSteps);

    Ip dstIp = Ip.parse("2.2.2.3");
    Ip sourceIp = Ip.parse("2.2.2.2");

    Fib fib =
        MockFib.builder().setFibEntries(ImmutableMap.of(dstIp, ImmutableSet.of(fibEntry))).build();

    Configuration c = Configuration.builder().setHostname("r1").build();
    c.setInterfaces(
        ImmutableSortedMap.of(
            iface1,
            TestInterface.builder()
                .setName(iface1)
                .setAddress(ConcreteInterfaceAddress.create(sourceIp, 31))
                .build()));
    assertEquals(
        InferFromFib.instance().getPotentialSourceIps(dstIp, fib, c), ImmutableSet.of(sourceIp));
  }

  /**
   * {@link InferFromFib#getPotentialSourceIps} should return an empty set if {@code fib} is null.
   */
  @Test
  public void testGetPotentialSourceIps_nullFib() {
    Configuration c = Configuration.builder().setHostname("r1").build();
    assertEquals(
        InferFromFib.instance().getPotentialSourceIps(Ip.parse("1.2.3.4"), null, c),
        ImmutableSet.of());
  }
}
