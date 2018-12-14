package org.batfish.question.traceroute;

import static org.batfish.common.util.TracePruner.DEFAULT_MAX_TRACES;
import static org.batfish.datamodel.matchers.EdgeMatchers.hasNode1;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressNode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressVrf;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIpProtocol;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasTag;
import static org.batfish.datamodel.matchers.FlowTraceHopMatchers.hasEdge;
import static org.batfish.datamodel.matchers.FlowTraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.FlowTraceMatchers.hasHop;
import static org.batfish.datamodel.matchers.FlowTraceMatchers.hasHops;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.question.traceroute.TracerouteAnswerer.COL_TRACES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.StaticRoute.Builder;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.matchers.TraceMatchers;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link TracerouteQuestion}. */
public class TracerouteTest {
  private static final String TAG = "tag";

  private static final String FAST_ETHERNET = "FastEthernet0/0";
  private static final String LOOPBACK = "Loopback0";
  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final Ip NODE1_FAST_ETHERNET_IP = new Ip("1.1.1.2");
  private static final Ip NODE1_LOOPBACK_IP = new Ip("1.1.1.1");
  private static final Ip NODE2_FAST_ETHERNET_IP = new Ip("1.1.1.3");
  private static final Ip NODE2_LOOPBACK_IP = new Ip("2.2.2.2");
  private static final String TESTRIGS_PREFIX = "org/batfish/allinone/testrigs/";
  private static final String TESTRIG_NAME = "specifiers-reachability";
  private static final List<String> TESTRIG_NODE_NAMES = ImmutableList.of(NODE1, NODE2);
  private static final String VRF = "default";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException thrown = ExpectedException.none();

  private Batfish _batfish;

