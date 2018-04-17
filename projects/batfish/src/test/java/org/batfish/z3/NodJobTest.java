package org.batfish.z3;

import static org.batfish.datamodel.FlowDisposition.*;
import static org.batfish.datamodel.matchers.EdgeMatchers.*;
import static org.batfish.datamodel.matchers.FlowTraceHopMatchers.hasEdge;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.bdp.BdpDataPlanePlugin;
import org.batfish.common.Pair;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.FlowTraceHop;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.ForwardingAnalysisImpl;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.matchers.FlowTraceMatchers;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.z3.state.OriginateVrf;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NodJobTest {

  private BdpDataPlanePlugin _bdpDataPlanePlugin;
  private SortedMap<String, Configuration> _configs;
  private DataPlane _dataPlane;
  private Configuration _dstNode;
  private OriginateVrf _originateVrf;
  private Configuration _srcNode;
  private Vrf _srcVrf;
  private Synthesizer _synthesizer;

  private static Status checkSat(NodJob nodJob) {
    Context z3Context = new Context();
    SmtInput smtInput = nodJob.computeSmtInput(System.currentTimeMillis(), z3Context);
    Solver solver = z3Context.mkSolver();
    solver.add(smtInput._expr);
    return solver.check();
  }

  private NodJob getNodJob(HeaderSpace headerSpace) {
    return getNodJob(headerSpace, null);
  }

  private NodJob getNodJob(HeaderSpace headerSpace, Boolean srcNatted) {
    StandardReachabilityQuerySynthesizer querySynthesizer =
        StandardReachabilityQuerySynthesizer.builder()
            .setActions(ImmutableSet.of(ForwardingAction.ACCEPT))
            .setFinalNodes(ImmutableSet.of(_dstNode.getHostname()))
            .setHeaderSpace(headerSpace)
            .setIngressNodeVrfs(
                ImmutableMap.of(_srcNode.getHostname(), ImmutableSet.of(_srcVrf.getName())))
            .setSrcNatted(srcNatted)
            .setTransitNodes(ImmutableSet.of())
            .setNonTransitNodes(ImmutableSet.of())
            .build();
    SortedSet<Pair<String, String>> ingressNodes =
        ImmutableSortedSet.of(new Pair<>(_srcNode.getHostname(), _srcVrf.getName()));
    return new NodJob(new Settings(), _synthesizer, querySynthesizer, ingressNodes, "tag", false);
  }

  @Before
  public void setup() throws IOException {
    setupConfigs();
    setupDataPlane();
    setupSynthesizer();
  }

  private void setupConfigs() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    IpAccessList.Builder aclb = nf.aclBuilder();
    SourceNat.Builder snb = SourceNat.builder();
    Vrf.Builder vb = nf.vrfBuilder();

    _srcNode = cb.build();
    _dstNode = cb.build();
    _srcVrf = vb.setOwner(_srcNode).build();
    _originateVrf = new OriginateVrf(_srcNode.getHostname(), _srcVrf.getName());
    Vrf dstVrf = vb.setOwner(_dstNode).build();
    Prefix p1 = Prefix.parse("1.0.0.0/31");
    Ip poolIp1 = new Ip("1.0.0.10");

    // apply NAT to all packets
    IpAccessList sourceNat1Acl =
        aclb.setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(ImmutableList.of(new IpWildcard("3.0.0.0/32")))
                            .build())))
            .setOwner(_srcNode)
            .build();

    SourceNat sourceNat1 =
        // TODO add a test with poolIp1 to poolIp2. That will exercise the range logic,
        // which is complex and inscrutable. Consider replacing that with bv_lte and bv_gte.
        // Would be easier to understand, and Nuno says it will likely be more efficient.
        snb.setPoolIpFirst(poolIp1).setPoolIpLast(poolIp1).setAcl(sourceNat1Acl).build();
    ib.setOwner(_srcNode)
        .setVrf(_srcVrf)
        .setAddress(new InterfaceAddress(p1.getStartIp(), p1.getPrefixLength()))
        .setSourceNats(ImmutableList.of(sourceNat1))
        .build();
    ib.setOwner(_dstNode)
        .setVrf(dstVrf)
        .setAddress(new InterfaceAddress(p1.getEndIp(), p1.getPrefixLength()))
        .setSourceNats(ImmutableList.of())
        .build();

    // For the destination
    Prefix pDest = Prefix.parse("2.0.0.0/32");
    ib.setOwner(_dstNode)
        .setVrf(dstVrf)
        .setAddress(new InterfaceAddress(pDest.getEndIp(), pDest.getPrefixLength()))
        .build();

    StaticRoute.Builder bld = StaticRoute.builder().setNetwork(pDest);
    _srcVrf.getStaticRoutes().add(bld.setNextHopIp(p1.getEndIp()).build());

    _configs =
        ImmutableSortedMap.of(
            _srcNode.getName(), _srcNode,
            _dstNode.getName(), _dstNode);
  }

  private void setupDataPlane() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    Batfish batfish = BatfishTestUtils.getBatfish(_configs, tmp);
    _bdpDataPlanePlugin = new BdpDataPlanePlugin();
    _bdpDataPlanePlugin.initialize(batfish);
    batfish.registerDataPlanePlugin(_bdpDataPlanePlugin, "bdp");
    batfish.computeDataPlane(false);
    _dataPlane = batfish.loadDataPlane();
  }

  private void setupSynthesizer() {
    Topology topology = new Topology(_dataPlane.getTopologyEdges());
    SynthesizerInput input =
        SynthesizerInputImpl.builder()
            .setConfigurations(_configs)
            .setForwardingAnalysis(
                new ForwardingAnalysisImpl(
                    _configs, _dataPlane.getRibs(), _dataPlane.getFibs(), topology))
            .setSimplify(false)
            .setTopology(topology)
            .build();

    _synthesizer = new Synthesizer(input);
  }

  /** Test that traffic originating from 3.0.0.0 is NATed */
  @Test
  public void testNatted() {
    HeaderSpace headerSpace = new HeaderSpace();
    headerSpace.setSrcIps(ImmutableList.of(new IpWildcard("3.0.0.0")));
    NodJob nodJob = getNodJob(headerSpace);

    Context z3Context = new Context();
    SmtInput smtInput = nodJob.computeSmtInput(System.currentTimeMillis(), z3Context);

    Map<OriginateVrf, Map<String, Long>> fieldConstraintsByOriginateVrf =
        nodJob.getOriginateVrfConstraints(z3Context, smtInput);
    assertThat(fieldConstraintsByOriginateVrf.entrySet(), hasSize(1));
    assertThat(fieldConstraintsByOriginateVrf, hasKey(_originateVrf));
    Map<String, Long> fieldConstraints = fieldConstraintsByOriginateVrf.get(_originateVrf);

    // Only one OriginateVrf choice, so this must be 0
    assertThat(
        fieldConstraints,
        hasEntry(OriginateVrfInstrumentation.ORIGINATE_VRF_FIELD_NAME, new Long(0)));
    assertThat(fieldConstraints, hasEntry(Field.ORIG_SRC_IP.getName(), new Ip("3.0.0.0").asLong()));
    assertThat(
        fieldConstraints,
        hasEntry(equalTo(Field.SRC_IP.getName()), not(equalTo(new Ip("3.0.0.0").asLong()))));
    assertThat(fieldConstraints, hasEntry(Field.SRC_IP.getName(), new Ip("1.0.0.10").asLong()));

    Set<Flow> flows = nodJob.getFlows(fieldConstraintsByOriginateVrf);
    _bdpDataPlanePlugin.processFlows(flows, _dataPlane);
    List<FlowTrace> flowTraces = _bdpDataPlanePlugin.getHistoryFlowTraces(_dataPlane);

    flowTraces.forEach(
        trace -> {
          assertThat(trace.getNotes(), is("ACCEPTED"));
          List<FlowTraceHop> hops = trace.getHops();
          assertThat(hops, hasSize(1));
          FlowTraceHop hop = hops.get(0);
          assertThat(hop.getTransformedFlow(), notNullValue());
          assertThat(hop.getTransformedFlow().getSrcIp(), equalTo(new Ip("1.0.0.10")));
        });
  }

  /**
   * Test that traffic originating from 3.0.0.0 that is expected to be NATed returns SAT when we
   * constrain to only allow NATed results.
   */
  @Test
  public void testNattedSat() {
    HeaderSpace headerSpace = new HeaderSpace();
    headerSpace.setSrcIps(ImmutableList.of(new IpWildcard("3.0.0.0")));
    NodJob nodJob = getNodJob(headerSpace, true);
    assertThat(checkSat(nodJob), equalTo(Status.SATISFIABLE));
  }

  /**
   * Test that traffic originating from 3.0.0.0 that is expected to be NATed returns UNSAT when we
   * constrain to only allow NOT-NATed results.
   */
  @Test
  public void testNattedUnsat() {
    HeaderSpace headerSpace = new HeaderSpace();
    headerSpace.setSrcIps(ImmutableList.of(new IpWildcard("3.0.0.0")));
    NodJob nodJob = getNodJob(headerSpace, false);
    assertThat(checkSat(nodJob), equalTo(Status.UNSATISFIABLE));
  }

  /** Test that traffic originating from 3.0.0.1 is not NATed */
  @Test
  public void testNotNatted() {
    HeaderSpace headerSpace = new HeaderSpace();
    headerSpace.setSrcIps(ImmutableList.of(new IpWildcard("3.0.0.1")));
    NodJob nodJob = getNodJob(headerSpace);

    Context z3Context = new Context();
    SmtInput smtInput = nodJob.computeSmtInput(System.currentTimeMillis(), z3Context);

    Map<OriginateVrf, Map<String, Long>> fieldConstraintsByOriginateVrf =
        nodJob.getOriginateVrfConstraints(z3Context, smtInput);
    assertThat(fieldConstraintsByOriginateVrf.entrySet(), hasSize(1));
    assertThat(fieldConstraintsByOriginateVrf, hasKey(_originateVrf));
    Map<String, Long> fieldConstraints = fieldConstraintsByOriginateVrf.get(_originateVrf);

    assertThat(
        fieldConstraints,
        hasEntry(OriginateVrfInstrumentation.ORIGINATE_VRF_FIELD_NAME, new Long(0)));
    assertThat(smtInput._variablesAsConsts, hasKey("SRC_IP"));
    assertThat(fieldConstraints, hasKey(Field.SRC_IP.getName()));

    assertThat(fieldConstraints, hasEntry(Field.ORIG_SRC_IP.getName(), new Ip("3.0.0.1").asLong()));
    assertThat(fieldConstraints, hasEntry(Field.SRC_IP.getName(), new Ip("3.0.0.1").asLong()));

    Set<Flow> flows = nodJob.getFlows(fieldConstraintsByOriginateVrf);
    _bdpDataPlanePlugin.processFlows(flows, _dataPlane);
    List<FlowTrace> flowTraces = _bdpDataPlanePlugin.getHistoryFlowTraces(_dataPlane);

    flowTraces.forEach(
        trace -> {
          assertThat(trace.getNotes(), is("ACCEPTED"));
          List<FlowTraceHop> hops = trace.getHops();
          assertThat(hops, hasSize(1));
          FlowTraceHop hop = hops.get(0);
          assertThat(hop.getTransformedFlow(), nullValue());
        });
  }

  /**
   * Test that traffic originating from 3.0.0.1 that is expected NOT to be NATed returns SAT when we
   * constrain to only allow NOT-NATed results.
   */
  @Test
  public void testNotNattedSat() {
    HeaderSpace headerSpace = new HeaderSpace();
    headerSpace.setSrcIps(ImmutableList.of(new IpWildcard("3.0.0.1")));
    NodJob nodJob = getNodJob(headerSpace, false);
    assertThat(checkSat(nodJob), equalTo(Status.SATISFIABLE));
  }

  /**
   * Test that traffic originating from 3.0.0.1 that is expected NOT to be NATed returns UNSAT when
   * we constrain to only allow NATed results.
   */
  @Test
  public void testNotNattedUnsat() {
    HeaderSpace headerSpace = new HeaderSpace();
    headerSpace.setSrcIps(ImmutableList.of(new IpWildcard("3.0.0.1")));
    NodJob nodJob = getNodJob(headerSpace, true);
    assertThat(checkSat(nodJob), equalTo(Status.UNSATISFIABLE));
  }

  /**
   * Test MatchSrcInterface AclLineMatchExpr. Build a network with two nodes srcNode and dstNode.
   * Traffic will originate at srcNode and be sent to dstNode. The two nodes can communicate via two
   * subnets (two interfaces on each node), and dstNode has another interface on the destination
   * subnet. That destination interface has an outgoing filter that only accepts traffic from
   * srcNode if it arrived on one of the two interfaces dstNode can receive traffic from srcNode on.
   */
  @Test
  public void testMatchSrcInterface() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    IpAccessList.Builder aclb = nf.aclBuilder();
    Vrf.Builder vb = nf.vrfBuilder();

    String iface1 = "iface1";
    String iface2 = "iface2";

    Configuration srcNode = cb.build();
    Configuration dstNode = cb.build();
    Vrf srcVrf = vb.setOwner(srcNode).build();
    OriginateVrf originateVrf = new OriginateVrf(srcNode.getHostname(), srcVrf.getName());
    Vrf dstVrf = vb.setOwner(dstNode).build();

    // create the ACL that only accepts traffic that entered via iface1
    IpAccessList matchSrcInterfaceAcl =
        aclb.setLines(
                ImmutableList.of(
                    IpAccessListLine.builder()
                        .setAction(LineAction.ACCEPT)
                        .setMatchCondition(new MatchSrcInterface(ImmutableList.of(iface1)))
                        .build()))
            .setOwner(dstNode)
            .build();

    ib.setOwner(srcNode)
        .setVrf(srcVrf)
        .setAddresses(new InterfaceAddress(new Ip("1.0.0.0"), 8))
        .build();
    ib.setAddresses(new InterfaceAddress(new Ip("2.0.0.0"), 8)).build();

    // create iface1
    ib.setOwner(dstNode)
        .setVrf(dstVrf)
        .setName(iface1)
        .setAddresses(new InterfaceAddress(new Ip("1.0.0.1"), 8))
        .build();

    // create iface2
    ib.setOwner(dstNode)
        .setVrf(dstVrf)
        .setName(iface2)
        .setAddresses(new InterfaceAddress(new Ip("2.0.0.1"), 8))
        .build();

    // For the destination
    Prefix pDest = Prefix.parse("3.0.0.0/8");
    ib.setOwner(dstNode)
        .setVrf(dstVrf)
        .setName("iface3")
        .setAddresses(new InterfaceAddress(pDest.getEndIp(), pDest.getPrefixLength()))
        .setOutgoingFilter(matchSrcInterfaceAcl)
        .build();

    StaticRoute.Builder bld = StaticRoute.builder().setNetwork(pDest);
    srcVrf.getStaticRoutes().add(bld.setNextHopIp(new Ip("1.0.0.1")).build());
    srcVrf.getStaticRoutes().add(bld.setNextHopIp(new Ip("2.0.0.1")).build());

    ImmutableSortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(srcNode.getName(), srcNode, dstNode.getName(), dstNode);

    /* set up data plane */
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, tmp);
    BdpDataPlanePlugin bdpDataPlanePlugin = new BdpDataPlanePlugin();
    bdpDataPlanePlugin.initialize(batfish);
    batfish.registerDataPlanePlugin(bdpDataPlanePlugin, "bdp");
    batfish.computeDataPlane(false);
    DataPlane dataPlane = batfish.loadDataPlane();

    /* set up synthesizer */
    Topology topology = new Topology(dataPlane.getTopologyEdges());
    SynthesizerInput input =
        SynthesizerInputImpl.builder()
            .setConfigurations(configs)
            .setForwardingAnalysis(
                new ForwardingAnalysisImpl(
                    configs, dataPlane.getRibs(), dataPlane.getFibs(), topology))
            .setSimplify(false)
            .setTopology(topology)
            .build();
    Synthesizer synthesizer = new Synthesizer(input);

    /* Construct NodJob */
    HeaderSpace headerSpace =
        HeaderSpace.builder()
            .setSrcIps(ImmutableList.of(new IpWildcard("1.1.1.1/32")))
            .setDstIps(ImmutableList.of(new IpWildcard("3.0.0.1/32")))
            .build();

    StandardReachabilityQuerySynthesizer querySynthesizer =
        StandardReachabilityQuerySynthesizer.builder()
            .setActions(ImmutableSet.of(ForwardingAction.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK))
            .setFinalNodes(ImmutableSet.of(dstNode.getHostname()))
            .setHeaderSpace(headerSpace)
            .setIngressNodeVrfs(
                ImmutableMap.of(srcNode.getHostname(), ImmutableSet.of(srcVrf.getName())))
            .build();
    SortedSet<Pair<String, String>> ingressNodes =
        ImmutableSortedSet.of(new Pair<>(srcNode.getHostname(), srcVrf.getName()));
    NodJob nodJob =
        new NodJob(new Settings(), synthesizer, querySynthesizer, ingressNodes, "tag", false);

    /* Run query */
    Context z3Context = new Context();
    SmtInput smtInput = nodJob.computeSmtInput(System.currentTimeMillis(), z3Context);

    Map<OriginateVrf, Map<String, Long>> fieldConstraintsByOriginateVrf =
        nodJob.getOriginateVrfConstraints(z3Context, smtInput);
    assertThat(fieldConstraintsByOriginateVrf.entrySet(), hasSize(1));
    assertThat(fieldConstraintsByOriginateVrf, hasKey(originateVrf));
    Map<String, Long> fieldConstraints = fieldConstraintsByOriginateVrf.get(originateVrf);

    assertThat(
        fieldConstraints,
        hasEntry(OriginateVrfInstrumentation.ORIGINATE_VRF_FIELD_NAME, new Long(0)));
    assertThat(smtInput._variablesAsConsts, hasKey("SRC_IP"));
    assertThat(fieldConstraints, hasKey(Field.SRC_IP.getName()));

    assertThat(fieldConstraints, hasEntry(Field.ORIG_SRC_IP.getName(), new Ip("1.1.1.1").asLong()));
    assertThat(fieldConstraints, hasEntry(Field.SRC_IP.getName(), new Ip("1.1.1.1").asLong()));

    Set<Flow> flows = nodJob.getFlows(fieldConstraintsByOriginateVrf);
    bdpDataPlanePlugin.processFlows(flows, dataPlane);
    List<FlowTrace> flowTraces = bdpDataPlanePlugin.getHistoryFlowTraces(dataPlane);
    assertThat(flowTraces, hasSize(2));
    assertThat(
        flowTraces,
        containsInAnyOrder(
            /* One trace should enter dstNode through iface1 and then pass the outgoing filter.
             * Specifically, the first hop should have an edge with int2=iface1.
             * We don't care about the second hop, so contains may not be the right matcher to
             * use here.
             */
            allOf(
                FlowTraceMatchers.hasDisposition(NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK),
                FlowTraceMatchers.hasHops(
                    contains(hasEdge(hasInt2(iface1)), any(FlowTraceHop.class)))),
            /* One trace should enter dstNode through iface2 and then be dropped by the outgoing
             * filter. The first hop should have an edge with int2=iface2.
             * We don't care about the second hop, so contains may not be the right matcher to
             * use here.
             */
            allOf(
                FlowTraceMatchers.hasDisposition(DENIED_OUT),
                FlowTraceMatchers.hasHops(
                    contains(hasEdge(hasInt2(iface2)), any(FlowTraceHop.class))))));
  }
}
