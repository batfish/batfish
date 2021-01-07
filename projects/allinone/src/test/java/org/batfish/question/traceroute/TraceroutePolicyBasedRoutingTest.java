package org.batfish.question.traceroute;

import static org.batfish.common.util.TracePruner.DEFAULT_MAX_TRACES;
import static org.batfish.datamodel.matchers.HopMatchers.hasAcceptingInterface;
import static org.batfish.datamodel.matchers.HopMatchers.hasOutputInterface;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.TraceMatchers.hasHop;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.LiteralVrfName;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end-ish tests of traceroute with policy-based routing */
public class TraceroutePolicyBasedRoutingTest {

  private static final String ROUTING_POLICY_NAME = "packetPolicy";
  private static final String SOURCE_LOCATION_STR = "enter(c1[ingressInterface])";
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final Ip INGRESS_IFACE_IP = Ip.parse("1.1.1.1");
  private static final Ip IFACE1_IP = Ip.parse("2.2.2.1");
  private static final Ip IFACE2_IP = Ip.parse("3.3.3.1");

  /*
   * Build a simple 1-node network with 2 VRFs
   */
  private static SortedMap<String, Configuration> pbrNetwork(boolean withPolicy) {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    ImmutableSortedMap.Builder<String, Configuration> configs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());

    Configuration c1 = cb.setHostname("c1").build();
    configs.put(c1.getHostname(), c1);

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    Vrf v2 = nf.vrfBuilder().setOwner(c1).setName("otherVRF").build();

    Interface ingressIface =
        nf.interfaceBuilder()
            .setAddress(ConcreteInterfaceAddress.create(INGRESS_IFACE_IP, 31))
            .setOwner(c1)
            .setVrf(v1)
            .setName("ingressInterface")
            .build();

    if (withPolicy) {
      ingressIface.setRoutingPolicy(ROUTING_POLICY_NAME);
      // If IP protocol is TCP use PBR to do a lookup in V2, which will cause the packet to
      // take a different exit interface
      c1.setPacketPolicies(
          ImmutableSortedMap.of(
              ROUTING_POLICY_NAME,
              new PacketPolicy(
                  ROUTING_POLICY_NAME,
                  ImmutableList.of(
                      new If(
                          new PacketMatchExpr(
                              new MatchHeaderSpace(
                                  HeaderSpace.builder().setIpProtocols(IpProtocol.TCP).build())),
                          ImmutableList.of(
                              new Return(new FibLookup(new LiteralVrfName(v2.getName()))))),
                      new If(
                          new PacketMatchExpr(
                              new MatchHeaderSpace(
                                  HeaderSpace.builder().setIpProtocols(IpProtocol.UDP).build())),
                          ImmutableList.of(new Return(Drop.instance())))),
                  new Return(new FibLookup(new LiteralVrfName(v1.getName()))))));
      ingressIface.setRoutingPolicy(ROUTING_POLICY_NAME);
    }

    Interface i1 =
        nf.interfaceBuilder()
            .setAddress(ConcreteInterfaceAddress.create(IFACE1_IP, 24))
            .setOwner(c1)
            .setVrf(v1)
            .setName("i1")
            .build();
    Interface i2 =
        nf.interfaceBuilder()
            .setAddress(ConcreteInterfaceAddress.create(IFACE2_IP, 24))
            .setOwner(c1)
            .setVrf(v2)
            .setName("i2")
            .build();

