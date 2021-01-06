package org.batfish.question.traceroute;

import static org.batfish.common.util.TracePruner.DEFAULT_MAX_TRACES;
import static org.batfish.datamodel.FlowDiff.flowDiff;
import static org.batfish.datamodel.matchers.HopMatchers.hasNodeName;
import static org.batfish.datamodel.matchers.HopMatchers.hasSteps;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.TraceMatchers.hasHops;
import static org.batfish.datamodel.matchers.TraceMatchers.hasLastHop;
import static org.batfish.datamodel.matchers.TraceMatchers.hasNthHop;
import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.question.traceroute.TracerouteAnswerer.COL_TRACES;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.StaticRoute.Builder;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.flow.BidirectionalTrace;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.TransformationStep;
import org.batfish.datamodel.flow.TransformationStep.TransformationStepDetail;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link TracerouteQuestion}. */
public class TracerouteTest {
  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final String TESTRIGS_PREFIX = "org/batfish/allinone/testrigs/";
  private static final String TESTRIG_NAME = "specifiers-reachability";
  private static final List<String> TESTRIG_NODE_NAMES = ImmutableList.of(NODE1, NODE2);

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String ALL = ".*";

  private Batfish _batfish;

  @Before
  public void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + TESTRIG_NAME, TESTRIG_NODE_NAMES)
                .build(),
            _folder);

    _batfish.computeDataPlane(_batfish.getSnapshot());
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
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.0/31"))
        .setOwner(c1)
        .setOutgoingFilter(
            nf.aclBuilder().setOwner(c1).setLines(ImmutableList.of(ExprAclLine.REJECT_ALL)).build())
        .setVrf(v1)
        .build();

    return configs.build();
  }

  @Test
  public void testIgnoreFilters() throws IOException {
    SortedMap<String, Configuration> configs = aclNetwork();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    PacketHeaderConstraints header = PacketHeaderConstraints.builder().setDstIp("1.1.1.1").build();

    TracerouteQuestion question = new TracerouteQuestion(ALL, header, false, DEFAULT_MAX_TRACES);

    // without ignoreFilters we get DENIED_OUT
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());
    assertThat(answer.getRows().getData(), hasSize(1));
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DENIED_OUT)),
                Schema.set(Schema.TRACE))));

    // with ignoreFilters we get DELIVERED_TO_SUBNET, since the dst ip is in the interface subnet
    question = new TracerouteQuestion(ALL, header, true, DEFAULT_MAX_TRACES);
    answerer = new TracerouteAnswerer(question, batfish);
    answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());
    assertThat(answer.getRows().getData(), hasSize(1));
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.TRACE))));
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
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/24"))
            .setOwner(c1)
            .setVrf(v1)
            .build();

    // set up static route "1.0.0.128/26" -> i1
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(i1.getName())
                .setNetwork(Prefix.parse("1.0.0.128/26"))
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();

    // set up interface on N2
    nf.interfaceBuilder()
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.2/24"))
        .setOwner(c2)
        .setVrf(v2)
        .setProxyArp(true)
        .build();

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("1.0.0.129").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());
    // should only have one trace
    assertThat(answer.getRows().getData(), hasSize(1));

    // the trace should only traverse R1
    assertThat(
        answer.getRows().getData(),
        everyItem(hasColumn(COL_TRACES, everyItem(hasHops(hasSize(1))), Schema.set(Schema.TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.TRACE))));
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
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/24"))
            .setOwner(c1)
            .setVrf(v1)
            .build();

    // set up static route "1.0.0.128/26" -> "1.0.0.2"
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(i1.getName())
                .setNextHopIp(Ip.parse("1.0.0.2"))
                .setNetwork(Prefix.parse("1.0.0.128/26"))
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();

    // set up interface on N2
    nf.interfaceBuilder()
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.2/24"))
        .setOwner(c2)
        .setVrf(v2)
        .setProxyArp(true)
        .build();

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("1.0.0.129").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());

    assertThat(
        answer.getRows().getData(),
        everyItem(hasColumn(COL_TRACES, everyItem(hasHops(hasSize(2))), Schema.set(Schema.TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.TRACE))));
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
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/24"))
            .setOwner(c1)
            .setVrf(v1)
            .setProxyArp(true)
            .build();

    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(n1i1.getName())
                .setNetwork(Prefix.parse("1.0.0.128/" + mask))
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();

    nf.interfaceBuilder()
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.2/24"))
        .setOwner(c2)
        .setVrf(v2)
        .setProxyArp(true)
        .build();

    Interface n2i2 =
        nf.interfaceBuilder()
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.129/" + mask))
            .setOwner(c2)
            .setVrf(v2)
            .setProxyArp(true)
            .build();

    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(n2i2.getName())
                .setNetwork(Prefix.parse("1.0.0.128/" + mask))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("1.0.0.130").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());
    return answer;
  }

  @Test
  public void testDeliveredToSubnetVSStaticRoute1() throws IOException {
    TableAnswerElement answer = testDeliveredToSubnetVSStaticRoute("24");
    assertThat(answer.getRows().getData(), hasSize(1));

    assertThat(
        answer.getRows().getData(),
        everyItem(hasColumn(COL_TRACES, everyItem(hasHops(hasSize(1))), Schema.set(Schema.TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.TRACE))));
  }

  @Test
  public void testDeliveredToSubnetVSStaticRoute2() throws IOException {
    TableAnswerElement answer = testDeliveredToSubnetVSStaticRoute("26");
    assertThat(answer.getRows().getData(), hasSize(1));

    assertThat(
        answer.getRows().getData(),
        everyItem(hasColumn(COL_TRACES, everyItem(hasHops(hasSize(2))), Schema.set(Schema.TRACE))));

    // check packets traverse R2 as the last hop
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasLastHop(hasNodeName("~configuration_1~"))),
                Schema.set(Schema.TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.TRACE))));
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
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.130/" + mask))
        .setOwner(c1)
        .setVrf(v1)
        .setProxyArp(true)
        .build();

    Interface n1i1 =
        nf.interfaceBuilder()
            .setAddress(ConcreteInterfaceAddress.parse("2.0.0.1/24"))
            .setOwner(c1)
            .setVrf(v1)
            .setProxyArp(true)
            .build();

    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(n1i1.getName())
                .setNetwork(Prefix.parse("1.0.0.128/24"))
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();

    nf.interfaceBuilder()
        .setAddress(ConcreteInterfaceAddress.parse("2.0.0.2/24"))
        .setOwner(c2)
        .setVrf(v2)
        .setProxyArp(true)
        .build();

    Interface n2i1 =
        nf.interfaceBuilder()
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.129/24"))
            .setOwner(c2)
            .setVrf(v2)
            .setProxyArp(true)
            .build();

    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(n2i1.getName())
                .setNetwork(Prefix.parse("1.0.0.128/24"))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("1.0.0.131").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());

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
        everyItem(hasColumn(COL_TRACES, everyItem(hasHops(hasSize(1))), Schema.set(Schema.TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.TRACE))));
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
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/" + mask))
            .setOwner(c1)
            .setVrf(v1)
            .setProxyArp(true)
            .build();

    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(n1i0.getName())
                .setNetwork(Prefix.parse("1.0.0.0/25"))
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();

    nf.interfaceBuilder()
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.2/" + mask))
        .setOwner(c2)
        .setVrf(v2)
        .setProxyArp(true)
        .build();

    Interface n2i1 =
        nf.interfaceBuilder()
            .setAddress(ConcreteInterfaceAddress.parse("2.0.0.1/24"))
            .setOwner(c2)
            .setVrf(v2)
            .setProxyArp(true)
            .build();

    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(n2i1.getName())
                .setNetwork(Prefix.parse("1.0.0.0/25"))
                .setAdministrativeCost(1)
                .build()));

    Configuration c3 = cb.build();
    configs.put(c3.getHostname(), c3);

    Vrf v3 = nf.vrfBuilder().setOwner(c3).build();

    nf.interfaceBuilder()
        .setAddress(ConcreteInterfaceAddress.parse("2.0.0.2/24"))
        .setOwner(c3)
        .setVrf(v3)
        .setProxyArp(true)
        .build();

    Interface n3i1 =
        nf.interfaceBuilder()
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.3/24"))
            .setOwner(c3)
            .setVrf(v3)
            .setProxyArp(true)
            .build();

    v3.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(n3i1.getName())
                .setNetwork(Prefix.parse("1.0.0.0/25"))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("1.0.0.4").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());

    return answer;
  }

  @Test
  public void testDispositionMultipleRouters1() throws IOException {
    TableAnswerElement answer = testDispositionMultipleRouters("24");
    assertThat(answer.getRows().getData(), hasSize(1));

    // check that packet should traverse R1 - R2 - R3 - R2
    assertThat(
        answer.getRows().getData(),
        everyItem(hasColumn(COL_TRACES, everyItem(hasHops(hasSize(4))), Schema.set(Schema.TRACE))));

    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasNthHop(3, hasNodeName("~configuration_1~"))),
                Schema.set(Schema.TRACE))));

    // check disposition
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.LOOP)),
                Schema.set(Schema.TRACE))));
  }

  @Test
  public void testDispositionMultipleRouters2() throws IOException {
    TableAnswerElement answer = testDispositionMultipleRouters("25");
    assertThat(answer.getRows().getData(), hasSize(1));

    // check that packet only traverse R1
    assertThat(
        answer.getRows().getData(),
        everyItem(hasColumn(COL_TRACES, everyItem(hasHops(hasSize(1))), Schema.set(Schema.TRACE))));

    // check disposition
    assertThat(
        answer.getRows().getData(),
        everyItem(
            hasColumn(
                COL_TRACES,
                everyItem(hasDisposition(FlowDisposition.DELIVERED_TO_SUBNET)),
                Schema.set(Schema.TRACE))));
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
    Interface lo0 = ifaceBuilder.setAddress(ConcreteInterfaceAddress.parse("5.5.5.5/32")).build();
    Interface lo1 = ifaceBuilder.setAddress(ConcreteInterfaceAddress.parse("6.6.6.6/32")).build();
    Builder routeBuilder = StaticRoute.testBuilder().setNetwork(dst).setAdministrativeCost(1);
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            routeBuilder.setNextHopInterface(lo0.getName()).build(),
            routeBuilder.setNextHopInterface(lo1.getName()).build()));

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(config.getHostname(), config), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish;
  }

  @Test
  public void testMaxTraces_default() throws IOException {
    Batfish batfish = maxTracesBatfish();

    TracerouteQuestion question =
        new TracerouteQuestion(
            ALL,
            PacketHeaderConstraints.builder().setSrcIp("1.1.1.0").setDstIp("1.1.1.1").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());
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
            ALL,
            PacketHeaderConstraints.builder().setSrcIp("1.1.1.0").setDstIp("1.1.1.1").build(),
            false,
            1);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());
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
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/30"))
        .setOwner(c1)
        .setVrf(v1)
        .build();

    // set up static route "8.8.8.0/24" -> 1.0.0.2
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopIp(Ip.parse("1.0.0.2"))
                .setNetwork(Prefix.parse("8.8.8.0/24"))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("8.8.8.8").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());

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
                            ImmutableList.of(hasDisposition(FlowDisposition.EXITS_NETWORK))),
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
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/30"))
            .setOwner(c1)
            .setVrf(v1)
            .build();

    // set up another node
    Configuration c2 = cb.build();
    configs.put(c2.getHostname(), c2);

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    nf.interfaceBuilder()
        .setAddresses(ConcreteInterfaceAddress.parse("2.0.0.2/31"))
        .setOwner(c2)
        .setVrf(v2)
        .build();

    // set up static route "8.8.8.0/24" -> 2.0.0.2
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopIp(Ip.parse("2.0.0.2"))
                .setNextHopInterface(i1.getName())
                .setNetwork(Prefix.parse("8.8.8.0/24"))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish = BatfishTestUtils.getBatfish(configs.build(), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());

    TracerouteQuestion question =
        new TracerouteQuestion(
            c1.getHostname(),
            PacketHeaderConstraints.builder().setDstIp("8.8.8.8").build(),
            false,
            DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
    TableAnswerElement answer = (TableAnswerElement) answerer.answer(batfish.getSnapshot());

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
                            ImmutableList.of(hasDisposition(FlowDisposition.INSUFFICIENT_INFO))),
                        Schema.set(Schema.TRACE))))));
  }

  @Ignore("https://github.com/batfish/batfish/issues/3588")
  @Test
  public void testNatTable() throws IOException {
    String hostname = "ios-nat-table";
    String inside = "FastEthernet0/0";

    _batfish =
        BatfishTestUtils.getBatfishForTextConfigs(
            _folder, "org/batfish/allinone/testconfigs/" + hostname);

    NetworkSnapshot snapshot = _batfish.getSnapshot();
    _batfish.computeDataPlane(snapshot);

    Flow flow =
        Flow.builder()
            .setSrcIp(Ip.parse("1.0.0.2"))
            .setSrcPort(10000)
            .setDstIp(Ip.parse("2.0.0.1"))
            .setDstPort(20000)
            .setIngressNode(hostname)
            .setIngressInterface(inside)
            .setIpProtocol(IpProtocol.TCP)
            .build();

    List<BidirectionalTrace> traces =
        BidirectionalTracerouteAnswerer.computeBidirectionalTraces(
            ImmutableSet.of(flow), _batfish.getTracerouteEngine(snapshot), false);

    assertThat(traces, hasSize(1));

    BidirectionalTrace bidirectionalTrace = traces.get(0);

    // Reverse source nat should be applied to the return flow
    assertThat(
        bidirectionalTrace.getReverseTrace(),
        hasHops(
            contains(
                hasSteps(
                    containsInAnyOrder(
                        new TransformationStep(
                            new TransformationStepDetail(
                                TransformationType.DEST_NAT,
                                ImmutableSortedSet.of(
                                    flowDiff(
                                        DESTINATION, Ip.parse("10.0.0.1"), Ip.parse("1.0.0.2")))),
                            StepAction.TRANSFORMED))))));

    // Routing should NOT be skipped to the return flow thus resulting in NULL_ROUTED disposition
    assertThat(bidirectionalTrace.getReverseTrace(), hasDisposition(FlowDisposition.NULL_ROUTED));
  }
}
