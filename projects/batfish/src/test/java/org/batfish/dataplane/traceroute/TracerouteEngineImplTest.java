package org.batfish.dataplane.traceroute;

import static org.batfish.datamodel.ExprAclLine.ACCEPT_ALL;
import static org.batfish.datamodel.ExprAclLine.REJECT_ALL;
import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.ExprAclLine.rejecting;
import static org.batfish.datamodel.Flow.Builder;
import static org.batfish.datamodel.Flow.builder;
import static org.batfish.datamodel.FlowDiff.flowDiff;
import static org.batfish.datamodel.FlowDiff.flowDiffs;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.DENIED_OUT;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.INSUFFICIENT_INFO;
import static org.batfish.datamodel.FlowDisposition.LOOP;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.Ip.parse;
import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressNode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressVrf;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.datamodel.matchers.HopMatchers.hasNodeName;
import static org.batfish.datamodel.matchers.HopMatchers.hasSteps;
import static org.batfish.datamodel.matchers.TraceAndReverseFlowMatchers.hasNewFirewallSessions;
import static org.batfish.datamodel.matchers.TraceAndReverseFlowMatchers.hasReverseFlow;
import static org.batfish.datamodel.matchers.TraceAndReverseFlowMatchers.hasTrace;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.TraceMatchers.hasHops;
import static org.batfish.datamodel.transformation.Noop.NOOP_DEST_NAT;
import static org.batfish.datamodel.transformation.Noop.NOOP_SOURCE_NAT;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationPort;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.dataplane.traceroute.FlowTracer.matchSessionReturnFlow;
import static org.batfish.dataplane.traceroute.TracerouteUtils.getFinalActionForDisposition;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Accept;
import org.batfish.datamodel.flow.ArpErrorStep;
import org.batfish.datamodel.flow.DeliveredStep;
import org.batfish.datamodel.flow.EnterInputIfaceStep;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.FilterStep;
import org.batfish.datamodel.flow.FilterStep.FilterStepDetail;
import org.batfish.datamodel.flow.FilterStep.FilterType;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.ForwardOutInterface;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.IncomingSessionScope;
import org.batfish.datamodel.flow.MatchSessionStep;
import org.batfish.datamodel.flow.OriginateStep;
import org.batfish.datamodel.flow.RouteInfo;
import org.batfish.datamodel.flow.RoutingStep;
import org.batfish.datamodel.flow.SessionMatchExpr;
import org.batfish.datamodel.flow.SessionScope;
import org.batfish.datamodel.flow.SetupSessionStep;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.flow.TransformationStep;
import org.batfish.datamodel.flow.TransformationStep.TransformationStepDetail;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.dataplane.TracerouteEngineImpl;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link TracerouteEngineImpl} */
public class TracerouteEngineImplTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Rule public TemporaryFolder _tempFolder = new TemporaryFolder();

  private static Flow makeFlow() {
    Flow.Builder builder = Flow.builder();
    builder.setSrcIp(Ip.parse("1.2.3.4"));
    builder.setIngressNode("foo");
    return builder.build();
  }

  private static SessionMatchExpr makeMatchCriteria(Flow flow) {
    return new SessionMatchExpr(
        flow.getIpProtocol(),
        flow.getSrcIp(),
        flow.getDstIp(),
        flow.getSrcPort(),
        flow.getDstPort());
  }

  private static List<Trace> computeTraces(IBatfish batfish, NetworkSnapshot snapshot, Flow flow) {
    return computeTraces(batfish, snapshot, flow, false);
  }

  private static List<Trace> computeTraces(
      IBatfish batfish, NetworkSnapshot snapshot, Flow flow, boolean ignoreFilters) {
    List<Trace> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(ImmutableSet.of(flow), ignoreFilters)
            .get(flow);
    return BatfishObjectMapper.clone(traces, new TypeReference<List<Trace>>() {});
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

    Interface i1 = ib.setVrf(vrf1).setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24")).build();
    Interface i2 = ib.setVrf(vrf2).setAddress(ConcreteInterfaceAddress.parse("2.2.2.2/24")).build();
    ib.setVrf(vrf2).setAddress(ConcreteInterfaceAddress.parse("3.3.3.3/24")).build();

    // Compute data plane
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(config.getHostname(), config);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    // Construct flows
    Builder fb = builder().setDstIp(parse("3.3.3.3")).setIngressNode(config.getHostname());

    Flow flow1 = fb.setIngressInterface(i1.getName()).setIngressVrf(vrf1.getName()).build();
    Flow flow2 = fb.setIngressInterface(i2.getName()).setIngressVrf(vrf2.getName()).build();

    assertThat(computeTraces(batfish, snapshot, flow1), contains(hasDisposition(NO_ROUTE)));
    assertThat(computeTraces(batfish, snapshot, flow2), contains(hasDisposition(ACCEPTED)));
  }

  @Test
  public void testNullRouted() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Prefix prefix = Prefix.parse("1.0.0.0/8");
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(prefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
                .build()));

    // Compute data plane
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(config.getHostname(), config);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    Flow flow =
        builder()
            .setDstIp(prefix.getFirstHostIp())
            .setIngressNode(config.getHostname())
            .setIngressVrf(vrf.getName())
            .build();

    // Compute flow traces
    List<Trace> traces = computeTraces(batfish, snapshot, flow);

    assertThat(traces, hasSize(1));

    List<Hop> hops = traces.get(0).getHops();
    assertThat(hops, hasSize(1));

    List<Step<?>> steps = hops.get(0).getSteps();
    assertThat(steps, hasSize(2));

    assertTrue(OriginateStep.class.isInstance(steps.get(0)));
    assertTrue(RoutingStep.class.isInstance(steps.get(1)));
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
    ib.setOwner(source)
        .setVrf(vSource)
        .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
        .build();

    Configuration dst = cb.build();
    Vrf vDst = vb.setOwner(dst).build();
    ib.setOwner(dst).setVrf(vDst).setAddress(ConcreteInterfaceAddress.parse("10.0.0.2/24")).build();

    Configuration other = cb.build();
    Vrf vOther = vb.setOwner(other).build();
    ib.setOwner(other)
        .setVrf(vOther)
        .setAddress(ConcreteInterfaceAddress.parse("10.0.0.3/24"))
        .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.<String, Configuration>naturalOrder()
            .put(source.getHostname(), source)
            .put(dst.getHostname(), dst)
            .put(other.getHostname(), other)
            .build();
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    Flow flow =
        builder()
            .setIngressNode(source.getHostname())
            .setSrcIp(parse("10.0.0.1"))
            .setDstIp(parse("10.0.0.2"))
            .build();
    List<Trace> traces = computeTraces(batfish, snapshot, flow);

    /*
     *  Since the 'other' neighbor should not respond to ARP:
     *  - There should only be one trace, ending at 'dst'.
     *  - It should be accepting.
     */
    assertThat(traces, Matchers.contains(hasDisposition(ACCEPTED)));
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
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/24"))
        .setOutgoingFilter(outgoingFilter)
        .build();
    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(c.getHostname(), c);
    Batfish b = BatfishTestUtils.getBatfish(configurations, _tempFolder);
    b.computeDataPlane(b.getSnapshot());
    Flow flow = builder().setIngressNode(c.getHostname()).setDstIp(parse("1.0.0.1")).build();
    SortedMap<Flow, List<Trace>> flowTraces =
        b.buildFlows(b.getSnapshot(), ImmutableSet.of(flow), false);
    Trace trace = flowTraces.get(flow).iterator().next();

    /* Flow should be blocked by ACL before ARP, which would otherwise result in unreachable neighbor */
    assertThat(trace.getDisposition(), equalTo(FlowDisposition.DENIED_OUT));
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
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/24"))
        .build();

    // c2
    Configuration c2 = cb.build();
    Vrf v2 = vb.setOwner(c2).build();
    ib.setOwner(c2)
        .setVrf(v2)
        .setOutgoingFilter(null)
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.3/24"))
        .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    Batfish b = BatfishTestUtils.getBatfish(configurations, _tempFolder);
    b.computeDataPlane(b.getSnapshot());
    Flow flow = builder().setIngressNode(c1.getHostname()).setDstIp(Ip.parse("1.0.0.1")).build();
    SortedMap<Flow, List<Trace>> flowTraces =
        b.buildFlows(b.getSnapshot(), ImmutableSet.of(flow), false);
    Trace trace = flowTraces.get(flow).iterator().next();

    /* Flow should be blocked by ACL before ARP, which would otherwise result in unreachable neighbor */
    assertThat(trace.getDisposition(), equalTo(FlowDisposition.DENIED_OUT));
  }

  /** Tests ingressInterface with an incoming ACL. */
  @Test
  public void testDeniedInVsAccept() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Interface.Builder ib =
        nf.interfaceBuilder()
            .setOwner(c)
            .setVrf(nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build());

    // This interface has no incoming filter.
    Interface ifaceAllowIn = ib.setAddress(ConcreteInterfaceAddress.parse("2.0.0.2/24")).build();

    // This interface has an incoming filter that denies everything.
    Interface ifaceDenyIn =
        ib.setIncomingFilter(
                nf.aclBuilder().setOwner(c).setName("in").setLines(ImmutableList.of()).build())
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/24"))
            .build();

    Batfish b = BatfishTestUtils.getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _tempFolder);
    b.computeDataPlane(b.getSnapshot());
    Flow.Builder fb =
        builder()
            .setIngressNode(c.getHostname())
            .setDstIp(ifaceDenyIn.getConcreteAddress().getIp());

    Flow flowDenied = fb.setIngressInterface(ifaceDenyIn.getName()).build();
    Flow flowAllowed = fb.setIngressInterface(ifaceAllowIn.getName()).build();

    SortedMap<Flow, List<Trace>> flowTraces =
        b.buildFlows(b.getSnapshot(), ImmutableSet.of(flowDenied, flowAllowed), false);

    /* Flow coming in through ifaceDenyIn should be blocked by ACL. */
    Trace trace = Iterables.getOnlyElement(flowTraces.get(flowDenied));
    assertThat(trace.getDisposition(), equalTo(FlowDisposition.DENIED_IN));

    /* Flow coming in through ifaceAllowIn should be allowed in and then accepted. */
    trace = Iterables.getOnlyElement(flowTraces.get(flowAllowed));
    assertThat(trace.getDisposition(), equalTo(FlowDisposition.ACCEPTED));
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
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    Set<Flow> flows = ImmutableSet.of(builder().setIngressNode("missingNode").build());

    _thrown.expect(IllegalArgumentException.class);
    batfish.getTracerouteEngine(snapshot).computeTraces(flows, false);
  }

  /*
   * Create a network with a forwarding loop. When we run with ACLs enabled, the loop is detected.
   * When we run with ACLs enabled, it's not an infinite loop: we apply source NAT in the first
   * iteration, and drop with an ingress ACL in the second iteration.
   */
  @Test
  public void testLoop() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    ConcreteInterfaceAddress c1Addr = ConcreteInterfaceAddress.parse("1.0.0.0/31");
    ConcreteInterfaceAddress c2Addr = ConcreteInterfaceAddress.parse("1.0.0.1/31");
    Interface i1 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(c1Addr).build();
    Prefix loopPrefix = Prefix.parse("2.0.0.0/32");
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(loopPrefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(i1.getName())
                .setNextHopIp(c2Addr.getIp())
                .build()));
    Configuration c2 = cb.build();
    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    Interface i2 =
        nf.interfaceBuilder().setActive(true).setOwner(c2).setVrf(v2).setAddress(c2Addr).build();
    Prefix natPoolIp = Prefix.parse("5.5.5.5/32");
    i2.setIncomingFilter(
        nf.aclBuilder()
            .setOwner(c2)
            .setLines(ImmutableList.of(rejecting(matchSrc(natPoolIp))))
            .build());
    i2.setOutgoingTransformation(
        always().apply(assignSourceIp(natPoolIp.getStartIp(), natPoolIp.getStartIp())).build());
    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(loopPrefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(i2.getName())
                .setNextHopIp(c1Addr.getIp())
                .build()));
    Batfish batfish =
        BatfishTestUtils.getBatfish(
            ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2), _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    Flow flow =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(v1.getName())
            .setDstIp(loopPrefix.getStartIp())
            // any src Ip other than the NAT pool IP will do
            .setSrcIp(Ip.parse("6.6.6.6"))
            .build();
    List<Trace> traces = computeTraces(batfish, snapshot, flow);
    assertThat(traces, contains(hasDisposition(DENIED_IN)));
    traces = computeTraces(batfish, snapshot, flow, true);
    assertThat(traces, contains(hasDisposition(LOOP)));
  }

  @Test
  public void testGetFinalActionForDisposition() {
    assertThat(
        getFinalActionForDisposition(DELIVERED_TO_SUBNET), equalTo(StepAction.DELIVERED_TO_SUBNET));
    assertThat(getFinalActionForDisposition(EXITS_NETWORK), equalTo(StepAction.EXITS_NETWORK));
    assertThat(
        getFinalActionForDisposition(INSUFFICIENT_INFO), equalTo(StepAction.INSUFFICIENT_INFO));
    assertThat(
        getFinalActionForDisposition(NEIGHBOR_UNREACHABLE),
        equalTo(StepAction.NEIGHBOR_UNREACHABLE));
  }

  @Test
  public void testApplyPreSourceNatFilter() {
    String iface1 = "iface1";
    String iface2 = "iface2";
    String prefix = "1.2.3.4/24";
    String filterName = "preSourceFilter";

    IpAccessList filter =
        IpAccessList.builder()
            .setName(filterName)
            .setLines(
                ImmutableList.of(
                    accepting(
                        AclLineMatchExprs.and(
                            new MatchSrcInterface(ImmutableList.of(iface1)),
                            matchSrc(Prefix.parse(prefix)))),
                    rejecting(
                        AclLineMatchExprs.and(
                            new MatchSrcInterface(ImmutableList.of(iface2)),
                            matchSrc(Prefix.parse(prefix))))))
            .build();

    Flow flow = makeFlow();

    FilterStep step =
        TracerouteUtils.createFilterStep(
            flow,
            iface1,
            filter,
            FilterType.INGRESS_FILTER,
            ImmutableMap.of(filterName, filter),
            ImmutableMap.of(),
            false);

    assertThat(step.getAction(), equalTo(StepAction.PERMITTED));

    FilterStepDetail detail = step.getDetail();
    assertThat(detail.getFilter(), equalTo(filterName));

    step =
        TracerouteUtils.createFilterStep(
            flow,
            iface2,
            filter,
            FilterType.INGRESS_FILTER,
            ImmutableMap.of(filterName, filter),
            ImmutableMap.of(),
            false);

    assertThat(step.getAction(), equalTo(StepAction.DENIED));

    detail = step.getDetail();
    assertThat(detail.getFilter(), equalTo(filterName));
  }

  @Test
  public void testPreSourceNatFilter() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    ConcreteInterfaceAddress i1Addr = ConcreteInterfaceAddress.parse("1.0.0.1/31");
    ConcreteInterfaceAddress i2Addr = ConcreteInterfaceAddress.parse("2.0.0.1/31");
    Interface i1 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(i1Addr).build();
    Interface i2 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(i2Addr).build();
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("0.0.0.0/0"))
                .setAdministrativeCost(1)
                .setNextHopInterface(i2.getName())
                .build()));

    IpAccessList filter =
        IpAccessList.builder()
            .setName("preSourceFilter")
            .setOwner(c1)
            .setLines(
                ImmutableList.of(
                    accepting(
                        AclLineMatchExprs.and(
                            new MatchSrcInterface(ImmutableList.of(i1.getName())),
                            matchSrc(Prefix.parse("10.0.0.1/32"))))))
            .build();

    i2.setPreTransformationOutgoingFilter(filter);

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(c1.getHostname(), c1), _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    Flow flow =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(v1.getName())
            .setIngressInterface(i1.getName())
            .setSrcIp(Ip.parse("10.0.0.1"))
            .setDstIp(Ip.parse("20.6.6.6"))
            .build();
    List<Trace> traceList = computeTraces(batfish, snapshot, flow);
    assertThat(traceList, contains(hasDisposition(EXITS_NETWORK)));
    assertThat(traceList, hasSize(1));
    List<Hop> hops = traceList.get(0).getHops();
    assertThat(hops, hasSize(1));
    List<Step<?>> steps = hops.get(0).getSteps();
    // should have ingress acl -> routing -> presourcenat acl -> egress acl
    assertThat(steps, hasSize(5));

    assertThat(steps.get(0), instanceOf(EnterInputIfaceStep.class));
    assertThat(steps.get(1), instanceOf(RoutingStep.class));
    assertThat(steps.get(2), instanceOf(FilterStep.class));
    assertThat(steps.get(3), instanceOf(ExitOutputIfaceStep.class));
    assertThat(steps.get(4), instanceOf(DeliveredStep.class));

    EnterInputIfaceStep step0 = (EnterInputIfaceStep) steps.get(0);
    assertThat(step0.getAction(), equalTo(StepAction.RECEIVED));
    assertThat(step0.getDetail().getInputVrf(), equalTo(v1.getName()));
    assertThat(
        step0.getDetail().getInputInterface(),
        equalTo(NodeInterfacePair.of(c1.getHostname(), i1.getName())));

    RoutingStep step1 = (RoutingStep) steps.get(1);
    assertThat(step1.getAction(), equalTo(StepAction.FORWARDED));
    assertThat(step1.getDetail().getRoutes(), hasSize(1));
    assertThat(
        step1.getDetail().getRoutes(),
        contains(new RouteInfo(RoutingProtocol.STATIC, Prefix.parse("0.0.0.0/0"), Ip.AUTO, null)));

    FilterStep step2 = (FilterStep) steps.get(2);
    assertThat(step2.getAction(), equalTo(StepAction.PERMITTED));
    assertThat(step2.getDetail().getFilter(), equalTo(filter.getName()));

    ExitOutputIfaceStep step3 = (ExitOutputIfaceStep) steps.get(3);
    assertThat(step3.getAction(), equalTo(StepAction.TRANSMITTED));
    assertThat(
        step3.getDetail().getOutputInterface(),
        equalTo(NodeInterfacePair.of(c1.getHostname(), i2.getName())));
    assertThat(step3.getDetail().getTransformedFlow(), nullValue());

    DeliveredStep step4 = (DeliveredStep) steps.get(4);
    assertThat(step4.getAction(), equalTo(StepAction.EXITS_NETWORK));
    assertThat(
        step4.getDetail().getOutputInterface(),
        equalTo(NodeInterfacePair.of(c1.getHostname(), i2.getName())));
    assertThat(step4.getDetail().getResolvedNexthopIp(), equalTo(Ip.parse("20.6.6.6")));

    Flow flow2 =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(v1.getName())
            .setIngressInterface(i1.getName())
            .setSrcIp(Ip.parse("10.1.1.1"))
            .setDstIp(Ip.parse("20.6.6.6"))
            .build();
    traceList = computeTraces(batfish, snapshot, flow2);
    assertThat(traceList, contains(hasDisposition(DENIED_OUT)));
    hops = traceList.get(0).getHops();
    assertThat(hops, hasSize(1));

    steps = hops.get(0).getSteps();
    assertThat(steps, hasSize(3));

    assertThat(steps.get(0), instanceOf(EnterInputIfaceStep.class));
    assertThat(steps.get(1), instanceOf(RoutingStep.class));
    assertThat(steps.get(2), instanceOf(FilterStep.class));

    step2 = (FilterStep) steps.get(2);
    assertThat(step2.getAction(), equalTo(StepAction.DENIED));

    traceList = computeTraces(batfish, snapshot, flow2, true);
    assertThat(traceList, contains(hasDisposition(EXITS_NETWORK)));
  }

  @Test
  public void testPreSourceNatFilterOriginatingPackets() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    ConcreteInterfaceAddress i1Addr = ConcreteInterfaceAddress.parse("1.0.0.1/31");
    ConcreteInterfaceAddress i2Addr = ConcreteInterfaceAddress.parse("2.0.0.1/31");
    Interface i1 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(i1Addr).build();
    Interface i2 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(i2Addr).build();
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("0.0.0.0/0"))
                .setAdministrativeCost(1)
                .setNextHopInterface(i2.getName())
                .build()));

    String filterName = "preSourceFilter";

    IpAccessList filter =
        IpAccessList.builder()
            .setName(filterName)
            .setOwner(c1)
            .setLines(
                ImmutableList.of(
                    accepting(
                        AclLineMatchExprs.and(
                            new MatchSrcInterface(ImmutableList.of(i1.getName())),
                            matchSrc(Prefix.parse("10.0.0.1/32")))),
                    new ExprAclLine(
                        LineAction.PERMIT, OriginatingFromDevice.INSTANCE, "HOST_OUTBOUND")))
            .build();

    i2.setPreTransformationOutgoingFilter(filter);

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(c1.getHostname(), c1), _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    Flow flow =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(v1.getName())
            .setSrcIp(Ip.parse("1.0.0.1"))
            .setDstIp(Ip.parse("20.6.6.6"))
            .build();
    List<Trace> traceList = computeTraces(batfish, snapshot, flow, false);
    assertThat(traceList, contains(hasDisposition(EXITS_NETWORK)));
    assertThat(traceList, hasSize(1));
    List<Hop> hops = traceList.get(0).getHops();
    assertThat(hops, hasSize(1));
    List<Step<?>> steps = hops.get(0).getSteps();
    // should have originated -> routing -> presourcenat acl -> egress acl
    assertThat(steps, hasSize(5));

    assertThat(steps.get(0), instanceOf(OriginateStep.class));
    assertThat(steps.get(1), instanceOf(RoutingStep.class));
    assertThat(steps.get(2), instanceOf(FilterStep.class));
    assertThat(steps.get(3), instanceOf(ExitOutputIfaceStep.class));
    assertThat(steps.get(4), instanceOf(DeliveredStep.class));

    assertThat(steps.get(0).getAction(), equalTo(StepAction.ORIGINATED));
    assertThat(steps.get(1).getAction(), equalTo(StepAction.FORWARDED));
    assertThat(steps.get(2).getAction(), equalTo(StepAction.PERMITTED));
    assertThat(steps.get(3).getAction(), equalTo(StepAction.TRANSMITTED));
    assertThat(steps.get(4).getAction(), equalTo(StepAction.EXITS_NETWORK));
  }

  @Test
  public void testTransformationSteps() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();

    Ip ip21 = Ip.parse("2.0.0.1");
    Ip ip22 = Ip.parse("2.0.0.2");
    Ip ip33 = Ip.parse("3.0.0.3");
    Ip ip41 = Ip.parse("4.0.0.1");
    Prefix prefix2 = Prefix.parse("2.0.0.0/24");
    Interface.Builder ib = nf.interfaceBuilder().setOwner(c).setVrf(vrf).setActive(true);
    Interface inInterface =
        ib.setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/24"))
            .setIncomingTransformation(
                when(matchDst(ip21))
                    .apply(NOOP_DEST_NAT)
                    .setOrElse(
                        when(matchDst(prefix2)).apply(assignDestinationIp(ip33, ip33)).build())
                    .build())
            .build();
    ib.setAddress(ConcreteInterfaceAddress.parse("4.0.0.0/24"))
        .setOutgoingTransformation(
            when(matchSrc(ip21))
                .apply(NOOP_SOURCE_NAT)
                .setOrElse(when(matchSrc(prefix2)).apply(assignSourceIp(ip33, ip33)).build())
                .build())
        .build();

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    // Test flows matched by dest nat rules that permit but don't transform
    Flow flow =
        builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(inInterface.getName())
            .setSrcIp(ip22)
            .setDstIp(ip21)
            .build();
    List<Trace> traces = computeTraces(batfish, snapshot, flow, false);
    assertThat(traces, hasSize(1));

    Trace trace = traces.get(0);
    assertThat(trace.getDisposition(), equalTo(NO_ROUTE));

    assertThat(trace.getHops(), hasSize(1));
    Hop hop = trace.getHops().get(0);

    assertThat(hop.getSteps(), hasSize(3));
    List<Step<?>> steps = hop.getSteps();

    assertThat(
        steps.get(1),
        equalTo(
            new TransformationStep(
                new TransformationStepDetail(DEST_NAT, ImmutableSortedSet.of()),
                StepAction.PERMITTED)));

    // Test flows matched and transformed by dest nat rules
    flow =
        builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(inInterface.getName())
            .setSrcIp(ip21)
            .setDstIp(ip22)
            .build();
    traces = computeTraces(batfish, snapshot, flow, false);
    assertThat(traces, hasSize(1));

    trace = traces.get(0);
    assertThat(trace.getDisposition(), equalTo(NO_ROUTE));

    assertThat(trace.getHops(), hasSize(1));
    hop = trace.getHops().get(0);

    assertThat(hop.getSteps(), hasSize(3));
    steps = hop.getSteps();

    assertThat(
        steps.get(1),
        equalTo(
            new TransformationStep(
                new TransformationStepDetail(
                    DEST_NAT, flowDiffs(flow, flow.toBuilder().setDstIp(ip33).build())),
                StepAction.TRANSFORMED)));

    // Test flows not matched by dest nat rules
    flow =
        builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(inInterface.getName())
            .setSrcIp(ip21)
            .setDstIp(ip33)
            .build();
    traces = computeTraces(batfish, snapshot, flow, false);
    assertThat(traces, hasSize(1));

    trace = traces.get(0);
    assertThat(trace.getDisposition(), equalTo(NO_ROUTE));

    assertThat(trace.getHops(), hasSize(1));
    hop = trace.getHops().get(0);

    assertThat(hop.getSteps(), hasSize(2));

    // Test flows matched by source nat rules that permit but don't transform
    flow =
        builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(inInterface.getName())
            .setSrcIp(ip21)
            .setDstIp(ip41)
            .build();
    traces = computeTraces(batfish, snapshot, flow, false);
    assertThat(traces, hasSize(1));

    trace = traces.get(0);
    assertThat(trace.getDisposition(), equalTo(DELIVERED_TO_SUBNET));

    assertThat(trace.getHops(), hasSize(1));
    hop = trace.getHops().get(0);

    assertThat(hop.getSteps(), hasSize(5));
    steps = hop.getSteps();

    // source nat step
    assertThat(
        steps.get(2),
        equalTo(
            new TransformationStep(
                new TransformationStepDetail(SOURCE_NAT, ImmutableSortedSet.of()),
                StepAction.PERMITTED)));

    // Test flows matched and transformed by source nat rules
    flow =
        builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(inInterface.getName())
            .setSrcIp(ip22)
            .setDstIp(ip41)
            .build();
    traces = computeTraces(batfish, snapshot, flow, false);
    assertThat(traces, hasSize(1));

    trace = traces.get(0);
    assertThat(trace.getDisposition(), equalTo(DELIVERED_TO_SUBNET));

    assertThat(trace.getHops(), hasSize(1));
    hop = trace.getHops().get(0);

    assertThat(hop.getSteps(), hasSize(5));
    steps = hop.getSteps();

    // source nat step
    assertThat(
        steps.get(2),
        equalTo(
            new TransformationStep(
                new TransformationStepDetail(
                    SOURCE_NAT, flowDiffs(flow, flow.toBuilder().setSrcIp(ip33).build())),
                StepAction.TRANSFORMED)));

    // Test flows that match no source nat rule
    flow =
        builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(inInterface.getName())
            .setSrcIp(ip33)
            .setDstIp(ip41)
            .build();
    traces = computeTraces(batfish, snapshot, flow, false);
    assertThat(traces, hasSize(1));

    trace = traces.get(0);
    assertThat(trace.getDisposition(), equalTo(DELIVERED_TO_SUBNET));

    assertThat(trace.getHops(), hasSize(1));
    hop = trace.getHops().get(0);

    assertThat(hop.getSteps(), hasSize(4));
  }

  @Test
  public void testIngressSteps() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    ConcreteInterfaceAddress i1Addr = ConcreteInterfaceAddress.parse("1.0.0.1/31");
    ConcreteInterfaceAddress i2Addr = ConcreteInterfaceAddress.parse("2.0.0.1/31");
    Interface i1 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(i1Addr).build();
    Interface i2 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(i2Addr).build();
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("0.0.0.0/0"))
                .setAdministrativeCost(1)
                .setNextHopInterface(i2.getName())
                .build()));

    String filter = "ingressFilter";

    IpAccessList ingressFilter =
        IpAccessList.builder()
            .setName(filter)
            .setLines(ImmutableList.of(accepting(matchSrc(Prefix.parse("10.0.0.1/32")))))
            .build();

    c1.getIpAccessLists().put(filter, ingressFilter);
    i1.setIncomingFilter(ingressFilter);

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(c1.getHostname(), c1), _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    Flow flow =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(v1.getName())
            .setIngressInterface(i1.getName())
            .setSrcIp(Ip.parse("10.0.0.1"))
            .setDstIp(Ip.parse("20.6.6.6"))
            .build();
    SortedMap<Flow, List<Trace>> flowTraces =
        batfish.getTracerouteEngine(snapshot).computeTraces(ImmutableSet.of(flow), false);
    List<Trace> traceList = flowTraces.get(flow);
    assertThat(traceList, contains(hasDisposition(EXITS_NETWORK)));
    assertThat(traceList, hasSize(1));
    List<Hop> hops = traceList.get(0).getHops();
    assertThat(hops, hasSize(1));
    List<Step<?>> steps = hops.get(0).getSteps();
    // should have enter interface -> filter -> routing -> exit interface -> exit network
    assertThat(steps, hasSize(5));

    assertThat(steps.get(0), instanceOf(EnterInputIfaceStep.class));
    assertThat(steps.get(1), instanceOf(FilterStep.class));
    assertThat(steps.get(2), instanceOf(RoutingStep.class));
    assertThat(steps.get(3), instanceOf(ExitOutputIfaceStep.class));
    assertThat(steps.get(4), instanceOf(DeliveredStep.class));

    assertThat(steps.get(0).getAction(), equalTo(StepAction.RECEIVED));
    assertThat(steps.get(1).getAction(), equalTo(StepAction.PERMITTED));
    assertThat(steps.get(2).getAction(), equalTo(StepAction.FORWARDED));
    assertThat(steps.get(3).getAction(), equalTo(StepAction.TRANSMITTED));
    assertThat(steps.get(4).getAction(), equalTo(StepAction.EXITS_NETWORK));

    flow =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(v1.getName())
            .setIngressInterface(i1.getName())
            .setSrcIp(Ip.parse("20.0.0.1"))
            .setDstIp(Ip.parse("20.6.6.6"))
            .build();
    flowTraces = batfish.getTracerouteEngine(snapshot).computeTraces(ImmutableSet.of(flow), false);
    traceList = flowTraces.get(flow);
    assertThat(traceList, contains(hasDisposition(DENIED_IN)));
    assertThat(traceList, hasSize(1));
    hops = traceList.get(0).getHops();
    assertThat(hops, hasSize(1));
    steps = hops.get(0).getSteps();
    // should have enter interface -> filter
    assertThat(steps, hasSize(2));

    assertThat(steps.get(0), instanceOf(EnterInputIfaceStep.class));
    assertThat(steps.get(1), instanceOf(FilterStep.class));

    assertThat(steps.get(0).getAction(), equalTo(StepAction.RECEIVED));
    assertThat(steps.get(1).getAction(), equalTo(StepAction.DENIED));
  }

  @Test
  public void testOutgoingSteps() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    ConcreteInterfaceAddress i1Addr = ConcreteInterfaceAddress.parse("1.0.0.1/31");
    ConcreteInterfaceAddress i2Addr = ConcreteInterfaceAddress.parse("2.0.0.1/31");
    Interface i1 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(i1Addr).build();
    Interface i2 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(i2Addr).build();
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("0.0.0.0/0"))
                .setAdministrativeCost(1)
                .setNextHopInterface(i2.getName())
                .build()));

    String filter = "outgoingFilter";

    IpAccessList outgoingFilter =
        IpAccessList.builder()
            .setName(filter)
            .setLines(ImmutableList.of(accepting(matchSrc(Prefix.parse("10.0.0.1/32")))))
            .build();

    c1.getIpAccessLists().put(filter, outgoingFilter);
    i2.setOutgoingFilter(outgoingFilter);

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(c1.getHostname(), c1), _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    Flow flow =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(v1.getName())
            .setIngressInterface(i1.getName())
            .setSrcIp(Ip.parse("10.0.0.1"))
            .setDstIp(Ip.parse("20.6.6.6"))
            .build();
    SortedMap<Flow, List<Trace>> flowTraces =
        batfish.getTracerouteEngine(snapshot).computeTraces(ImmutableSet.of(flow), false);
    List<Trace> traceList = flowTraces.get(flow);
    assertThat(traceList, contains(hasDisposition(EXITS_NETWORK)));
    assertThat(traceList, hasSize(1));
    List<Hop> hops = traceList.get(0).getHops();
    assertThat(hops, hasSize(1));
    List<Step<?>> steps = hops.get(0).getSteps();
    // should have enter interface -> routing -> filter -> exit interface -> exit network
    assertThat(steps, hasSize(5));

    assertThat(steps.get(0), instanceOf(EnterInputIfaceStep.class));
    assertThat(steps.get(1), instanceOf(RoutingStep.class));
    assertThat(steps.get(2), instanceOf(FilterStep.class));
    assertThat(steps.get(3), instanceOf(ExitOutputIfaceStep.class));
    assertThat(steps.get(4), instanceOf(DeliveredStep.class));

    assertThat(steps.get(0).getAction(), equalTo(StepAction.RECEIVED));
    assertThat(steps.get(1).getAction(), equalTo(StepAction.FORWARDED));
    assertThat(steps.get(2).getAction(), equalTo(StepAction.PERMITTED));
    assertThat(steps.get(3).getAction(), equalTo(StepAction.TRANSMITTED));
    assertThat(steps.get(4).getAction(), equalTo(StepAction.EXITS_NETWORK));

    flow =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(v1.getName())
            .setIngressInterface(i1.getName())
            .setSrcIp(Ip.parse("20.0.0.1"))
            .setDstIp(Ip.parse("20.6.6.6"))
            .build();
    flowTraces = batfish.getTracerouteEngine(snapshot).computeTraces(ImmutableSet.of(flow), false);
    traceList = flowTraces.get(flow);
    assertThat(traceList, contains(hasDisposition(DENIED_OUT)));
    assertThat(traceList, hasSize(1));
    hops = traceList.get(0).getHops();
    assertThat(hops, hasSize(1));
    steps = hops.get(0).getSteps();
    // should have enter interface -> routing -> filter
    assertThat(steps, hasSize(3));

    assertThat(steps.get(0), instanceOf(EnterInputIfaceStep.class));
    assertThat(steps.get(1), instanceOf(RoutingStep.class));
    assertThat(steps.get(2), instanceOf(FilterStep.class));

    assertThat(steps.get(0).getAction(), equalTo(StepAction.RECEIVED));
    assertThat(steps.get(1).getAction(), equalTo(StepAction.FORWARDED));
    assertThat(steps.get(2).getAction(), equalTo(StepAction.DENIED));
  }

  /**
   * Tests that {@link TracerouteEngineImpl#computeTracesAndReverseFlows} returns the expected
   * return flow for different dispositions.
   */
  @Test
  public void testReturnFlow() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Vrf.Builder vb = nf.vrfBuilder().setOwner(c1);
    Interface.Builder ib = nf.interfaceBuilder().setOwner(c1);

    Vrf vrf1 = vb.build();
    Vrf vrf2 = vb.build();

    IpAccessList denySrcPort1234 =
        nf.aclBuilder()
            .setOwner(c1)
            .setLines(ImmutableList.of(rejecting(matchSrcPort(1234)), ACCEPT_ALL))
            .build();
    IpAccessList denySrcPort1235 =
        nf.aclBuilder()
            .setOwner(c1)
            .setLines(ImmutableList.of(rejecting(matchSrcPort(1235)), ACCEPT_ALL))
            .build();
    Interface i1 = ib.setVrf(vrf1).setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24")).build();
    Interface i2 =
        ib.setVrf(vrf2)
            .setAddress(ConcreteInterfaceAddress.parse("2.2.2.2/24"))
            .setIncomingFilter(denySrcPort1234)
            .build();
    Ip poolIp = Ip.parse("9.9.9.9");
    Interface i3 =
        ib.setVrf(vrf2)
            .setAddress(ConcreteInterfaceAddress.parse("3.3.3.3/24"))
            .setOutgoingFilter(denySrcPort1235)
            .setOutgoingTransformation(always().apply(assignSourceIp(poolIp, poolIp)).build())
            .build();
    ib.setOutgoingFilter(null);

    // for neighbor unreachable: unreachable loopback IP is in a connected subnet
    Ip unreachableLoopbackIp = Ip.parse("100.0.0.0");
    ib.setVrf(vrf2).setAddress(ConcreteInterfaceAddress.parse("100.0.0.1/31")).build();

    // static routes for EXITS_NETWORK and INSUFFICIENT_INFO
    StaticRoute.Builder srb =
        StaticRoute.testBuilder().setAdministrativeCost(1).setNextHopInterface(i3.getName());
    StaticRoute exitRoute = srb.setNetwork(Prefix.parse("10.0.0.2/32")).build();
    StaticRoute insufficientInfoRoute =
        srb.setNetwork(Prefix.parse("10.0.0.1/32")).setNextHopIp(unreachableLoopbackIp).build();
    vrf2.setStaticRoutes(ImmutableSortedSet.of(insufficientInfoRoute, exitRoute));

    // create a config that owns the unreachableLoopbackIp on a loopback interface
    Configuration c2 = cb.build();
    Vrf vrf3 = vb.setOwner(c2).build();
    ib.setOwner(c2)
        .setVrf(vrf3)
        .setAddress(ConcreteInterfaceAddress.create(unreachableLoopbackIp, 32))
        .build();

    // Compute data plane
    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    // Construct flows
    Builder fb =
        builder()
            .setDstIp(parse("3.3.3.3"))
            .setSrcIp(parse("5.5.5.5"))
            .setIpProtocol(TCP)
            .setSrcPort(0)
            .setDstPort(0)
            .setIngressNode(c1.getHostname());

    Flow acceptFlow = fb.setIngressInterface(i2.getName()).build();
    Flow noRouteFlow = fb.setIngressInterface(i1.getName()).build();

    Flow deliveredFlow = fb.setIngressInterface(i2.getName()).setDstIp(Ip.parse("3.3.3.2")).build();
    Flow deniedInFlow = fb.setSrcPort(1234).build();
    Flow deniedOutFlow = fb.setSrcPort(1235).build();
    fb.setSrcPort(0);
    Flow exitFlow = fb.setDstIp(exitRoute.getNetwork().getStartIp()).build();
    Flow insufficientInfoFlow =
        fb.setDstIp(insufficientInfoRoute.getNetwork().getStartIp()).build();
    Flow neighborUnreachableFlow = fb.setDstIp(unreachableLoopbackIp).build();

    // Compute flow traces
    SortedMap<Flow, List<TraceAndReverseFlow>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTracesAndReverseFlows(
                ImmutableSet.of(
                    acceptFlow,
                    deliveredFlow,
                    deniedInFlow,
                    deniedOutFlow,
                    exitFlow,
                    insufficientInfoFlow,
                    neighborUnreachableFlow,
                    noRouteFlow),
                false);

    assertThat(
        traces,
        hasEntry(
            equalTo(acceptFlow),
            contains(
                allOf(
                    hasTrace(hasDisposition(ACCEPTED)),
                    hasReverseFlow(
                        allOf(
                            hasDstIp(acceptFlow.getSrcIp()),
                            hasSrcIp(acceptFlow.getDstIp()),
                            hasIngressNode(c1.getHostname()),
                            hasIngressVrf(vrf2.getName()),
                            hasIngressInterface(nullValue())))))));
    assertThat(
        traces,
        hasEntry(
            equalTo(deniedInFlow),
            contains(allOf(hasTrace(hasDisposition(DENIED_IN)), hasReverseFlow(nullValue())))));
    assertThat(
        traces,
        hasEntry(
            equalTo(deniedOutFlow),
            contains(allOf(hasTrace(hasDisposition(DENIED_OUT)), hasReverseFlow(nullValue())))));
    assertThat(
        traces,
        hasEntry(
            equalTo(deliveredFlow),
            contains(
                allOf(
                    hasTrace(hasDisposition(DELIVERED_TO_SUBNET)),
                    hasReverseFlow(
                        allOf(
                            // forward flow went through the source NAT
                            hasDstIp(poolIp),
                            hasSrcIp(deliveredFlow.getDstIp()),
                            hasIngressNode(c1.getHostname()),
                            hasIngressVrf(nullValue()),
                            hasIngressInterface(i3.getName())))))));
    assertThat(
        traces,
        hasEntry(
            equalTo(exitFlow),
            contains(
                allOf(
                    hasTrace(hasDisposition(EXITS_NETWORK)),
                    hasReverseFlow(
                        allOf(
                            hasDstIp(poolIp),
                            hasSrcIp(exitFlow.getDstIp()),
                            hasIngressNode(c1.getHostname()),
                            hasIngressVrf(nullValue()),
                            hasIngressInterface(i3.getName())))))));
    assertThat(
        traces,
        hasEntry(
            equalTo(insufficientInfoFlow),
            contains(
                allOf(hasTrace(hasDisposition(INSUFFICIENT_INFO)), hasReverseFlow(nullValue())))));
    assertThat(
        traces,
        hasEntry(
            equalTo(neighborUnreachableFlow),
            contains(
                allOf(
                    hasTrace(hasDisposition(NEIGHBOR_UNREACHABLE)), hasReverseFlow(nullValue())))));
    assertThat(
        traces,
        hasEntry(
            equalTo(noRouteFlow),
            contains(allOf(hasTrace(hasDisposition(NO_ROUTE)), hasReverseFlow(nullValue())))));
  }

  @Test
  public void testSessionReturnFibLookupSameVrf() throws IOException {
    String i1Name = "i1";
    String i2Name = "i2";
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    String hostname = c.getHostname();
    Vrf.Builder vb = nf.vrfBuilder().setOwner(c);
    Vrf vrf = vb.build();
    Flow flow =
        builder()
            .setDstIp(parse("10.0.2.2"))
            .setDstPort(NamedPort.SSH.number()) // arbitrary
            .setIngressNode(c.getHostname())
            .setIngressInterface(i1Name)
            .setIpProtocol(TCP)
            .setSrcIp(parse("10.0.1.2"))
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .build();
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setOwner(c).setVrf(vrf);
    ib.setName(i1Name).setAddresses(ConcreteInterfaceAddress.parse("10.0.1.1/24")).build();
    ib.setName("i2")
        .setAddresses(ConcreteInterfaceAddress.parse("10.0.2.1/24"))
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.POST_NAT_FIB_LOOKUP, ImmutableSet.of(i2Name), null, null))
        .build();
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(hostname, c);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());

    TracerouteEngineImpl te = new TracerouteEngineImpl(dp, Topology.EMPTY, configs);
    List<TraceAndReverseFlow> forwardTraces =
        te.computeTracesAndReverseFlows(ImmutableSet.of(flow), false).get(flow);

    assertThat(forwardTraces, hasSize(1));

    TraceAndReverseFlow forwardTrace = forwardTraces.iterator().next();

    // forward flow should be delivered out i2
    assertThat(forwardTraces, contains(hasTrace(hasDisposition(DELIVERED_TO_SUBNET))));

    Flow reverseFlow = forwardTrace.getReverseFlow();

    List<TraceAndReverseFlow> reverseTraces =
        te.computeTracesAndReverseFlows(
                ImmutableSet.of(reverseFlow), forwardTrace.getNewFirewallSessions(), false)
            .get(reverseFlow);

    assertThat(reverseTraces, hasSize(1));
    // return flow should be delivered out i1
    assertThat(
        reverseTraces.iterator().next().getTrace().getDisposition(),
        equalTo(FlowDisposition.DELIVERED_TO_SUBNET));
  }

  @Test
  public void testSessionReturnFibLookupDifferentVrf() throws IOException {
    String i1Name = "i1";
    String i2Name = "i2";
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    String hostname = c.getHostname();
    Vrf.Builder vb = nf.vrfBuilder().setOwner(c);
    Vrf vrf1 = vb.build();
    Vrf vrf2 = vb.build();
    Flow flow =
        builder()
            .setDstIp(parse("10.0.2.2"))
            .setDstPort(NamedPort.SSH.number()) // arbitrary
            .setIngressNode(c.getHostname())
            .setIngressInterface(i1Name)
            .setIpProtocol(TCP)
            .setSrcIp(parse("10.0.1.2"))
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .build();
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setOwner(c);
    ib.setName(i1Name)
        .setVrf(vrf1)
        .setAddresses(ConcreteInterfaceAddress.parse("10.0.1.1/24"))
        .build();
    ib.setName("i2")
        .setVrf(vrf2)
        .setAddresses(ConcreteInterfaceAddress.parse("10.0.2.1/24"))
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.POST_NAT_FIB_LOOKUP, ImmutableSet.of(i2Name), null, null))
        .build();
    vrf1.getStaticRoutes()
        .add(
            StaticRoute.testBuilder()
                .setAdmin(1)
                .setNetwork(Prefix.ZERO)
                .setNextHop(NextHopDiscard.instance())
                .setNextHop(org.batfish.datamodel.route.nh.NextHopVrf.of(vrf2.getName()))
                .build());
    vrf2.getStaticRoutes()
        .add(
            StaticRoute.testBuilder()
                .setAdmin(1)
                .setNetwork(Prefix.ZERO)
                .setNextHop(NextHopDiscard.instance())
                .setNextHop(org.batfish.datamodel.route.nh.NextHopVrf.of(vrf1.getName()))
                .build());
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(hostname, c);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());

    TracerouteEngineImpl te = new TracerouteEngineImpl(dp, Topology.EMPTY, configs);
    List<TraceAndReverseFlow> forwardTraces =
        te.computeTracesAndReverseFlows(ImmutableSet.of(flow), false).get(flow);

    assertThat(forwardTraces, hasSize(1));

    TraceAndReverseFlow forwardTrace = forwardTraces.iterator().next();

    // forward flow should be delivered out i2
    assertThat(forwardTraces, contains(hasTrace(hasDisposition(DELIVERED_TO_SUBNET))));

    Flow reverseFlow = forwardTrace.getReverseFlow();

    List<TraceAndReverseFlow> reverseTraces =
        te.computeTracesAndReverseFlows(
                ImmutableSet.of(reverseFlow), forwardTrace.getNewFirewallSessions(), false)
            .get(reverseFlow);

    assertThat(reverseTraces, hasSize(1));
    // return flow should be delivered out i1
    assertThat(
        reverseTraces.iterator().next().getTrace().getDisposition(),
        equalTo(FlowDisposition.DELIVERED_TO_SUBNET));
  }

  @Test
  public void testEstablishedFlowDisposition() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration c1 = cb.build();
    Configuration c2 = cb.build();

    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).build();

    IpAccessList permitOutGoing =
        nf.aclBuilder().setOwner(c1).setLines(ImmutableList.of(ACCEPT_ALL)).build();

    IpAccessList onlyPermitEstablishedIncoming =
        nf.aclBuilder()
            .setOwner(c1)
            .setLines(
                ImmutableList.of(
                    ExprAclLine.acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(ImmutableList.of(IpProtocol.TCP))
                            .setDstIps(Ip.parse("1.0.1.2").toIpSpace())
                            .setTcpFlags(ImmutableSet.of(TcpFlagsMatchConditions.ACK_TCP_FLAG))
                            .build()),
                    REJECT_ALL))
            .build();

    nf.interfaceBuilder()
        .setOwner(c1)
        .setVrf(vrf1)
        .setAddress(ConcreteInterfaceAddress.create(Ip.parse("1.0.1.2"), 31))
        .setOutgoingFilter(permitOutGoing)
        .setIncomingFilter(onlyPermitEstablishedIncoming)
        .build();

    nf.interfaceBuilder()
        .setOwner(c2)
        .setVrf(vrf2)
        .setAddress(ConcreteInterfaceAddress.create(Ip.parse("1.0.1.3"), 31))
        .build();

    // Compute data plane
    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    // Flow going out
    Flow flowOut =
        builder()
            .setDstIp(parse("1.0.1.3"))
            .setSrcIp(parse("1.0.1.2"))
            .setIngressNode(c1.getHostname())
            .setIngressVrf(vrf1.getName())
            .setIpProtocol(TCP)
            .setSrcPort(80)
            .setDstPort(80)
            .setTcpFlagsSyn(1)
            .build();

    // Flow going in
    Flow flowIn =
        builder()
            .setDstIp(parse("1.0.1.2"))
            .setSrcIp(parse("1.0.1.3"))
            .setIngressNode(c2.getHostname())
            .setIngressVrf(vrf2.getName())
            .setIpProtocol(TCP)
            .setSrcPort(80)
            .setDstPort(80)
            .setTcpFlagsSyn(1)
            .build();

    TraceAndReverseFlow traceAndReverseFlowOut =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTracesAndReverseFlows(ImmutableSet.of(flowOut), false)
            .values()
            .iterator()
            .next()
            .iterator()
            .next();

    Trace traceToIn =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTracesAndReverseFlows(ImmutableSet.of(flowIn), false)
            .values()
            .iterator()
            .next()
            .iterator()
            .next()
            .getTrace();

    // uni directional traceroute should be permitted out and denied in
    assertThat(traceAndReverseFlowOut.getTrace().getDisposition(), equalTo(ACCEPTED));
    assertThat(traceToIn.getDisposition(), equalTo(DENIED_IN));

    assertThat(traceAndReverseFlowOut.getReverseFlow(), notNullValue());

    // getting reverse trace
    Trace reverseTrace =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTracesAndReverseFlows(
                ImmutableSet.of(traceAndReverseFlowOut.getReverseFlow()),
                traceAndReverseFlowOut.getNewFirewallSessions(),
                false)
            .values()
            .iterator()
            .next()
            .iterator()
            .next()
            .getTrace();

    // reverse trace should be permitted back in as reverse flow has ACK set
    assertThat(reverseTrace.getDisposition(), equalTo(ACCEPTED));
  }

  @Test
  public void testNewSessionsSingleHop() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(c1).setVrf(vrf1);

    String i1Name = "iface1";
    ib.setName(i1Name).setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24")).build();

    String i2Name = "iface2";
    String incomingAclName = "incomingAclName";
    String outgoingAclName = "outgoingAclName";
    ib.setName(i2Name)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.2.1/24"))
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE,
                ImmutableSet.of(i2Name),
                incomingAclName,
                outgoingAclName))
        .build();

    String i3Name = "iface3";
    ib.setName(i3Name)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.3.1/24"))
        .setFirewallSessionInterfaceInfo(null)
        .build();

    String i4Name = "iface4";
    Ip poolIp = Ip.parse("4.4.4.4");
    ib.setName(i4Name)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.4.1/24"))
        .setOutgoingTransformation(
            always().apply(assignSourceIp(poolIp, poolIp), assignSourcePort(2000, 2000)).build())
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE,
                ImmutableSet.of(i4Name),
                incomingAclName,
                outgoingAclName))
        .build();

    // Compute data plane
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(c1.getHostname(), c1);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(snapshot);

    // When exiting i2, we make a new session
    {
      Ip srcIp = Ip.parse("1.1.1.2");
      Ip dstIp = Ip.parse("1.1.2.2");
      int dstPort = 100;
      int srcPort = 200;
      IpProtocol ipProtocol = IpProtocol.TCP;
      Flow flow =
          builder()
              .setIngressNode(c1.getHostname())
              .setIngressInterface(i1Name)
              .setIpProtocol(ipProtocol)
              .setSrcIp(srcIp)
              .setSrcPort(srcPort)
              .setDstIp(dstIp)
              .setDstPort(dstPort)
              .build();
      List<TraceAndReverseFlow> traces =
          tracerouteEngine.computeTracesAndReverseFlows(ImmutableSet.of(flow), false).get(flow);
      SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of(i2Name));
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c1.getHostname(),
              new ForwardOutInterface(i1Name, null),
              sessionScope,
              matchSessionReturnFlow(flow),
              null);
      assertThat(
          traces,
          contains(
              allOf(
                  hasTrace(hasDisposition(DELIVERED_TO_SUBNET)),
                  hasReverseFlow(
                      builder()
                          .setIngressNode(c1.getHostname())
                          .setIngressInterface(i2Name)
                          .setIpProtocol(ipProtocol)
                          .setSrcIp(dstIp)
                          .setSrcPort(dstPort)
                          .setDstIp(srcIp)
                          .setDstPort(srcPort)
                          .build()),
                  hasNewFirewallSessions(contains(session)))));
      // SetupSessionStep should be captured
      assertThat(
          traces.get(0).getTrace().getHops().get(0).getSteps(),
          hasItem(
              allOf(
                  instanceOf(SetupSessionStep.class),
                  hasProperty(
                      "detail",
                      allOf(
                          hasProperty("sessionScope", equalTo(sessionScope)),
                          hasProperty("sessionAction", equalTo(session.getAction())),
                          hasProperty("matchCriteria", equalTo(session.getMatchCriteria())),
                          hasProperty("transformation", hasSize(0)))))));
    }

    // When exiting i3, we don't make a new session
    {
      Ip srcIp = Ip.parse("1.1.1.2");
      Ip dstIp = Ip.parse("1.1.3.2");
      int dstPort = 100;
      int srcPort = 200;
      IpProtocol ipProtocol = IpProtocol.TCP;
      Flow flow =
          builder()
              .setIngressNode(c1.getHostname())
              .setIngressInterface(i1Name)
              .setIpProtocol(ipProtocol)
              .setSrcIp(srcIp)
              .setSrcPort(srcPort)
              .setDstIp(dstIp)
              .setDstPort(dstPort)
              .build();
      List<TraceAndReverseFlow> traces =
          tracerouteEngine.computeTracesAndReverseFlows(ImmutableSet.of(flow), false).get(flow);
      assertThat(
          traces,
          contains(
              allOf(
                  hasTrace(hasDisposition(DELIVERED_TO_SUBNET)),
                  hasReverseFlow(
                      builder()
                          .setIngressNode(c1.getHostname())
                          .setIngressInterface(i3Name)
                          .setIpProtocol(ipProtocol)
                          .setSrcIp(dstIp)
                          .setSrcPort(dstPort)
                          .setDstIp(srcIp)
                          .setDstPort(srcPort)
                          .build()),
                  hasNewFirewallSessions(empty()))));
      // SetupSessionStep should not be captured
      assertThat(
          traces.get(0).getTrace().getHops().get(0).getSteps(),
          not(hasItem(instanceOf(SetupSessionStep.class))));
    }

    // When exiting i4, make a new session with a transformation
    {
      Ip srcIp = Ip.parse("1.1.1.2");
      Ip dstIp = Ip.parse("1.1.4.2");
      int dstPort = 100;
      int srcPort = 200;
      int poolPort = 2000;
      IpProtocol ipProtocol = IpProtocol.TCP;
      Flow flow =
          builder()
              .setIngressNode(c1.getHostname())
              .setIngressInterface(i1Name)
              .setIpProtocol(ipProtocol)
              .setSrcIp(srcIp)
              .setSrcPort(srcPort)
              .setDstIp(dstIp)
              .setDstPort(dstPort)
              .build();
      Flow transformedFlow = flow.toBuilder().setSrcIp(poolIp).setSrcPort(poolPort).build();
      List<TraceAndReverseFlow> traces =
          tracerouteEngine.computeTracesAndReverseFlows(ImmutableSet.of(flow), false).get(flow);
      SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of(i4Name));
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c1.getHostname(),
              new ForwardOutInterface(i1Name, null),
              sessionScope,
              matchSessionReturnFlow(transformedFlow),
              always()
                  .apply(assignDestinationIp(srcIp, srcIp), assignDestinationPort(srcPort, srcPort))
                  .build());
      assertThat(
          traces,
          contains(
              allOf(
                  hasTrace(hasDisposition(DELIVERED_TO_SUBNET)),
                  hasReverseFlow(
                      builder()
                          .setIngressNode(c1.getHostname())
                          .setIngressInterface(i4Name)
                          .setIpProtocol(ipProtocol)
                          .setSrcIp(dstIp)
                          .setSrcPort(dstPort)
                          .setDstIp(poolIp)
                          .setDstPort(2000)
                          .build()),
                  hasNewFirewallSessions(contains(session)))));
      assertThat(
          traces.get(0).getTrace().getHops().get(0).getSteps(),
          hasItem(
              allOf(
                  instanceOf(SetupSessionStep.class),
                  hasProperty(
                      "detail",
                      allOf(
                          hasProperty("sessionScope", equalTo(sessionScope)),
                          hasProperty("sessionAction", equalTo(session.getAction())),
                          hasProperty("matchCriteria", equalTo(session.getMatchCriteria())),
                          hasProperty(
                              "transformation",
                              contains(
                                  flowDiff(IpField.DESTINATION, poolIp, srcIp),
                                  flowDiff(PortField.DESTINATION, poolPort, srcPort))))))));
    }
  }

  @Test
  public void testNewSessionsMultiHop() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).build();

    String c1i1Name = "c1i1";
    nf.interfaceBuilder()
        .setName(c1i1Name)
        .setOwner(c1)
        .setVrf(vrf1)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.2/24"))
        .build();
    vrf1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.1.2.0/24"))
                .setNextHopIp(Ip.parse("1.1.1.1"))
                .setNextHopInterface(c1i1Name)
                .setAdministrativeCost(1)
                .build()));

    Configuration c2 = cb.build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(c2).setVrf(vrf2);

    String c2i1Name = "c2i1";
    ib.setName(c2i1Name).setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24")).build();

    String c2i2Name = "c2i2";
    ib.setName(c2i2Name)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.2.1/24"))
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ImmutableSet.of(c2i2Name), null, null))
        .build();

    // Compute data plane
    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(snapshot);

    /* c1:i1 -> c2:i1 -> c2:i2. The session created for return traffic has outgoing interface=c2i1
     * and next hop node/interface c1:i1.
     */
    {
      Ip srcIp = Ip.parse("1.1.1.2");
      Ip dstIp = Ip.parse("1.1.2.2");
      int dstPort = 100;
      int srcPort = 200;
      IpProtocol ipProtocol = IpProtocol.TCP;
      Flow flow =
          builder()
              .setIngressNode(c1.getHostname())
              .setIngressVrf(vrf1.getName())
              .setIpProtocol(ipProtocol)
              .setSrcIp(srcIp)
              .setSrcPort(srcPort)
              .setDstIp(dstIp)
              .setDstPort(dstPort)
              .build();
      List<TraceAndReverseFlow> traces =
          tracerouteEngine.computeTracesAndReverseFlows(ImmutableSet.of(flow), false).get(flow);
      SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of(c2i2Name));
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c2.getHostname(),
              new ForwardOutInterface(c2i1Name, NodeInterfacePair.of(c1.getHostname(), c1i1Name)),
              sessionScope,
              matchSessionReturnFlow(flow),
              null);
      assertThat(
          traces,
          contains(
              allOf(
                  hasTrace(hasDisposition(DELIVERED_TO_SUBNET)),
                  hasReverseFlow(
                      builder()
                          .setIngressNode(c2.getHostname())
                          .setIngressInterface(c2i2Name)
                          .setIpProtocol(ipProtocol)
                          .setSrcIp(dstIp)
                          .setSrcPort(dstPort)
                          .setDstIp(srcIp)
                          .setDstPort(srcPort)
                          .build()),
                  hasNewFirewallSessions(contains(session)))));
      assertThat(
          traces.get(0).getTrace().getHops().get(1).getSteps(),
          hasItem(
              allOf(
                  instanceOf(SetupSessionStep.class),
                  hasProperty(
                      "detail",
                      allOf(
                          hasProperty("sessionScope", equalTo(sessionScope)),
                          hasProperty("sessionAction", equalTo(session.getAction())),
                          hasProperty("matchCriteria", equalTo(session.getMatchCriteria())),
                          hasProperty("transformation", hasSize(0)))))));
    }
  }

  /** A test that uses sessions. */
  @Test
  public void testUseSession() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).build();

    Ip ingressDenySrcIp = Ip.parse("5.5.5.5");
    Ip egressDenySrcIp = Ip.parse("6.6.6.6");

    IpAccessList sessionIngressFilter =
        nf.aclBuilder()
            .setOwner(c1)
            .setLines(ImmutableList.of(rejecting(matchSrc(ingressDenySrcIp)), ACCEPT_ALL))
            .build();
    IpAccessList sessionEgressFilter =
        nf.aclBuilder()
            .setOwner(c1)
            .setLines(ImmutableList.of(rejecting(matchSrc(egressDenySrcIp)), ACCEPT_ALL))
            .build();

    String c1i1Name = "c1i1";
    nf.interfaceBuilder()
        .setName(c1i1Name)
        .setOwner(c1)
        .setVrf(vrf1)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24"))
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE,
                ImmutableSet.of(c1i1Name),
                sessionIngressFilter.getName(),
                null))
        .build();

    String c1i2Name = "c1i2";
    nf.interfaceBuilder()
        .setName(c1i2Name)
        .setOwner(c1)
        .setVrf(vrf1)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.2.1/24"))
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE,
                ImmutableSet.of(c1i2Name),
                null,
                sessionEgressFilter.getName()))
        .build();

    Configuration c2 = cb.build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(c2).setVrf(vrf2);

    String c2i1Name = "c2i1";
    ConcreteInterfaceAddress c2i1Addr = ConcreteInterfaceAddress.parse("1.1.2.2/24");
    ib.setName(c2i1Name).setAddress(c2i1Addr).build();

    // Compute data plane
    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(snapshot);

    Ip ip10 = Ip.parse("10.10.10.10");
    Ip ip11 = Ip.parse("11.11.11.11");
    Flow protoFlow =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressInterface(c1i1Name)
            .setDstIp(ip10)
            .setSrcIp(ip11)
            .build();

    // No session. No route
    {
      Flow flow = protoFlow;
      List<TraceAndReverseFlow> results =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flow), ImmutableSet.of(), false)
              .get(flow);
      // MatchSessionStep is not captured in trace
      assertThat(
          results.get(0).getTrace().getHops().get(0).getSteps(),
          not(hasItem(instanceOf(MatchSessionStep.class))));
      assertThat(results, contains(hasTrace(hasDisposition(NO_ROUTE))));
    }

    // Session exists with no outgoingInterface. Accepted at that node.
    {
      Flow flow = protoFlow;
      SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of(c1i1Name));
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c1.getHostname(), Accept.INSTANCE, sessionScope, makeMatchCriteria(flow), null);
      List<TraceAndReverseFlow> results =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flow), ImmutableSet.of(session), false)
              .get(flow);
      // MatchSessionStep is captured in trace
      assertThat(
          results.get(0).getTrace().getHops().get(0).getSteps(),
          hasItem(
              allOf(
                  instanceOf(MatchSessionStep.class),
                  hasProperty(
                      "detail",
                      allOf(
                          hasProperty("sessionScope", equalTo(sessionScope)),
                          hasProperty("sessionAction", equalTo(Accept.INSTANCE)),
                          hasProperty("matchCriteria", equalTo(session.getMatchCriteria())),
                          hasProperty("transformation", hasSize(0)))))));
      assertThat(
          results,
          contains(
              hasTrace(
                  allOf(
                      hasDisposition(ACCEPTED),
                      hasHops(contains(hasNodeName(c1.getHostname())))))));
    }

    // Session exists with outgoingInterface but no next-hop. Normal ARP-failure disposition
    {
      Flow flow = protoFlow;
      SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of(c1i1Name));
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c1.getHostname(),
              new ForwardOutInterface(c1i2Name, null),
              sessionScope,
              makeMatchCriteria(flow),
              null);
      List<TraceAndReverseFlow> results =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flow), ImmutableSet.of(session), false)
              .get(flow);
      assertThat(
          results.get(0).getTrace().getHops().get(0).getSteps(),
          hasItem(
              allOf(
                  instanceOf(MatchSessionStep.class),
                  hasProperty(
                      "detail",
                      allOf(
                          hasProperty("sessionScope", equalTo(sessionScope)),
                          hasProperty("sessionAction", equalTo(session.getAction())),
                          hasProperty("matchCriteria", equalTo(session.getMatchCriteria())),
                          hasProperty("transformation", hasSize(0)))))));
      /* Disposition is always exits network -- see:
       * TracerouteEngineImplContext#buildSessionArpFailureTrace(String, TransmissionContext, List).
       */
      assertThat(
          results,
          contains(
              hasTrace(
                  allOf(
                      hasDisposition(EXITS_NETWORK),
                      hasHops(contains(hasNodeName(c1.getHostname())))))));
    }

    // Session exists, no transformation, permitted by both filters.
    {
      Flow flow = protoFlow;
      SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of(c1i1Name));
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c1.getHostname(),
              new ForwardOutInterface(c1i2Name, NodeInterfacePair.of(c2.getHostname(), c2i1Name)),
              sessionScope,
              makeMatchCriteria(flow),
              null);
      List<TraceAndReverseFlow> results =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flow), ImmutableSet.of(session), false)
              .get(flow);
      assertThat(
          results.get(0).getTrace().getHops().get(0).getSteps(),
          hasItem(
              allOf(
                  instanceOf(MatchSessionStep.class),
                  hasProperty(
                      "detail",
                      allOf(
                          hasProperty("sessionScope", equalTo(sessionScope)),
                          hasProperty("sessionAction", equalTo(session.getAction())),
                          hasProperty("matchCriteria", equalTo(session.getMatchCriteria())),
                          hasProperty("transformation", hasSize(0)))))));
      // flow reaches c2.
      assertThat(
          results,
          contains(
              hasTrace(
                  hasHops(
                      contains(
                          ImmutableList.of(
                              hasNodeName(c1.getHostname()), hasNodeName(c2.getHostname())))))));
    }

    // Session exists, denied by ingress filter
    {
      Flow flow = protoFlow.toBuilder().setSrcIp(ingressDenySrcIp).build();
      SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of(c1i1Name));
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c1.getHostname(),
              new ForwardOutInterface(c1i2Name, NodeInterfacePair.of(c2.getHostname(), c2i1Name)),
              sessionScope,
              makeMatchCriteria(flow),
              null);
      List<TraceAndReverseFlow> results =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flow), ImmutableSet.of(session), false)
              .get(flow);
      assertThat(
          results.get(0).getTrace().getHops().get(0).getSteps(),
          hasItem(
              allOf(
                  instanceOf(MatchSessionStep.class),
                  hasProperty(
                      "detail",
                      allOf(
                          hasProperty("sessionScope", equalTo(sessionScope)),
                          hasProperty("sessionAction", equalTo(session.getAction())),
                          hasProperty("matchCriteria", equalTo(session.getMatchCriteria())),
                          hasProperty("transformation", hasSize(0)))))));
      assertThat(results, contains(hasTrace(hasDisposition(DENIED_IN))));
    }

    // Session exists, denied by egress filter
    {
      Flow flow = protoFlow.toBuilder().setSrcIp(egressDenySrcIp).build();
      SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of(c1i1Name));
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c1.getHostname(),
              new ForwardOutInterface(c1i2Name, NodeInterfacePair.of(c2.getHostname(), c2i1Name)),
              sessionScope,
              makeMatchCriteria(flow),
              null);
      List<TraceAndReverseFlow> results =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flow), ImmutableSet.of(session), false)
              .get(flow);
      assertThat(
          results.get(0).getTrace().getHops().get(0).getSteps(),
          hasItem(
              allOf(
                  instanceOf(MatchSessionStep.class),
                  hasProperty(
                      "detail",
                      allOf(
                          hasProperty("sessionScope", equalTo(sessionScope)),
                          hasProperty("sessionAction", equalTo(session.getAction())),
                          hasProperty("matchCriteria", equalTo(session.getMatchCriteria())),
                          hasProperty("transformation", hasSize(0)))))));
      assertThat(results, contains(hasTrace(hasDisposition(DENIED_OUT))));
    }

    /* Test that transformation is applied before egress filter. Original srcIp would be denied by
     * egress filter, butt transformation changes it.
     */
    {
      Flow flow = protoFlow.toBuilder().setSrcIp(egressDenySrcIp).build();
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c1.getHostname(),
              new ForwardOutInterface(c1i2Name, NodeInterfacePair.of(c2.getHostname(), c2i1Name)),
              ImmutableSet.of(c1i1Name),
              makeMatchCriteria(flow),
              always().apply(assignSourceIp(ip11, ip11)).build());
      List<TraceAndReverseFlow> results =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flow), ImmutableSet.of(session), false)
              .get(flow);
      assertThat(
          results.get(0).getTrace().getHops().get(0).getSteps(),
          hasItem(
              allOf(
                  instanceOf(MatchSessionStep.class),
                  hasProperty(
                      "detail",
                      hasProperty(
                          "transformation",
                          contains(flowDiff(IpField.SOURCE, egressDenySrcIp, ip11)))))));
      // flow reaches c2.
      assertThat(
          results,
          contains(
              hasTrace(
                  hasHops(
                      contains(
                          ImmutableList.of(
                              hasNodeName(c1.getHostname()), hasNodeName(c2.getHostname())))))));
    }

    /* Test that transformation is applied after session ingress filter. Transformed srcIp would be
     * permitted by ingress filter, but ingress filter is applied before the transformation.
     */
    {
      Flow flow = protoFlow.toBuilder().setSrcIp(ingressDenySrcIp).build();
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c1.getHostname(),
              new ForwardOutInterface(c1i2Name, NodeInterfacePair.of(c2.getHostname(), c2i1Name)),
              ImmutableSet.of(c1i1Name),
              makeMatchCriteria(flow),
              always().apply(assignSourceIp(ip11, ip11)).build());
      List<TraceAndReverseFlow> results =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flow), ImmutableSet.of(session), false)
              .get(flow);
      assertThat(
          results.get(0).getTrace().getHops().get(0).getSteps(),
          hasItem(
              allOf(
                  instanceOf(MatchSessionStep.class),
                  hasProperty(
                      "detail",
                      hasProperty(
                          "transformation",
                          contains(flowDiff(IpField.SOURCE, ingressDenySrcIp, ip11)))))));
      assertThat(results, contains(hasTrace(hasDisposition(DENIED_IN))));
    }

    // Session filters respect ignoreFilters.
    {
      Flow flow = protoFlow.toBuilder().setSrcIp(ingressDenySrcIp).build();
      FirewallSessionTraceInfo session =
          new FirewallSessionTraceInfo(
              c1.getHostname(),
              new ForwardOutInterface(c1i2Name, NodeInterfacePair.of(c2.getHostname(), c2i1Name)),
              ImmutableSet.of(c1i1Name),
              makeMatchCriteria(flow),
              always().apply(assignSourceIp(ip11, ip11)).build());
      List<TraceAndReverseFlow> results =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flow), ImmutableSet.of(session), true)
              .get(flow);
      // flow reaches c2.
      assertThat(
          results,
          contains(
              hasTrace(
                  hasHops(
                      contains(
                          ImmutableList.of(
                              hasNodeName(c1.getHostname()), hasNodeName(c2.getHostname())))))));

      flow = protoFlow.toBuilder().setSrcIp(egressDenySrcIp).build();
      session =
          new FirewallSessionTraceInfo(
              c1.getHostname(),
              new ForwardOutInterface(c1i2Name, NodeInterfacePair.of(c2.getHostname(), c2i1Name)),
              ImmutableSet.of(c1i1Name),
              makeMatchCriteria(flow),
              always().apply(assignSourceIp(ip11, ip11)).build());
      results =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flow), ImmutableSet.of(session), true)
              .get(flow);
      // flow reaches c2.
      assertThat(
          results,
          contains(
              hasTrace(
                  hasHops(
                      contains(
                          ImmutableList.of(
                              hasNodeName(c1.getHostname()), hasNodeName(c2.getHostname())))))));
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMultipleNextHopInterfaces() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Interface.Builder ib =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(ExprAclLine.REJECT_ALL))
                    .build());

    Interface i1 = ib.setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24")).build();

    StaticRoute.Builder sb =
        StaticRoute.testBuilder()
            .setAdministrativeCost(1)
            .setNetwork(Prefix.parse("5.0.0.0/32"))
            .setNextHopInterface(i1.getName());

    // two static routes with the same outgoing interface but two different next-hop IPs.
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            sb.setNextHopIp(Ip.parse("8.8.8.8")).build(),
            sb.setNextHopIp(Ip.parse("8.8.8.9")).build()));

    // Compute data plane
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(config.getHostname(), config);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(snapshot);

    Flow flow =
        builder()
            .setDstIp(parse("5.0.0.0"))
            .setIngressNode(config.getHostname())
            .setIngressVrf(vrf.getName())
            .build();
    List<Trace> traces = tracerouteEngine.computeTraces(ImmutableSet.of(flow), false).get(flow);

    /* The trace steps we create for each route should be not leak into the other trace. If they did
     * the number of steps would increase in the second trace.
     */
    assertThat(
        traces,
        contains(hasHops(contains(hasSteps(hasSize(3)))), hasHops(contains(hasSteps(hasSize(3))))));
  }

  @Test
  public void testPAT() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();

    Ip srcIp = Ip.parse("1.1.1.1");
    int srcPort = 3000;
    int poolPort = 2000;

    Transformation transformation =
        when(matchSrc(srcIp)).apply(assignSourcePort(poolPort, poolPort)).build();

    Interface.Builder ib =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setOutgoingTransformation(transformation);

    Interface i1 = ib.setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24")).build();

    StaticRoute.Builder sb =
        StaticRoute.testBuilder()
            .setAdministrativeCost(1)
            .setNetwork(Prefix.parse("5.0.0.0/32"))
            .setNextHopInterface(i1.getName());

    vrf.setStaticRoutes(ImmutableSortedSet.of(sb.build()));

    // Compute data plane
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(config.getHostname(), config);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(snapshot);

    Flow flow =
        builder()
            .setSrcIp(srcIp)
            .setDstIp(parse("5.0.0.0"))
            .setIpProtocol(TCP)
            .setSrcPort(srcPort)
            .setDstPort(0)
            .setIngressNode(config.getHostname())
            .setIngressVrf(vrf.getName())
            .build();
    List<Trace> traces = tracerouteEngine.computeTraces(ImmutableSet.of(flow), false).get(flow);

    assertThat(traces, hasSize(1));
    assertThat(traces.get(0).getHops(), hasSize(1));
    assertThat(traces.get(0).getHops().get(0).getSteps(), hasSize(5));
    assertThat(
        traces.get(0).getHops().get(0).getSteps().get(2),
        equalTo(
            new TransformationStep(
                new TransformationStepDetail(
                    SOURCE_NAT,
                    ImmutableSortedSet.of(flowDiff(PortField.SOURCE, srcPort, poolPort))),
                StepAction.TRANSFORMED)));
  }

  @Test
  public void testFilterStep() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Interface.Builder ib = nf.interfaceBuilder();

    // c1
    Configuration c1 = cb.build();
    Vrf v1 = vb.setOwner(c1).build();
    // permits everything
    IpAccessList outgoingFilter =
        nf.aclBuilder()
            .setOwner(c1)
            .setName("outgoingAcl")
            .setLines(ImmutableList.of(ExprAclLine.accepting(AclLineMatchExprs.TRUE)))
            .build();
    ib.setOwner(c1)
        .setVrf(v1)
        .setOutgoingFilter(outgoingFilter)
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/24"))
        .build();

    // c2
    Configuration c2 = cb.build();
    Vrf v2 = vb.setOwner(c2).build();
    // denies all
    IpAccessList postTransformationFilter =
        nf.aclBuilder()
            .setOwner(c1)
            .setName("postTransformationAcl")
            .setLines(ImmutableList.of())
            .build();
    Interface i2 =
        ib.setOwner(c2)
            .setVrf(v2)
            .setIncomingTransformation(
                always().apply(assignSourceIp(Ip.parse("10.0.0.1"), Ip.parse("10.0.0.2"))).build())
            .setPostTransformationIncomingFilter(postTransformationFilter)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.3/24"))
            .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    Batfish b = BatfishTestUtils.getBatfish(configurations, _tempFolder);
    b.computeDataPlane(b.getSnapshot());
    Flow flow = builder().setIngressNode(c1.getHostname()).setDstIp(Ip.parse("1.0.0.3")).build();
    SortedMap<Flow, List<Trace>> flowTraces =
        b.buildFlows(b.getSnapshot(), ImmutableSet.of(flow), false);
    Trace trace = flowTraces.get(flow).iterator().next();

    assertThat(trace.getHops(), hasSize(2));

    // hop 0
    {
      List<Step<?>> steps = trace.getHops().get(0).getSteps();
      assertThat(steps, hasSize(4));

      assertTrue(OriginateStep.class.isInstance(steps.get(0)));
      assertTrue(RoutingStep.class.isInstance(steps.get(1)));
      assertTrue(FilterStep.class.isInstance(steps.get(2)));
      assertTrue(ExitOutputIfaceStep.class.isInstance(steps.get(3)));

      FilterStep filterStep = (FilterStep) steps.get(2);
      assertThat(filterStep.getAction(), equalTo(StepAction.PERMITTED));
      assertThat(filterStep.getDetail().getFilter(), equalTo("outgoingAcl"));
      assertThat(filterStep.getDetail().getType(), equalTo(FilterType.EGRESS_FILTER));
      assertNull(filterStep.getDetail().getInputInterface());
      assertThat(filterStep.getDetail().getFlow(), equalTo(flow));
    }

    // hop 1
    {
      List<Step<?>> steps = trace.getHops().get(1).getSteps();
      assertThat(steps, hasSize(3));

      assertTrue(EnterInputIfaceStep.class.isInstance(steps.get(0)));
      assertTrue(TransformationStep.class.isInstance(steps.get(1)));
      assertTrue(FilterStep.class.isInstance(steps.get(2)));

      FilterStep filterStep = (FilterStep) steps.get(2);
      assertThat(filterStep.getAction(), equalTo(StepAction.DENIED));
      assertThat(filterStep.getDetail().getFilter(), equalTo("postTransformationAcl"));
      assertThat(
          filterStep.getDetail().getType(), equalTo(FilterType.POST_TRANSFORMATION_INGRESS_FILTER));
      assertThat(filterStep.getDetail().getInputInterface(), equalTo(i2.getName()));
      assertThat(
          filterStep.getDetail().getFlow(),
          equalTo(flow.toBuilder().setSrcIp(Ip.parse("10.0.0.1")).build()));
    }
  }

  @Test
  public void testBidirectionalTracerouteWithDeliveredStep() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration c1 = cb.setHostname("c1").build();
    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).build();

    nf.interfaceBuilder()
        .setName("c1_out")
        .setOwner(c1)
        .setVrf(vrf1)
        .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
        .build();

    String c1ToFw = "c1_to_fw";
    nf.interfaceBuilder()
        .setName(c1ToFw)
        .setOwner(c1)
        .setVrf(vrf1)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24"))
        .build();

    vrf1.getStaticRoutes()
        .add(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("20.0.0.2/32"))
                .setNextHopInterface(c1ToFw)
                .setNextHopIp(Ip.parse("1.1.1.2"))
                .setAdministrativeCost(1)
                .build());

    Configuration fw =
        cb.setConfigurationFormat(ConfigurationFormat.CISCO_ASA).setHostname("fw").build();
    Vrf fwVrf = nf.vrfBuilder().setOwner(fw).build();

    String fwToC1Name = "fw_to_c1";
    nf.interfaceBuilder()
        .setName(fwToC1Name)
        .setOwner(fw)
        .setVrf(fwVrf)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.2/24"))
        .build();

    // set up another interface with session
    String fwOutName = "fwOut";
    nf.interfaceBuilder()
        .setName(fwOutName)
        .setOwner(fw)
        .setVrf(fwVrf)
        .setAddress(ConcreteInterfaceAddress.parse("20.0.0.1/24"))
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.POST_NAT_FIB_LOOKUP, ImmutableSet.of(fwOutName), null, null))
        .build();

    // set up a static route for the reverse flow
    fwVrf
        .getStaticRoutes()
        .add(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("10.0.0.0/24"))
                .setAdministrativeCost(1)
                .setNextHopIp(Ip.parse("1.1.1.1"))
                .build());

    // Compute data plane
    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(c1.getHostname(), c1, fw.getHostname(), fw);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(snapshot);

    Flow flow =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(vrf1.getName())
            .setSrcIp(Ip.parse("10.0.0.2"))
            .setDstIp(Ip.parse("20.0.0.2"))
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(12345)
            .setDstPort(12346)
            .build();

    List<TraceAndReverseFlow> forwardTracerouteResult =
        tracerouteEngine.computeTracesAndReverseFlows(ImmutableSet.of(flow), false).get(flow);

    assertThat(forwardTracerouteResult, hasSize(1));

    Flow reverseFlow = forwardTracerouteResult.get(0).getReverseFlow();
    assertNotNull(reverseFlow);

    Set<FirewallSessionTraceInfo> newSessions =
        forwardTracerouteResult.get(0).getNewFirewallSessions();

    // reverse direction
    List<TraceAndReverseFlow> reverseResult =
        tracerouteEngine
            .computeTracesAndReverseFlows(ImmutableSet.of(reverseFlow), newSessions, false)
            .get(reverseFlow);

    assertThat(reverseResult, hasSize(1));

    List<Hop> reverseHops = reverseResult.get(0).getTrace().getHops();
    assertThat(reverseHops, hasSize(2));

    assertThat(reverseHops.get(0).getSteps(), hasSize(4));
    assertTrue(reverseHops.get(0).getSteps().get(0) instanceof EnterInputIfaceStep);
    assertTrue(reverseHops.get(0).getSteps().get(1) instanceof MatchSessionStep);
    assertTrue(reverseHops.get(0).getSteps().get(2) instanceof RoutingStep);
    assertTrue(reverseHops.get(0).getSteps().get(3) instanceof ExitOutputIfaceStep);

    assertThat(reverseHops.get(1).getSteps(), hasSize(4));
    assertTrue(reverseHops.get(1).getSteps().get(0) instanceof EnterInputIfaceStep);
    assertTrue(reverseHops.get(1).getSteps().get(1) instanceof RoutingStep);
    assertTrue(reverseHops.get(1).getSteps().get(2) instanceof ExitOutputIfaceStep);
    assertTrue(reverseHops.get(1).getSteps().get(3) instanceof DeliveredStep);
  }

  @Test
  public void testBidirectionalTracerouteWithArpErrorStep() throws IOException {
    // Construct network
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration c1 = cb.setHostname("c1").build();
    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).build();

    nf.interfaceBuilder()
        .setName("c1_out")
        .setOwner(c1)
        .setVrf(vrf1)
        .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
        .build();

    String c1ToFw = "c1_to_fw";
    nf.interfaceBuilder()
        .setName(c1ToFw)
        .setOwner(c1)
        .setVrf(vrf1)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24"))
        .build();

    vrf1.getStaticRoutes()
        .add(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("20.0.0.2/32"))
                .setNextHopInterface(c1ToFw)
                .setNextHopIp(Ip.parse("1.1.1.2"))
                .setAdministrativeCost(1)
                .build());

    Configuration fw =
        cb.setConfigurationFormat(ConfigurationFormat.CISCO_ASA).setHostname("fw").build();
    Vrf fwVrf = nf.vrfBuilder().setOwner(fw).build();

    String fwToC1Name = "fw_to_c1";
    nf.interfaceBuilder()
        .setName(fwToC1Name)
        .setOwner(fw)
        .setVrf(fwVrf)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.2/24"))
        .build();

    // set up another interface with session
    String fwOutName = "fwOut";
    nf.interfaceBuilder()
        .setName(fwOutName)
        .setOwner(fw)
        .setVrf(fwVrf)
        .setAddress(ConcreteInterfaceAddress.parse("20.0.0.1/24"))
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.POST_NAT_FIB_LOOKUP, ImmutableSet.of(fwOutName), null, null))
        .build();

    // set up a dummy interface to cause arp failure for return flow
    String fwDummyName = "fwDummy";
    nf.interfaceBuilder()
        .setName(fwDummyName)
        .setOwner(fw)
        .setVrf(fwVrf)
        .setAddress(ConcreteInterfaceAddress.parse("200.0.0.1/24"))
        .build();

    // set up a static route for the reverse flow
    fwVrf
        .getStaticRoutes()
        .add(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("30.0.0.0/24"))
                .setAdministrativeCost(1)
                .setNextHopIp(Ip.parse("100.1.1.1"))
                .setNextHopInterface(fwDummyName)
                .build());

    // set up a dummy node to cause Insufficient Info
    Configuration dummy =
        cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS).setHostname("dummy").build();
    Vrf dummyVrf = nf.vrfBuilder().setOwner(dummy).build();
    nf.interfaceBuilder()
        .setName("dummy_interface")
        .setOwner(dummy)
        .setVrf(dummyVrf)
        .setAddress(ConcreteInterfaceAddress.parse("100.1.1.1/24"))
        .build();

    // Compute data plane
    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(
            c1.getHostname(), c1, fw.getHostname(), fw, dummy.getHostname(), dummy);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    TracerouteEngine tracerouteEngine = batfish.getTracerouteEngine(snapshot);

    Flow flow =
        builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(vrf1.getName())
            .setSrcIp(Ip.parse("30.0.0.1"))
            .setDstIp(Ip.parse("20.0.0.2"))
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(12345)
            .setDstPort(12346)
            .build();

    List<TraceAndReverseFlow> forwardTracerouteResult =
        tracerouteEngine.computeTracesAndReverseFlows(ImmutableSet.of(flow), false).get(flow);

    assertThat(forwardTracerouteResult, hasSize(1));

    Flow reverseFlow = forwardTracerouteResult.get(0).getReverseFlow();
    assertNotNull(reverseFlow);

    Set<FirewallSessionTraceInfo> newSessions =
        forwardTracerouteResult.get(0).getNewFirewallSessions();

    // reverse direction
    List<TraceAndReverseFlow> reverseResult =
        tracerouteEngine
            .computeTracesAndReverseFlows(ImmutableSet.of(reverseFlow), newSessions, false)
            .get(reverseFlow);

    assertThat(reverseResult, hasSize(1));

    List<Hop> reverseHops = reverseResult.get(0).getTrace().getHops();
    assertThat(reverseHops, hasSize(1));

    assertThat(reverseHops.get(0).getSteps(), hasSize(5));
    assertTrue(reverseHops.get(0).getSteps().get(0) instanceof EnterInputIfaceStep);
    assertTrue(reverseHops.get(0).getSteps().get(1) instanceof MatchSessionStep);
    assertTrue(reverseHops.get(0).getSteps().get(2) instanceof RoutingStep);
    assertTrue(reverseHops.get(0).getSteps().get(3) instanceof ExitOutputIfaceStep);
    assertTrue(reverseHops.get(0).getSteps().get(4) instanceof ArpErrorStep);
  }
}