  @Before
  public void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + TESTRIG_NAME, TESTRIG_NODE_NAMES)
                .build(),
            _folder);

    _batfish.computeDataPlane(false);
  }

  @Test
  public void testGetFlows_interfaceSource() {
    TracerouteQuestion question =
        new TracerouteQuestion(
            "node1",
            PacketHeaderConstraints.builder().setDstIp("2.2.2.2").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, _batfish);
    Set<Flow> flows = answerer.getFlows(TAG);

    String ingressInterface = null;
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        hasItem(
            allOf(
                hasIngressInterface(ingressInterface),
                hasIngressNode("node1"),
                hasIngressVrf(VRF),
                hasSrcIp(NODE1_LOOPBACK_IP),
                hasTag(TAG))));
    assertThat(
        flows,
        hasItem(
            allOf(
                hasIngressInterface(ingressInterface),
                hasIngressNode("node1"),
                hasIngressVrf(VRF),
                hasSrcIp(NODE1_FAST_ETHERNET_IP),
                hasTag(TAG))));
  }

  @Test
  public void testGetFlows_interfaceLinkSourceNoSourceIp() {
    TracerouteQuestion question =
        new TracerouteQuestion(
            "enter(node1)",
            PacketHeaderConstraints.builder().setDstIp("2.2.2.2").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, _batfish);
    thrown.expect(IllegalArgumentException.class);
    answerer.getFlows(TAG);
  }

  @Test
  public void testGetFlows_interfaceLinkSource() {
    TracerouteQuestion question =
        new TracerouteQuestion(
            "enter(node1)",
            PacketHeaderConstraints.builder().setSrcIp("1.1.1.0").setDstIp("2.2.2.2").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, _batfish);
    Set<Flow> flows = answerer.getFlows(TAG);

    // neither interfaces have networks for which link (host) IPs can be inferred
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        hasItem(
            allOf(
                hasIngressInterface(LOOPBACK),
                hasIngressNode("node1"),
                hasIngressVrf(nullValue()),
                hasSrcIp(new Ip("1.1.1.0")),
                hasIpProtocol(IpProtocol.UDP),
                hasTag(TAG))));
    assertThat(
        flows,
        hasItem(
            allOf(
                hasIngressInterface(FAST_ETHERNET),
                hasIngressNode("node1"),
                hasIngressVrf(nullValue()),
                hasSrcIp(new Ip("1.1.1.0")),
                hasIpProtocol(IpProtocol.UDP),
                hasTag(TAG))));
  }

  @Test
  public void testGetFlows_dst() {
    TracerouteQuestion question =
        new TracerouteQuestion(
            String.format("%s[%s]", NODE1, LOOPBACK),
            PacketHeaderConstraints.builder().setDstIp("ofLocation(node2)").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, _batfish);
    Set<Flow> flows = answerer.getFlows(TAG);
    assertThat(
        flows, everyItem(anyOf(hasDstIp(NODE2_FAST_ETHERNET_IP), hasDstIp(NODE2_LOOPBACK_IP))));
  }

  /*
   * Build a simple 1-node network with an ACL
   */
  private static SortedMap<String, Configuration> aclNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    ImmutableSortedMap.Builder<String, Configuration> configs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());

    Configuration c1 = cb.build();
    configs.put(c1.getHostname(), c1);

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    // destination interface
    nf.interfaceBuilder()
        .setAddress(new InterfaceAddress("1.1.1.0/31"))
        .setOwner(c1)
        .setOutgoingFilter(
            nf.aclBuilder()
                .setOwner(c1)
                .setLines(ImmutableList.of(IpAccessListLine.REJECT_ALL))
                .build())
        .setVrf(v1)
        .build();

    return configs.build();
  }

  @Test
  public void testIgnoreFilters() throws IOException {
    SortedMap<String, Configuration> configs = aclNetwork();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.getSettings().setDebugFlags(ImmutableList.of("oldtraceroute"));
    batfish.computeDataPlane(false);
    PacketHeaderConstraints header = PacketHeaderConstraints.builder().setDstIp("1.1.1.1").build();

    TracerouteQuestion question = new TracerouteQuestion(".*", header, false, DEFAULT_MAX_TRACES);

    // without ignoreFilters we get DENIED_OUT
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer();
    assertThat(answer.getRows().getData(), hasSize(1));
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DENIED_OUT)),
                Schema.set(Schema.FLOW_TRACE))));

    // with ignoreFilters we get DELIVERED_TO_SUBNET, since the dst ip is in the interface subnet
    question = new TracerouteQuestion(".*", header, true, DEFAULT_MAX_TRACES);
    answerer = new TracerouteAnswerer(question, batfish);
    answer = (TableAnswerElement) answerer.answer();
    assertThat(answer.getRows().getData(), hasSize(1));
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.FLOW_TRACE))));
  }

  /*
   * Topology: R1 -- R2
   * R1 interface IP: 1.0.0.1/24
   * R2 interface IP: 1.0.0.2/24
   *
   * R1 static route: 1.0.0.128/26 -> interface on R1
   *
   * traceroute R1 -> 1.0.0.129
   *
   * R1 should deliver the packet to the subnet
   *
   */
  @Test
  public void testDeliveredToSubnet1() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    ImmutableSortedMap.Builder<String, Configuration> configs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());

    Configuration c1 = cb.build();
    configs.put(c1.getHostname(), c1);

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    // set up interface
    Interface i1 =
        nf.interfaceBuilder()
            .setAddress(new InterfaceAddress("1.0.0.1/24"))
            .setOwner(c1)
            .setVrf(v1)
            .build();

    // set up static route "1.0.0.128/26" -> i1
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopInterface(i1.getName())
                .setNetwork(Prefix.parse("1.0.0.128/26"))
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();

    // set up interface on N2
    nf.interfaceBuilder()
        .setAddress(new InterfaceAddress("1.0.0.2/24"))
        .setOwner(c2)
        .setVrf(v2)
        .setProxyArp(true)
        .build();

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.getSettings().setDebugFlags(ImmutableList.of("oldtraceroute"));
    batfish.computeDataPlane(false);

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("1.0.0.129").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer();
    // should only have one trace
    assertThat(answer.getRows().getData(), hasSize(1));

    // the trace should only traverse R1
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(COL_TRACES, everyItem(hasHops(hasSize(1))), Schema.set(Schema.FLOW_TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.FLOW_TRACE))));
  }

  /*
   * Topology: R1 -- R2
   * R1 interface IP: 1.0.0.1/24
   * R2 interface IP: 1.0.0.2/24
   *
   * R1 static route: 1.0.0.128/26 -> 1.0.0.2
   *
   * traceroute R1 -> 1.0.0.129
   *
   * Expected: R1 -> R2 -> deliver to the subnet
   *
   */
  @Test
  public void testDeliveredToSubnet2() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    ImmutableSortedMap.Builder<String, Configuration> configs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());

    Configuration c1 = cb.build();
    configs.put(c1.getHostname(), c1);

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    // set up interface
    Interface i1 =
        nf.interfaceBuilder()
            .setAddress(new InterfaceAddress("1.0.0.1/24"))
            .setOwner(c1)
            .setVrf(v1)
            .build();

    // set up static route "1.0.0.128/26" -> "1.0.0.2"
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopInterface(i1.getName())
                .setNextHopIp(new Ip("1.0.0.2"))
                .setNetwork(Prefix.parse("1.0.0.128/26"))
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();

    // set up interface on N2
    nf.interfaceBuilder()
        .setAddress(new InterfaceAddress("1.0.0.2/24"))
        .setOwner(c2)
        .setVrf(v2)
        .setProxyArp(true)
        .build();

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.getSettings().setDebugFlags(ImmutableList.of("oldtraceroute"));
    batfish.computeDataPlane(false);

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("1.0.0.129").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer();

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(COL_TRACES, everyItem(hasHops(hasSize(2))), Schema.set(Schema.FLOW_TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.FLOW_TRACE))));
  }

  /*
   * Topology:
   * R1 -- R2 --
   * R1 interface1 IP: 1.0.0.1/24
   * R2 interface1 IP: 1.0.0.2/24
   * R2 interface2 IP: 1.0.0.129/mask
   *
   * R1 static route 1.0.0.128/mask -> interface1
   * R2 static route 1.0.0.128/mask -> interface2
   *
   * traceroute R1 -> 1.0.0.130
   *
   * Case 1: mask <= 24 : R1 -> DELIVERED_TO_SUBNET
   *  since connected route has higher priority than static route
   * Case 2: mask > 24 : DELIVERED_TO_SUBNET with trace R1 -> R2
   *
   * Note: in practice, R2 would complain about overlapping interfaces
   */
  private TableAnswerElement testDeliveredToSubnetVSStaticRoute(String mask) throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    ImmutableSortedMap.Builder<String, Configuration> configs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());

    Configuration c1 = cb.build();
    configs.put(c1.getHostname(), c1);

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    // destination interface
    Interface n1i1 =
        nf.interfaceBuilder()
            .setAddress(new InterfaceAddress("1.0.0.1/24"))
            .setOwner(c1)
            .setVrf(v1)
            .setProxyArp(true)
            .build();

    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopInterface(n1i1.getName())
                .setNetwork(Prefix.parse("1.0.0.128/" + mask))
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();

    nf.interfaceBuilder()
        .setAddress(new InterfaceAddress("1.0.0.2/24"))
        .setOwner(c2)
        .setVrf(v2)
        .setProxyArp(true)
        .build();

    Interface n2i2 =
        nf.interfaceBuilder()
            .setAddress(new InterfaceAddress("1.0.0.129/" + mask))
            .setOwner(c2)
            .setVrf(v2)
            .setProxyArp(true)
            .build();

    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopInterface(n2i2.getName())
                .setNetwork(Prefix.parse("1.0.0.128/" + mask))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.getSettings().setDebugFlags(ImmutableList.of("oldtraceroute"));
    batfish.computeDataPlane(false);

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("1.0.0.130").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer();
    return answer;
  }

  @Test
  public void testDeliveredToSubnetVSStaticRoute1() throws IOException {
    TableAnswerElement answer = testDeliveredToSubnetVSStaticRoute("24");
    assertThat(answer.getRows().getData(), hasSize(1));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(COL_TRACES, everyItem(hasHops(hasSize(1))), Schema.set(Schema.FLOW_TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.FLOW_TRACE))));
  }

  @Test
  public void testDeliveredToSubnetVSStaticRoute2() throws IOException {
    TableAnswerElement answer = testDeliveredToSubnetVSStaticRoute("26");
    assertThat(answer.getRows().getData(), hasSize(1));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(COL_TRACES, everyItem(hasHops(hasSize(2))), Schema.set(Schema.FLOW_TRACE))));

    // check packets traverse R2 as the last hop
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasHop(1, hasEdge(hasNode1(equalTo("~Configuration_1~"))))),
                Schema.set(Schema.FLOW_TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.FLOW_TRACE))));
  }

  /*
   * -- R1 -- R2 --
   * R1 interface1 IP: 1.0.0.130/mask
   * R1 interface2 IP: 2.0.0.1/24
   * R2 interface1 IP: 2.0.0.2/24
   * R2 interface2 IP: 1.0.0.129/24
   *
   * Static routes:
   * R1: 1.0.0.128/24 -> interface2
   * R2: 1.0.0.128/24 -> interface2
   *
   * Traceroute: R1 -> 1.0.0.131
   */
  private TableAnswerElement testDispositionMultiInterfaces(String mask) throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    ImmutableSortedMap.Builder<String, Configuration> configs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());

    Configuration c1 = cb.build();
    configs.put(c1.getHostname(), c1);

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    nf.interfaceBuilder()
        .setAddress(new InterfaceAddress("1.0.0.130/" + mask))
        .setOwner(c1)
        .setVrf(v1)
        .setProxyArp(true)
        .build();

    Interface n1i1 =
        nf.interfaceBuilder()
            .setAddress(new InterfaceAddress("2.0.0.1/24"))
            .setOwner(c1)
            .setVrf(v1)
            .setProxyArp(true)
            .build();

    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopInterface(n1i1.getName())
                .setNetwork(Prefix.parse("1.0.0.128/24"))
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();

    nf.interfaceBuilder()
        .setAddress(new InterfaceAddress("2.0.0.2/24"))
        .setOwner(c2)
        .setVrf(v2)
        .setProxyArp(true)
        .build();

    Interface n2i1 =
        nf.interfaceBuilder()
            .setAddress(new InterfaceAddress("1.0.0.129/24"))
            .setOwner(c2)
            .setVrf(v2)
            .setProxyArp(true)
            .build();

    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopInterface(n2i1.getName())
                .setNetwork(Prefix.parse("1.0.0.128/24"))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.getSettings().setDebugFlags(ImmutableList.of("oldtraceroute"));
    batfish.computeDataPlane(false);

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("1.0.0.131").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer();

    return answer;
  }

  /*
   * If mask > 24: DELIVERD_TO_SUBNET (to R1)
   */
  @Test
  public void testDispositionMultiInterfaces() throws IOException {
    TableAnswerElement answer = testDispositionMultiInterfaces("25");
    assertThat(answer.getRows().getData(), hasSize(2));

    // check that packet should reach R1
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(COL_TRACES, everyItem(hasHops(hasSize(1))), Schema.set(Schema.FLOW_TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.FLOW_TRACE))));
  }

  /*
   * Topology: R1 -- R2 -- R3 --
   *
   * R1 interface1 IP: 1.0.0.1/mask
   * R2 interface1 IP: 1.0.0.2/mask
   * R2 interface2 IP: 2.0.0.1/24
   * R3 interface1 IP: 2.0.0.2/24
   * R3 interface2 IP: 1.0.0.3/24
   *
   * Static routes:
   * R1: 1.0.0.0/25 -> interface1
   * R2: 1.0.0.0/25 -> interface2
   * R3: 1.0.0.0/25 -> interface2
   *
   * Traceroute: R1 -> 1.0.0.4
   *
   * Case 1: mask = 24: LOOP (R1->R2->R3->R2), since packets
   *   take interface1 on R1, and then R2 followed by R3 before coming back to R2.
   *   Note that R1 does not reply arp request from R3, but R2 does.
   * Case 2: mask >= 25: NEIGHBOR_UNREACHABLE
   *    since the connected route is taken, and R1 should deliver the packet to subnet
   */
  private TableAnswerElement testDispositionMultipleRouters(String mask) throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    ImmutableSortedMap.Builder<String, Configuration> configs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());

    Configuration c1 = cb.build();
    configs.put(c1.getHostname(), c1);

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    Interface n1i0 =
        nf.interfaceBuilder()
            .setAddress(new InterfaceAddress("1.0.0.1/" + mask))
            .setOwner(c1)
            .setVrf(v1)
            .setProxyArp(true)
            .build();

    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopInterface(n1i0.getName())
                .setNetwork(Prefix.parse("1.0.0.0/25"))
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();

    nf.interfaceBuilder()
        .setAddress(new InterfaceAddress("1.0.0.2/" + mask))
        .setOwner(c2)
        .setVrf(v2)
        .setProxyArp(true)
        .build();

    Interface n2i1 =
        nf.interfaceBuilder()
            .setAddress(new InterfaceAddress("2.0.0.1/24"))
            .setOwner(c2)
            .setVrf(v2)
            .setProxyArp(true)
            .build();

    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopInterface(n2i1.getName())
                .setNetwork(Prefix.parse("1.0.0.0/25"))
                .setAdministrativeCost(1)
                .build()));

    Configuration c3 = cb.build();
    configs.put(c3.getHostname(), c3);

    Vrf v3 = nf.vrfBuilder().setOwner(c3).build();

    nf.interfaceBuilder()
        .setAddress(new InterfaceAddress("2.0.0.2/24"))
        .setOwner(c3)
        .setVrf(v3)
        .setProxyArp(true)
        .build();

    Interface n3i1 =
        nf.interfaceBuilder()
            .setAddress(new InterfaceAddress("1.0.0.3/24"))
            .setOwner(c3)
            .setVrf(v3)
            .setProxyArp(true)
            .build();

    v3.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopInterface(n3i1.getName())
                .setNetwork(Prefix.parse("1.0.0.0/25"))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.getSettings().setDebugFlags(ImmutableList.of("oldtraceroute"));
    batfish.computeDataPlane(false);

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("1.0.0.4").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer();

    return answer;
  }

  @Test
  public void testDispositionMultipleRouters1() throws IOException {
    TableAnswerElement answer = testDispositionMultipleRouters("24");
    assertThat(answer.getRows().getData(), hasSize(1));

    // check that packet should traverse R1 - R2 - R3 - R2
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(COL_TRACES, everyItem(hasHops(hasSize(4))), Schema.set(Schema.FLOW_TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasHop(3, hasEdge(hasNode1(equalTo("~Configuration_1~"))))),
                Schema.set(Schema.FLOW_TRACE))));

    // check disposition
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.LOOP)),
                Schema.set(Schema.FLOW_TRACE))));
  }

  @Test
  public void testDispositionMultipleRouters2() throws IOException {
    TableAnswerElement answer = testDispositionMultipleRouters("25");
    assertThat(answer.getRows().getData(), hasSize(1));

    // check that packet only traverse R1
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(COL_TRACES, everyItem(hasHops(hasSize(1))), Schema.set(Schema.FLOW_TRACE))));

    // check disposition
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.FLOW_TRACE))));
  }

  private Batfish maxTracesBatfish() throws IOException {
    // create a simple network with two paths for a given destination
    Prefix dst = Prefix.parse("1.1.1.1/32");
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Interface.Builder ifaceBuilder =
        nf.interfaceBuilder().setActive(true).setOwner(config).setVrf(vrf);
    Interface lo0 = ifaceBuilder.setAddress(new InterfaceAddress("5.5.5.5/32")).build();
    Interface lo1 = ifaceBuilder.setAddress(new InterfaceAddress("6.6.6.6/32")).build();
    Builder routeBuilder = StaticRoute.builder().setNetwork(dst).setAdministrativeCost(1);
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            routeBuilder.setNextHopInterface(lo0.getName()).build(),
            routeBuilder.setNextHopInterface(lo1.getName()).build()));

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(config.getHostname(), config), _folder);
    batfish.computeDataPlane(false);
    return batfish;
  }

  @Test
  public void testMaxTraces_default() throws IOException {
    Batfish batfish = maxTracesBatfish();

    TracerouteQuestion question =
        new TracerouteQuestion(
            ".*",
            PacketHeaderConstraints.builder().setSrcIp("1.1.1.0").setDstIp("1.1.1.1").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer();
    assertThat(
        answer,
        hasRows(
            contains(
                allOf(
                    hasColumn(TracerouteAnswerer.COL_TRACES, hasSize(2), Schema.set(Schema.TRACE)),
                    hasColumn(TracerouteAnswerer.COL_TRACE_COUNT, equalTo(2), Schema.INTEGER)))));
  }

  @Test
  public void testMaxTraces_limited() throws IOException {
    Batfish batfish = maxTracesBatfish();
    TracerouteQuestion question =
        new TracerouteQuestion(
            ".*",
            PacketHeaderConstraints.builder().setSrcIp("1.1.1.0").setDstIp("1.1.1.1").build(),
            false,
            1);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer();
    assertThat(
        answer,
        hasRows(
            contains(
                allOf(
                    hasColumn(TracerouteAnswerer.COL_TRACES, hasSize(1), Schema.set(Schema.TRACE)),
                    hasColumn(TracerouteAnswerer.COL_TRACE_COUNT, equalTo(2), Schema.INTEGER)))));
  }

  /*
   *  Test setup: Send packets with destination 8.8.8.8 to the next hop 1.0.0.2.
   *  Since the destination Ip 8.8.8.8 is outside the network and next hop Ip 1.0.0.2 is not
   *  owned by any device, so this is EXITS_NETWORK.
   */
  @Test
  public void testInsufficientInfoVsExitsNetwork1() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    ImmutableSortedMap.Builder<String, Configuration> configs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());
    Configuration c1 = cb.build();
    configs.put(c1.getHostname(), c1);

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    // set up interface
    nf.interfaceBuilder()
        .setAddress(new InterfaceAddress("1.0.0.1/30"))
        .setOwner(c1)
        .setVrf(v1)
        .build();

    // set up static route "8.8.8.0/24" -> 1.0.0.2
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopIp(new Ip("1.0.0.2"))
                .setNetwork(Prefix.parse("8.8.8.0/24"))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.computeDataPlane(false);

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("8.8.8.8").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer();

    // should only have one trace with disposition EXITS_NETWORK
    assertThat(
        answer,
        hasRows(
            contains(
                allOf(
                    hasColumn(TracerouteAnswerer.COL_TRACES, hasSize(1), Schema.set(Schema.TRACE)),
                    hasColumn(
                        TracerouteAnswerer.COL_TRACES,
                        containsInAnyOrder(
                            ImmutableList.of(
                                TraceMatchers.hasDisposition(FlowDisposition.EXITS_NETWORK))),
                        Schema.set(Schema.TRACE))))));
  }

  /*
   *  Test setup: Send packets with destination 8.8.8.8 to the next hop 2.0.0.2.
   *  Since the destination Ip 8.8.8.8 is outside the network and next hop Ip 2.0.0.2 is
   *  owned by a device, so this is INSUFFICIENT_INFO.
   */
  @Test
  public void testInsufficientInfoVsExitsNetwork2() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    ImmutableSortedMap.Builder<String, Configuration> configs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());
    Configuration c1 = cb.build();
    configs.put(c1.getHostname(), c1);

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    // set up interface
    Interface i1 =
        nf.interfaceBuilder()
            .setAddress(new InterfaceAddress("1.0.0.1/30"))
            .setOwner(c1)
            .setVrf(v1)
            .build();

    // set up another node
    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    nf.interfaceBuilder()
        .setAddresses(new InterfaceAddress("2.0.0.2/31"))
        .setOwner(c2)
        .setVrf(v2)
        .build();

    // set up static route "8.8.8.0/24" -> 2.0.0.2
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopIp(new Ip("2.0.0.2"))
                .setNextHopInterface(i1.getName())
                .setNetwork(Prefix.parse("8.8.8.0/24"))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.computeDataPlane(false);

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("8.8.8.8").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer();

    // should only have one trace with disposition INSUFFICIENT_INFO
    assertThat(
        answer,
        hasRows(
            contains(
                allOf(
                    hasColumn(TracerouteAnswerer.COL_TRACES, hasSize(1), Schema.set(Schema.TRACE)),
                    hasColumn(
                        TracerouteAnswerer.COL_TRACES,
                        containsInAnyOrder(
                            ImmutableList.of(
                                TraceMatchers.hasDisposition(FlowDisposition.INSUFFICIENT_INFO))),
                        Schema.set(Schema.TRACE))))));
  }
}
