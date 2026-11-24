package org.batfish.datamodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.junit.jupiter.api.Test;

class InferFromFibTest {
  /**
   * {@link InferFromFib#getPotentialSourceIps} should dynamically resolve source IP based on FIB
   * and interface configurations.
   */
  @Test
  void testGetPotentialSourceIps() {
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
  void testGetPotentialSourceIps_nullFib() {
    Configuration c = Configuration.builder().setHostname("r1").build();
    assertEquals(
        InferFromFib.instance().getPotentialSourceIps(Ip.parse("1.2.3.4"), null, c),
        ImmutableSet.of());
  }
}
