package org.batfish.datamodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import junit.framework.TestCase;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SourceIpInferenceTest extends TestCase {
  /** Test serializing/deserializing {@link SourceIpInference.InferFromFib}. */
  @Test
  public void testJacksonSerialization_InferFromFib() {
    assertEquals(
        BatfishObjectMapper.clone(
            SourceIpInference.InferFromFib.instance(), SourceIpInference.InferFromFib.class),
        SourceIpInference.InferFromFib.instance());
  }

  /**
   * {@link SourceIpInference.InferFromFib#getPotentialSourceIps} should dynamically resolve source
   * IP based on FIB and interface configurations.
   */
  @Test
  public void testInferFromFib_getPotentialSourceIps() {
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
            Interface.builder()
                .setName(iface1)
                .setAddress(ConcreteInterfaceAddress.create(sourceIp, 31))
                .build()));
    assertEquals(
        SourceIpInference.InferFromFib.instance().getPotentialSourceIps(dstIp, fib, c),
        ImmutableSet.of(sourceIp));
  }

  /** Test serializing/deserializing {@link SourceIpInference.UseConstantIp}. */
  @Test
  public void testJacksonSerialization_UseConstantIp() {
    SourceIpInference.UseConstantIp useConstantIp =
        SourceIpInference.UseConstantIp.create(Ip.parse("1.2.3.4"));
    assertEquals(
        BatfishObjectMapper.clone(useConstantIp, SourceIpInference.UseConstantIp.class),
        useConstantIp);
  }

  /**
   * {@link SourceIpInference.UseConstantIp#getPotentialSourceIps} should return a singleton set of
   * the given constant IP address.
   */
  @Test
  public void testUseConstantIp_getPotentialSourceIps() {
    Ip constantIp = Ip.parse("1.2.3.4");
    SourceIpInference.UseConstantIp useConstantIp =
        SourceIpInference.UseConstantIp.create(constantIp);
    Fib dummyFib = MockFib.builder().build();
    Configuration dummyConf = Configuration.builder().setHostname("r1").build();
    assertEquals(
        useConstantIp.getPotentialSourceIps(Ip.parse("5.6.7.8"), dummyFib, dummyConf),
        ImmutableSet.of(constantIp));
  }
}