    // Static routes. Different next hop interfaces in different VRFs
    final Prefix prefixToMatch = Prefix.parse("9.9.9.0/24");
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(prefixToMatch)
                .setNextHopInterface(i1.getName())
                .setAdmin(1)
                .build()));
    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(prefixToMatch)
                .setNextHopInterface(i2.getName())
                .setAdmin(1)
                .build()));

    return configs.build();
  }

  @Test
  public void testWithNoPolicy() throws IOException {
    /*
     Construct TCP flow to 9.9.9.9, but don't set up PBR. Flow should take V1's static route out i1.
    */
    SortedMap<String, Configuration> configs = pbrNetwork(false);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    PacketHeaderConstraints header =
        PacketHeaderConstraints.builder()
            .setSrcIp("1.1.1.222")
            .setDstIp("9.9.9.9")
            .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
            .build();

    TracerouteQuestion question =
        new TracerouteQuestion(SOURCE_LOCATION_STR, header, false, DEFAULT_MAX_TRACES);
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    Map<Flow, List<Trace>> traces = answerer.getTraces(batfish.getSnapshot(), question);

    assertThat(traces.entrySet(), hasSize(1));
    assertThat(
        traces.values().iterator().next(),
        contains(hasHop(hasOutputInterface(NodeInterfacePair.of("c1", "i1")))));
  }

  @Test
  public void testMatchesPolicyIf() throws IOException {
    /*
     Construct TCP flow to 9.9.9.9. This should match the first PBR rule, resulting in a FIB lookup
     in V2, so it should take V2's static route out i2.
    */
    SortedMap<String, Configuration> configs = pbrNetwork(true);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    PacketHeaderConstraints header =
        PacketHeaderConstraints.builder()
            .setSrcIp("1.1.1.222")
            .setDstIp("9.9.9.9")
            .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
            .build();

    TracerouteQuestion question =
        new TracerouteQuestion(SOURCE_LOCATION_STR, header, false, DEFAULT_MAX_TRACES);
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    Map<Flow, List<Trace>> traces = answerer.getTraces(batfish.getSnapshot(), question);

    assertThat(traces.entrySet(), hasSize(1));
    assertThat(
        traces.values().iterator().next(),
        contains(hasHop(hasOutputInterface(NodeInterfacePair.of("c1", "i2")))));
  }

  @Test
  public void testMatchesPolicyIfThenDropDrop() throws IOException {
    /*
     Construct UDP flow to 9.9.9.9. This should match the second PBR rule and get dropped.
    */
    SortedMap<String, Configuration> configs = pbrNetwork(true);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    PacketHeaderConstraints header =
        PacketHeaderConstraints.builder()
            .setSrcIp("1.1.1.222")
            .setDstIp("9.9.9.9")
            .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
            .build();

    TracerouteQuestion question =
        new TracerouteQuestion(SOURCE_LOCATION_STR, header, false, DEFAULT_MAX_TRACES);
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    Map<Flow, List<Trace>> traces = answerer.getTraces(batfish.getSnapshot(), question);

    assertThat(traces.entrySet(), hasSize(1));
    assertThat(
        traces.values().iterator().next(), contains(hasDisposition(FlowDisposition.DENIED_IN)));
  }

  @Test
  public void testDoesNotMatchPolicyIf() throws IOException {
    /*
     Construct ICMP flow to 9.9.9.9. This shouldn't match either PBR rule, so should take the
     policy's default action of FIB lookup in V1. Should take V1's static route out i1.
    */
    SortedMap<String, Configuration> configs = pbrNetwork(true);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    PacketHeaderConstraints header =
        PacketHeaderConstraints.builder()
            .setSrcIp("1.1.1.222")
            .setDstIp("9.9.9.9")
            .setIpProtocols(ImmutableSet.of(IpProtocol.ICMP))
            .build();

    TracerouteQuestion question =
        new TracerouteQuestion(SOURCE_LOCATION_STR, header, false, DEFAULT_MAX_TRACES);
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    Map<Flow, List<Trace>> traces = answerer.getTraces(batfish.getSnapshot(), question);

    assertThat(traces.entrySet(), hasSize(1));
    assertThat(
        traces.values().iterator().next(),
        contains(hasHop(hasOutputInterface(NodeInterfacePair.of("c1", "i1")))));
  }

  @Test
  public void testMatchesPolicyIf_destinedForFirstVrf() throws IOException {
    /*
     Construct TCP flow to an IP owned by V1. This should match the first PBR rule, then get
     accepted into V1.
    */
    SortedMap<String, Configuration> configs = pbrNetwork(true);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    PacketHeaderConstraints header =
        PacketHeaderConstraints.builder()
            .setSrcIp("1.1.1.222")
            .setDstIp(IFACE1_IP.toString())
            .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
            .build();

    TracerouteQuestion question =
        new TracerouteQuestion(SOURCE_LOCATION_STR, header, false, DEFAULT_MAX_TRACES);
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    Map<Flow, List<Trace>> traces = answerer.getTraces(batfish.getSnapshot(), question);

    assertThat(traces.entrySet(), hasSize(1));
    assertThat(
        traces.values().iterator().next(),
        contains(hasHop(hasAcceptingInterface(NodeInterfacePair.of("c1", "i1")))));
  }

  @Test
  public void testMatchesPolicyIfThenDrop_destinedForFirstVrf() throws IOException {
    /*
     Construct UDP flow to an IP owned by V1. This should match the second PBR rule and get dropped.
    */
    SortedMap<String, Configuration> configs = pbrNetwork(true);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    PacketHeaderConstraints header =
        PacketHeaderConstraints.builder()
            .setSrcIp("1.1.1.222")
            .setDstIp(IFACE1_IP.toString())
            .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
            .build();

    TracerouteQuestion question =
        new TracerouteQuestion(SOURCE_LOCATION_STR, header, false, DEFAULT_MAX_TRACES);
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    Map<Flow, List<Trace>> traces = answerer.getTraces(batfish.getSnapshot(), question);

    assertThat(traces.entrySet(), hasSize(1));
    assertThat(
        traces.values().iterator().next(), contains(hasDisposition(FlowDisposition.DENIED_IN)));
  }

  @Test
  public void testDoesNotMatchPolicy_destinedForFirstVrf() throws IOException {
    /*
     Construct ICMP flow to an IP owned by V1. This shouldn't match either PBR rule, but since the
     policy's default action is not DROP, it should still be accepted into V1.
    */
    SortedMap<String, Configuration> configs = pbrNetwork(true);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    PacketHeaderConstraints header =
        PacketHeaderConstraints.builder()
            .setSrcIp("1.1.1.222")
            .setDstIp(IFACE1_IP.toString())
            .setIpProtocols(ImmutableSet.of(IpProtocol.ICMP))
            .build();

    TracerouteQuestion question =
        new TracerouteQuestion(SOURCE_LOCATION_STR, header, false, DEFAULT_MAX_TRACES);
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    Map<Flow, List<Trace>> traces = answerer.getTraces(batfish.getSnapshot(), question);

    assertThat(traces.entrySet(), hasSize(1));
    assertThat(
        traces.values().iterator().next(),
        contains(hasHop(hasAcceptingInterface(NodeInterfacePair.of("c1", "i1")))));
  }

  @Test
  public void testMatchesPolicyIf_destinedForSecondVrf() throws IOException {
    /*
     Construct TCP flow to an IP owned by V2. This should match the first PBR rule, then should be
     forwarded according to V2's FIB, resulting in NEIGHBOR_UNREACHABLE. There should never be an
     opportunity for V2 to accept the flow (even though it owns the dst IP).
    */
    SortedMap<String, Configuration> configs = pbrNetwork(true);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    PacketHeaderConstraints header =
        PacketHeaderConstraints.builder()
            .setSrcIp("1.1.1.222")
            .setDstIp(IFACE2_IP.toString())
            .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
            .build();

    TracerouteQuestion question =
        new TracerouteQuestion(SOURCE_LOCATION_STR, header, false, DEFAULT_MAX_TRACES);
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    Map<Flow, List<Trace>> traces = answerer.getTraces(batfish.getSnapshot(), question);

    assertThat(traces.entrySet(), hasSize(1));
    assertThat(
        traces.values().iterator().next(),
        contains(hasDisposition(FlowDisposition.NEIGHBOR_UNREACHABLE)));
  }
}
