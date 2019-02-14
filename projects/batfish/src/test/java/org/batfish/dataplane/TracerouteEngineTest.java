package org.batfish.dataplane;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.MockFib;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link TracerouteEngineImpl} */
public class TracerouteEngineTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Rule public TemporaryFolder _tempFolder = new TemporaryFolder();

  private static boolean interfaceRepliesToArpRequestForIp(
      Interface iface, Fib ifaceFib, Ip arpIp) {
    // interfaces without addresses never reply
    if (iface.getAllAddresses().isEmpty()) {
      return false;
    }
    // the interface that owns the arpIp always replies
    if (iface.getAllAddresses().stream().anyMatch(addr -> addr.getIp().equals(arpIp))) {
      return true;
    }

    /*
     * iface does not own arpIp, so it replies if and only if:
     * 1. proxy-arp is enabled
     * 2. the interface's vrf has a route to the destination
     * 3. the destination is not on the incoming edge.
     */
    Set<String> nextHopInterfaces = ifaceFib.getNextHopInterfaces(arpIp);
    return iface.getProxyArp()
        && !nextHopInterfaces.isEmpty()
        && nextHopInterfaces.stream().noneMatch(iface.getName()::equals);
  }

  /*
   * iface1 and iface2 are interfaces on the same node. Send traffic with dstIp=iface2's ip to
   * iface1. Should accept if and only if iface1 and iface2 are in the same VRF.
   */
  @Test
  public void testAcceptVrf() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.build();
    Vrf.Builder vb = nf.vrfBuilder().setOwner(config);
    Interface.Builder ib = nf.interfaceBuilder().setOwner(config);

    Vrf vrf1 = vb.build();
    Vrf vrf2 = vb.build();

    Interface i1 = ib.setVrf(vrf1).setAddress(new InterfaceAddress("1.1.1.1/24")).build();
    Interface i2 = ib.setVrf(vrf2).setAddress(new InterfaceAddress("2.2.2.2/24")).build();
    ib.setVrf(vrf2).setAddress(new InterfaceAddress("3.3.3.3/24")).build();

    // Compute data plane
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(config.getHostname(), config);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    batfish.computeDataPlane();
    DataPlane dp = batfish.loadDataPlane();

    // Construct flows
    Flow.Builder fb =
        Flow.builder()
            .setDstIp(Ip.parse("3.3.3.3"))
            .setIngressNode(config.getHostname())
            .setTag("TAG");

    Flow flow1 = fb.setIngressInterface(i1.getName()).setIngressVrf(vrf1.getName()).build();
    Flow flow2 = fb.setIngressInterface(i2.getName()).setIngressVrf(vrf2.getName()).build();

    // Compute flow traces
    SortedMap<Flow, List<Trace>> traces =
        new TracerouteEngineImpl(dp).computeTraces(ImmutableSet.of(flow1, flow2), false);

    assertThat(traces, hasEntry(equalTo(flow1), contains(hasDisposition(NO_ROUTE))));
    assertThat(traces, hasEntry(equalTo(flow2), contains(hasDisposition(ACCEPTED))));
  }

  @Test
  public void testArpMultipleAccess() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Interface.Builder ib = nf.interfaceBuilder().setProxyArp(true);

    Configuration source = cb.build();
    Vrf vSource = vb.setOwner(source).build();
    ib.setOwner(source).setVrf(vSource).setAddress(new InterfaceAddress("10.0.0.1/24")).build();

    Configuration dst = cb.build();
    Vrf vDst = vb.setOwner(dst).build();
    ib.setOwner(dst).setVrf(vDst).setAddress(new InterfaceAddress("10.0.0.2/24")).build();

    Configuration other = cb.build();
    Vrf vOther = vb.setOwner(other).build();
    ib.setOwner(other).setVrf(vOther).setAddress(new InterfaceAddress("10.0.0.3/24")).build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.<String, Configuration>naturalOrder()
            .put(source.getHostname(), source)
            .put(dst.getHostname(), dst)
            .put(other.getHostname(), other)
            .build();
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, _tempFolder);
    batfish.computeDataPlane();
    DataPlane dp = batfish.loadDataPlane();
    Flow flow =
        Flow.builder()
            .setIngressNode(source.getHostname())
            .setSrcIp(Ip.parse("10.0.0.1"))
            .setDstIp(Ip.parse("10.0.0.2"))
            .setTag("tag")
            .build();
    List<Trace> traces =
        new TracerouteEngineImpl(dp).computeTraces(ImmutableSet.of(flow), false).get(flow);

    /*
     *  Since the 'other' neighbor should not respond to ARP:
     *  - There should only be one trace, ending at 'dst'.
     *  - It should be accepting.
     */
    assertThat(traces, contains(hasDisposition(ACCEPTED)));
  }

  /*
   * iface1 and iface2 are on the same node. Their IPs are on different networks, and iface1
   * has proxy-arp on. We send an ARP request to iface1 for iface2's IP. iface1 should reply
   * if and only if iface1 and iface2 are in the same VRF.
   */
  @Test
  public void testArpVrf() {
    // Construct interfaces
    NetworkFactory nf = new NetworkFactory();
    Interface.Builder ib = nf.interfaceBuilder();

    Interface i1 = ib.setAddress(new InterfaceAddress("1.1.1.0/24")).setProxyArp(true).build();
    Interface i2 = ib.setAddress(new InterfaceAddress("2.2.2.0/24")).setProxyArp(true).build();
    Interface i3 = ib.setAddress(new InterfaceAddress("3.3.3.0/24")).setProxyArp(false).build();
    Interface i4 = ib.setAddress(new InterfaceAddress("4.4.4.4/24")).setProxyArp(false).build();
    Interface i5 = ib.setAddress(null).setProxyArp(true).build();

    Ip arpIp = Ip.parse("4.4.4.4");

    String i1Name = i1.getName();
    String i4Name = i4.getName();

    // vrf1 has no routes to arpIp
    Fib vrf1Fib = MockFib.builder().setNextHopInterfacesByIp(ImmutableMap.of()).build();

    // vrf2 routes arpIp through i4
    Fib vrf2Fib =
        MockFib.builder()
            .setNextHopInterfacesByIp(
                ImmutableMap.of(arpIp, ImmutableMap.of(i4Name, ImmutableMap.of())))
            .build();

    assertFalse(
        "ARP request to interface on a different VRF should fail",
        interfaceRepliesToArpRequestForIp(i1, vrf1Fib, arpIp));
    assertTrue(
        "ARP request to interface with proxy-arp enabled should succeed",
        interfaceRepliesToArpRequestForIp(i2, vrf2Fib, arpIp));
    assertFalse(
        "ARP request to interface with proxy-arp disabled should fail",
        interfaceRepliesToArpRequestForIp(i3, vrf2Fib, arpIp));
    assertTrue(
        "ARP request to interface that owns arpIp should succeed",
        interfaceRepliesToArpRequestForIp(i4, vrf2Fib, arpIp));
    assertFalse(
        "ARP request to interface with no address should fail",
        interfaceRepliesToArpRequestForIp(i5, vrf2Fib, arpIp));

    // arpIp isn't owned by the VRF, but is routable
    arpIp = Ip.parse("4.4.4.0");
    assertFalse(
        "ARP request for interface subnet to the same interface should fail",
        interfaceRepliesToArpRequestForIp(i4, vrf2Fib, arpIp));

    /*
     * There are routes for arpIp through multiple interfaces, but i4 still doesn't reply because
     * there is a route for arpIp through i4 itself.
     */
    arpIp = Ip.parse("4.4.4.0");
    vrf2Fib =
        MockFib.builder()
            .setNextHopInterfacesByIp(
                ImmutableMap.of(
                    arpIp, ImmutableMap.of(i1Name, ImmutableMap.of(), i4Name, ImmutableMap.of())))
            .build();
    assertFalse(
        "ARP request for interface subnet to the same interface should fail, "
            + "even if other routes are available",
        interfaceRepliesToArpRequestForIp(i4, vrf2Fib, arpIp));
  }

  @Test
  public void testAclBeforeArpNoEdge() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c = cb.build();
    Vrf v = nf.vrfBuilder().setOwner(c).setName(Configuration.DEFAULT_VRF_NAME).build();
    // denies everything
    IpAccessList outgoingFilter =
        nf.aclBuilder().setOwner(c).setName("outgoingAcl").setLines(ImmutableList.of()).build();
    nf.interfaceBuilder()
        .setOwner(c)
        .setVrf(v)
        .setAddress(new InterfaceAddress("1.0.0.0/24"))
        .setOutgoingFilter(outgoingFilter)
        .build();
    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(c.getHostname(), c);
    Batfish b = BatfishTestUtils.getBatfish(configurations, _tempFolder);
    b.computeDataPlane();
    Flow flow =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setTag(Flow.BASE_FLOW_TAG)
            .setDstIp(Ip.parse("1.0.0.1"))
            .build();
    Trace trace = Iterables.getOnlyElement(b.buildFlows(ImmutableSet.of(flow), false).get(flow));

    /* Flow should be blocked by ACL before ARP, which would otherwise result in unreachable neighbor */
    assertThat(trace, hasDisposition(FlowDisposition.DENIED_OUT));
  }

  @Test
  public void testAclBeforeArpWithEdge() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Interface.Builder ib = nf.interfaceBuilder();

    // c1
    Configuration c1 = cb.build();
    Vrf v1 = vb.setOwner(c1).build();
    // denies everything
    IpAccessList outgoingFilter =
        nf.aclBuilder().setOwner(c1).setName("outgoingAcl").setLines(ImmutableList.of()).build();
    ib.setOwner(c1)
        .setVrf(v1)
        .setOutgoingFilter(outgoingFilter)
        .setAddress(new InterfaceAddress("1.0.0.0/24"))
        .build();

    // c2
    Configuration c2 = cb.build();
    Vrf v2 = vb.setOwner(c2).build();
    ib.setOwner(c2)
        .setVrf(v2)
        .setOutgoingFilter(null)
        .setAddress(new InterfaceAddress("1.0.0.3/24"))
        .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    Batfish b = BatfishTestUtils.getBatfish(configurations, _tempFolder);
    b.computeDataPlane();
    Flow flow =
        Flow.builder()
            .setIngressNode(c1.getHostname())
            .setTag(Flow.BASE_FLOW_TAG)
            .setDstIp(Ip.parse("1.0.0.1"))
            .build();
    Trace trace = Iterables.getOnlyElement(b.buildFlows(ImmutableSet.of(flow), false).get(flow));

    /* Flow should be blocked by ACL before ARP, which would otherwise result in unreachable neighbor */
    assertThat(trace, hasDisposition(FlowDisposition.DENIED_OUT));
  }

  /** When ingress node is non-existent, don't crash with null-pointer. */
  @Test
  public void testTracerouteOutsideNetwork() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(c1.getHostname(), c1), _tempFolder);
    batfish.computeDataPlane();
    DataPlane dp = batfish.loadDataPlane();

    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Node missingNode is not in the network");
    new TracerouteEngineImpl(dp)
        .computeTraces(
            ImmutableSet.of(Flow.builder().setTag("tag").setIngressNode("missingNode").build()),
            false);
  }
}
