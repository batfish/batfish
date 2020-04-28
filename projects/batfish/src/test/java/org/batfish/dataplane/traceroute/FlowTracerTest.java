package org.batfish.dataplane.traceroute;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.LOOP;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.FlowDisposition.NULL_ROUTED;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.matchers.TraceAndReverseFlowMatchers.hasNewFirewallSessions;
import static org.batfish.datamodel.matchers.TraceAndReverseFlowMatchers.hasTrace;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.dataplane.traceroute.FlowTracer.buildFirewallSessionTraceInfo;
import static org.batfish.dataplane.traceroute.FlowTracer.buildRoutingStep;
import static org.batfish.dataplane.traceroute.FlowTracer.getSessionAction;
import static org.batfish.dataplane.traceroute.FlowTracer.initialFlowTracer;
import static org.batfish.dataplane.traceroute.FlowTracer.matchSessionReturnFlow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDOps;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.MemoizedIpAccessListToBdd;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.MockDataPlane;
import org.batfish.datamodel.MockFib;
import org.batfish.datamodel.MockForwardingAnalysis;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Accept;
import org.batfish.datamodel.flow.ArpErrorStep;
import org.batfish.datamodel.flow.DeliveredStep;
import org.batfish.datamodel.flow.FibLookup;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.ForwardOutInterface;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.MatchSessionStep;
import org.batfish.datamodel.flow.OriginatingSessionScope;
import org.batfish.datamodel.flow.RouteInfo;
import org.batfish.datamodel.flow.RoutingStep;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.batfish.datamodel.flow.SessionMatchExpr;
import org.batfish.datamodel.flow.SetupSessionStep;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.transformation.Transformation;
import org.junit.Test;

/** Tests for {@link FlowTracer}. */
public final class FlowTracerTest {
  @Test
  public void testBuildDeniedTraceNoNewSessions() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();

    Flow flow =
        Flow.builder()
            .setDstIp(Ip.parse("1.1.1.1"))
            .setIngressNode(c.getHostname())
            .setIngressVrf(vrf.getName())
            .build();

    List<TraceAndReverseFlow> traces = new ArrayList<>();

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setConfigs(ImmutableMap.of(c.getHostname(), c)).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false);
    FlowTracer flowTracer = initialFlowTracer(ctxt, c.getHostname(), null, flow, traces::add);
    flowTracer.buildDeniedTrace(DENIED_IN);
    assertThat(
        traces,
        contains(allOf(hasTrace(hasDisposition(DENIED_IN)), hasNewFirewallSessions(empty()))));
  }

  @Test
  public void testBuildDeniedTraceNewSessions() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();

    Flow flow =
        Flow.builder()
            .setDstIp(Ip.parse("1.1.1.1"))
            .setIngressNode(c.getHostname())
            .setIngressVrf(vrf.getName())
            .build();
    List<TraceAndReverseFlow> traces = new ArrayList<>();

    SessionMatchExpr dummySessionFlow =
        new SessionMatchExpr(IpProtocol.TCP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    FirewallSessionTraceInfo sessionInfo =
        new FirewallSessionTraceInfo(
            "hostname", Accept.INSTANCE, ImmutableSet.of(), dummySessionFlow, null);
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setConfigs(ImmutableMap.of(c.getHostname(), c)).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false);
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            c,
            null,
            new Node(c.getHostname()),
            traces::add,
            NodeInterfacePair.of("node", "iface"),
            ImmutableSet.of(sessionInfo),
            flow,
            vrf.getName(),
            new ArrayList<>(),
            ImmutableList.of(),
            new Stack<>(),
            flow);

    flowTracer.buildDeniedTrace(DENIED_IN);
    assertThat(
        traces,
        contains(
            allOf(
                hasTrace(hasDisposition(DENIED_IN)),
                hasNewFirewallSessions(contains(sessionInfo)))));
  }

  @Test
  public void testBuildAcceptTraceNewSessions() {
    /* Simulates an incoming flow that is accepted and causes a session to be set up. */
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    vrf.setHasOriginatingSessions(true);
    Interface iface = nf.interfaceBuilder().setOwner(c).setVrf(vrf).build();

    Ip srcIp = Ip.parse("1.1.1.1");
    Ip dstIp = Ip.parse("2.2.2.2");
    int srcPort = 22;
    int dstPort = 40;
    Flow flow =
        Flow.builder()
            .setSrcIp(srcIp)
            .setDstIp(dstIp)
            .setIngressNode(c.getHostname())
            .setIngressInterface(iface.getName())
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(srcPort)
            .setDstPort(dstPort)
            .build();

    // Accepting interface should be the one that owns the dst IP, not necessarily ingress interface
    String acceptingIfaceName = "acceptingIface";
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setConfigs(ImmutableMap.of(c.getHostname(), c))
                .setForwardingAnalysis(
                    MockForwardingAnalysis.builder()
                        .setAcceptedIps(
                            ImmutableMap.of(
                                c.getHostname(),
                                ImmutableMap.of(
                                    vrf.getName(),
                                    ImmutableMap.of(acceptingIfaceName, dstIp.toIpSpace()))))
                        .build())
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false);
    NodeInterfacePair lastNodeIfacePair = NodeInterfacePair.of("node", "iface");
    List<TraceAndReverseFlow> traces = new ArrayList<>();
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            c,
            iface.getName(),
            new Node(c.getHostname()),
            traces::add,
            lastNodeIfacePair,
            new HashSet<>(),
            flow,
            vrf.getName(),
            new ArrayList<>(),
            new ArrayList<>(),
            new Stack<>(),
            flow);

    // To match session, return flow must have same protocol and port and swapped src/dst IPs
    SessionMatchExpr flowMatchingNewSession =
        new SessionMatchExpr(IpProtocol.TCP, dstIp, srcIp, dstPort, srcPort);
    FirewallSessionTraceInfo expectedNewSession =
        new FirewallSessionTraceInfo(
            c.getHostname(),
            // Matching flows should be forwarded out original ingress interface
            new ForwardOutInterface(iface.getName(), lastNodeIfacePair),
            new OriginatingSessionScope(vrf.getName()),
            flowMatchingNewSession,
            null);
    flowTracer.buildAcceptTrace();
    TraceAndReverseFlow traceAndReverseFlow = Iterables.getOnlyElement(traces);
    Trace trace = traceAndReverseFlow.getTrace();
    Set<FirewallSessionTraceInfo> newSessions = traceAndReverseFlow.getNewFirewallSessions();
    assertThat(trace, hasDisposition(ACCEPTED));
    assertThat(
        trace.getHops().get(0).getSteps(),
        contains(
            // Trace should include a SetupSessionStep
            instanceOf(SetupSessionStep.class),
            // Inbound step should show accepting interface, not ingress interface
            hasProperty("detail", hasProperty("interface", equalTo(acceptingIfaceName)))));
    assertThat(newSessions, contains(expectedNewSession));
  }

  @Test
  public void testOriginatingFlowMatchesInboundSession() {
    /* Simulates a reverse flow originating in a VRF where a session has been set up. */
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    vrf.setHasOriginatingSessions(true);
    String ifaceName = "ifaceName";
    nf.interfaceBuilder()
        .setOwner(c)
        .setVrf(vrf)
        .setName(ifaceName)
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(false, ImmutableSet.of(ifaceName), null, null))
        .build();

    // To match session, return flow must have same protocol and port and swapped src/dst IPs
    Ip srcIp = Ip.parse("1.1.1.1");
    Ip dstIp = Ip.parse("2.2.2.2");
    int srcPort = 22;
    int dstPort = 40;
    Flow returnFlow =
        Flow.builder()
            .setSrcIp(dstIp)
            .setDstIp(srcIp)
            .setIngressNode(c.getHostname())
            .setIngressVrf(vrf.getName())
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(dstPort)
            .setDstPort(srcPort)
            .build();
    SessionMatchExpr flowMatchingNewSession =
        new SessionMatchExpr(IpProtocol.TCP, dstIp, srcIp, dstPort, srcPort);
    FirewallSessionTraceInfo inboundSession =
        new FirewallSessionTraceInfo(
            c.getHostname(),
            // Matching flows should be forwarded out original ingress interface
            new ForwardOutInterface(ifaceName, null),
            new OriginatingSessionScope(vrf.getName()),
            flowMatchingNewSession,
            null);

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setConfigs(ImmutableMap.of(c.getHostname(), c))
                .setForwardingAnalysis(
                    MockForwardingAnalysis.builder()
                        .setAcceptedIps(
                            ImmutableMap.of(
                                c.getHostname(), ImmutableMap.of(vrf.getName(), ImmutableMap.of())))
                        .build())
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(inboundSession),
            ImmutableSet.of(),
            ImmutableMap.of(
                c.getHostname(), ImmutableMap.of(vrf.getName(), MockFib.builder().build())),
            false);

    {
      // Return flow matches session
      List<TraceAndReverseFlow> traces = new ArrayList<>();
      FlowTracer flowTracer =
          new FlowTracer(
              ctxt,
              c,
              null,
              new Node(c.getHostname()),
              traces::add,
              null,
              new HashSet<>(),
              returnFlow,
              vrf.getName(),
              new ArrayList<>(),
              new ArrayList<>(),
              new Stack<>(),
              returnFlow);
      flowTracer.processHop();

      // Reverse trace should match session and get forwarded out original ingress interface
      TraceAndReverseFlow traceAndReverseFlow = Iterables.getOnlyElement(traces);
      Trace trace = traceAndReverseFlow.getTrace();
      assertThat(trace, hasDisposition(EXITS_NETWORK));
      assertThat(trace.getHops().get(0).getSteps(), hasItem(instanceOf(MatchSessionStep.class)));
      assertThat(traceAndReverseFlow.getNewFirewallSessions(), empty());
    }

    {
      // Return flow does not match session
      Flow nonMatchingReturnFlow = returnFlow.toBuilder().setIpProtocol(IpProtocol.UDP).build();
      List<TraceAndReverseFlow> traces = new ArrayList<>();
      FlowTracer flowTracer =
          new FlowTracer(
              ctxt,
              c,
              null,
              new Node(c.getHostname()),
              traces::add,
              null,
              new HashSet<>(),
              nonMatchingReturnFlow,
              vrf.getName(),
              new ArrayList<>(),
              new ArrayList<>(),
              new Stack<>(),
              nonMatchingReturnFlow);
      flowTracer.processHop();

      // Reverse trace should not match session, so should be dropped (FIB has no routes)
      TraceAndReverseFlow traceAndReverseFlow = Iterables.getOnlyElement(traces);
      Trace trace = traceAndReverseFlow.getTrace();
      assertThat(trace, hasDisposition(NO_ROUTE));
      assertThat(
          trace.getHops().get(0).getSteps(), not(hasItem(instanceOf(MatchSessionStep.class))));
      assertThat(traceAndReverseFlow.getNewFirewallSessions(), empty());
    }
  }

  @Test
  public void testFibLookupNullRoute() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    String hostname = c.getHostname();
    Vrf.Builder vb = nf.vrfBuilder().setOwner(c);
    Vrf srcVrf = vb.build();
    String srcVrfName = srcVrf.getName();

    Flow flow =
        Flow.builder()
            .setDstIp(Ip.parse("1.1.1.1"))
            .setIngressNode(c.getHostname())
            .setIngressVrf(srcVrfName)
            .build();
    Ip dstIp = flow.getDstIp();
    ImmutableList.Builder<TraceAndReverseFlow> traces = ImmutableList.builder();

    Fib srcFib =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(
                            FibNullRoute.INSTANCE,
                            ImmutableList.of(
                                StaticRoute.builder()
                                    .setAdmin(1)
                                    .setNetwork(Prefix.ZERO)
                                    .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
                                    .build())))))
            .build();

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setConfigs(ImmutableMap.of(c.getHostname(), c)).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(hostname, ImmutableMap.of(srcVrfName, srcFib)),
            false);
    FlowTracer flowTracer = initialFlowTracer(ctxt, hostname, null, flow, traces::add);
    flowTracer.fibLookup(dstIp, hostname, srcFib);
    List<TraceAndReverseFlow> finalTraces = traces.build();
    assertThat(traces.build(), contains(hasTrace(hasDisposition(NULL_ROUTED))));
    assertThat(finalTraces.get(0).getTrace().getHops(), hasSize(1));

    Hop hop = finalTraces.get(0).getTrace().getHops().get(0);
    assertThat(hop.getSteps().get(0), instanceOf(RoutingStep.class));
    RoutingStep routingStep = (RoutingStep) hop.getSteps().get(0);
    assertThat(routingStep.getAction(), equalTo(StepAction.NULL_ROUTED));
  }

  @Test
  public void testFibLookupNextVrf() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    String hostname = c.getHostname();
    Vrf.Builder vb = nf.vrfBuilder().setOwner(c);
    Vrf srcVrf = vb.build();
    String srcVrfName = srcVrf.getName();
    Vrf nextVrf = vb.build();
    String nextVrfName = nextVrf.getName();
    Flow flow =
        Flow.builder()
            .setDstIp(Ip.parse("1.1.1.1"))
            .setIngressNode(c.getHostname())
            .setIngressVrf(srcVrfName)
            .build();
    Ip dstIp = flow.getDstIp();
    StaticRoute nextVrfRoute =
        StaticRoute.builder().setAdmin(1).setNetwork(Prefix.ZERO).setNextVrf(nextVrfName).build();
    StaticRoute nullRoute =
        StaticRoute.builder()
            .setAdmin(1)
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
            .build();
    Fib srcFib =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(new FibNextVrf(nextVrfName), ImmutableList.of(nextVrfRoute)))))
            .build();
    Fib nextFib =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(FibNullRoute.INSTANCE, ImmutableList.of(nullRoute)))))
            .build();
    List<TraceAndReverseFlow> traces = new ArrayList<>();
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setConfigs(ImmutableMap.of(c.getHostname(), c)).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(hostname, ImmutableMap.of(srcVrfName, srcFib, nextVrfName, nextFib)),
            false);
    FlowTracer flowTracer = initialFlowTracer(ctxt, hostname, null, flow, traces::add);
    flowTracer.fibLookup(dstIp, hostname, srcFib);

    // Should be delegated from srcFib to nextFib and eventually NULL_ROUTED
    assertThat(traces, contains(hasTrace(hasDisposition(NULL_ROUTED))));

    List<Hop> hops = traces.get(0).getTrace().getHops();

    // There should be a single hop
    assertThat(hops, hasSize(1));

    List<Step<?>> steps = hops.iterator().next().getSteps();

    // There should be 2 routing steps and an exit-output-iface step;
    // - next-vr should occur in the first step
    // - null-route should occur in the second step
    // - third step should have null-route action

    assertThat(steps, contains(instanceOf(RoutingStep.class), instanceOf(RoutingStep.class)));
    assertThat(
        ((RoutingStep) steps.get(0)).getDetail().getRoutes().get(0).getNextVrf(),
        equalTo(nextVrfName));
    assertThat((steps.get(0)).getAction(), equalTo(StepAction.FORWARDED_TO_NEXT_VRF));
    assertThat(
        ((RoutingStep) steps.get(1)).getDetail().getRoutes().get(0).getNextHopIp(),
        equalTo(Ip.AUTO));
    assertThat((steps.get(1)).getAction(), equalTo(StepAction.NULL_ROUTED));
  }

  @Test
  public void testFibLookupNextVrfLoop() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    String hostname = c.getHostname();
    Vrf.Builder vb = nf.vrfBuilder().setOwner(c);
    Vrf vrf1 = vb.build();
    String vrf1Name = vrf1.getName();
    Vrf vrf2 = vb.build();
    String vrf2Name = vrf2.getName();
    Flow flow =
        Flow.builder()
            .setDstIp(Ip.parse("1.1.1.1"))
            .setIngressNode(c.getHostname())
            .setIngressVrf(vrf1Name)
            .build();
    Ip dstIp = flow.getDstIp();
    StaticRoute vrf1NextVrfRoute =
        StaticRoute.builder().setAdmin(1).setNetwork(Prefix.ZERO).setNextVrf(vrf2Name).build();
    StaticRoute vrf2NextVrfRoute =
        StaticRoute.builder().setAdmin(1).setNetwork(Prefix.ZERO).setNextVrf(vrf1Name).build();
    Fib fib1 =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(
                            new FibNextVrf(vrf2Name), ImmutableList.of(vrf1NextVrfRoute)))))
            .build();
    Fib fib2 =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(
                            new FibNextVrf(vrf1Name), ImmutableList.of(vrf2NextVrfRoute)))))
            .build();
    List<TraceAndReverseFlow> traces = new ArrayList<>();
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setConfigs(ImmutableMap.of(c.getHostname(), c)).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(hostname, ImmutableMap.of(vrf1Name, fib1, vrf2Name, fib2)),
            false);
    FlowTracer flowTracer = initialFlowTracer(ctxt, hostname, null, flow, traces::add);
    flowTracer.fibLookup(dstIp, hostname, fib1);

    // Should be delegated from fib1 to fib2 and then looped back to fib1
    assertThat(traces, contains(hasTrace(hasDisposition(LOOP))));

    List<Hop> hops = traces.get(0).getTrace().getHops();

    // There should be a single hop
    assertThat(hops, hasSize(1));

    List<Step<?>> steps = hops.iterator().next().getSteps();

    // There should be 2 routing steps, with no action due to loop.
    // - next-vr should occur in the first step
    // - next-vr should occur in the second step

    assertThat(steps, contains(instanceOf(RoutingStep.class), instanceOf(RoutingStep.class)));
    assertThat(
        ((RoutingStep) steps.get(0)).getDetail().getRoutes().get(0).getNextVrf(),
        equalTo(vrf2Name));
    assertThat(
        ((RoutingStep) steps.get(1)).getDetail().getRoutes().get(0).getNextVrf(),
        equalTo(vrf1Name));
  }

  @Test
  public void testFibLookupForwarded() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    String hostname = c.getHostname();
    Vrf.Builder vb = nf.vrfBuilder().setOwner(c);
    Vrf srcVrf = vb.build();
    nf.interfaceBuilder()
        .setName("iface1")
        .setAddress(ConcreteInterfaceAddress.parse("123.12.1.12/24"))
        .setVrf(srcVrf)
        .setOwner(c)
        .build();
    String srcVrfName = srcVrf.getName();

    Flow flow =
        Flow.builder()
            .setDstIp(Ip.parse("1.1.1.1"))
            .setIngressNode(c.getHostname())
            .setIngressVrf(srcVrfName)
            .build();
    Ip dstIp = flow.getDstIp();
    ImmutableList.Builder<TraceAndReverseFlow> traces = ImmutableList.builder();
    Ip finalNhip = Ip.parse("12.12.12.12");
    String finalNhif = "iface1";

    Fib srcFib =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(
                            new FibForward(finalNhip, finalNhif),
                            ImmutableList.of(
                                StaticRoute.builder()
                                    .setAdmin(1)
                                    .setNetwork(Prefix.ZERO)
                                    .setNextHopIp(Ip.parse("1.2.3.4"))
                                    .build())),
                        new FibEntry(
                            new FibForward(finalNhip, finalNhif),
                            ImmutableList.of(
                                StaticRoute.builder()
                                    .setAdmin(1)
                                    .setNetwork(Prefix.ZERO)
                                    .setNextHopIp(Ip.parse("2.3.4.5"))
                                    .build())))))
            .build();

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setForwardingAnalysis(
                    MockForwardingAnalysis.builder()
                        .setDeliveredToSubnet(
                            ImmutableMap.of(
                                c.getHostname(),
                                ImmutableMap.of(
                                    srcVrf.getName(),
                                    ImmutableMap.of("iface1", dstIp.toIpSpace()))))
                        .build())
                .setConfigs(ImmutableMap.of(c.getHostname(), c))
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(hostname, ImmutableMap.of(srcVrfName, srcFib)),
            false);
    FlowTracer flowTracer = initialFlowTracer(ctxt, hostname, null, flow, traces::add);
    flowTracer.fibLookup(dstIp, hostname, srcFib);
    List<TraceAndReverseFlow> finalTraces = traces.build();
    assertThat(traces.build(), contains(hasTrace(hasDisposition(DELIVERED_TO_SUBNET))));
    assertThat(finalTraces.get(0).getTrace().getHops(), hasSize(1));

    Hop hop = finalTraces.get(0).getTrace().getHops().get(0);
    assertThat(hop.getSteps().get(0), instanceOf(RoutingStep.class));
    RoutingStep routingStep = (RoutingStep) hop.getSteps().get(0);
    assertThat(routingStep.getAction(), equalTo(StepAction.FORWARDED));

    assertThat(
        routingStep.getDetail(),
        equalTo(
            RoutingStepDetail.builder()
                .setOutputInterface(finalNhif)
                .setArpIp(finalNhip)
                .setRoutes(
                    ImmutableList.of(
                        new RouteInfo(
                            RoutingProtocol.STATIC, Prefix.ZERO, Ip.parse("1.2.3.4"), null),
                        new RouteInfo(
                            RoutingProtocol.STATIC, Prefix.ZERO, Ip.parse("2.3.4.5"), null)))
                .build()));
  }

  /**
   * Test that {@link FlowTracer#eval(Transformation transformation)} evaluates the transformation
   * using the {@link IpSpace IpSpaces} defined on the current node.
   */
  @Test
  public void testTransformationEvaluatorNode() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    Ip ip3 = Ip.parse("3.3.3.3");

    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration c1 = cb.build();
    c1.setIpSpaces(ImmutableSortedMap.of("ips", ip1.toIpSpace()));

    Configuration c2 = cb.build();
    c2.setIpSpaces(ImmutableSortedMap.of("ips", ip2.toIpSpace()));

    Transformation transformation =
        when(matchDst(new IpSpaceReference("ips"))).apply(assignDestinationIp(ip3, ip3)).build();

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setConfigs(ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2))
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false);
    Flow.Builder fb =
        Flow.builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(Configuration.DEFAULT_VRF_NAME);

    // 1. evaluate dstIp=ip1 on c1. Should be transformed to ip3
    {
      Flow flow = fb.setDstIp(ip1).build();
      FlowTracer flowTracer = initialFlowTracer(ctxt, c1.getHostname(), null, flow, tarf -> {});
      assertThat(flowTracer.eval(transformation).getOutputFlow().getDstIp(), equalTo(ip3));
    }

    // 2. evaluate dstIp=ip2 on c1. Should not be transformed.
    {
      Flow flow = fb.setDstIp(ip2).build();
      FlowTracer flowTracer = initialFlowTracer(ctxt, c1.getHostname(), null, flow, tarf -> {});
      assertThat(flowTracer.eval(transformation).getOutputFlow().getDstIp(), equalTo(ip2));
    }

    // 3. evaluate dstIp=ip1 after forking to c2. Should not be transformed
    {
      Flow flow = fb.setDstIp(ip1).build();
      FlowTracer flowTracer = initialFlowTracer(ctxt, c1.getHostname(), null, flow, tarf -> {});
      flowTracer =
          flowTracer.forkTracer(c2, null, new ArrayList<>(), null, Configuration.DEFAULT_VRF_NAME);
      assertThat(flowTracer.eval(transformation).getOutputFlow().getDstIp(), equalTo(ip1));
    }

    // 4. evaluate dstIp=ip2 after forking to c2. Should be transformed to ip3
    {
      Flow flow = fb.setDstIp(ip2).build();
      FlowTracer flowTracer = initialFlowTracer(ctxt, c1.getHostname(), null, flow, tarf -> {});
      flowTracer =
          flowTracer.forkTracer(c2, null, new ArrayList<>(), null, Configuration.DEFAULT_VRF_NAME);
      assertThat(flowTracer.eval(transformation).getOutputFlow().getDstIp(), equalTo(ip3));
    }
  }

  @Test
  public void testBuildFirewallSessionTraceInfo_protocolWithoutSessions() {
    Flow flow =
        Flow.builder()
            .setIngressNode("node")
            .setIngressVrf("vrf")
            .setIpProtocol(IpProtocol.HOPOPT)
            .build();
    assertNull(
        buildFirewallSessionTraceInfo(
            "", flow, flow, Accept.INSTANCE, new OriginatingSessionScope("vrf")));
  }

  @Test
  public void testBuildFirewallSessionTraceInfo_icmp() {
    Flow flow =
        Flow.builder()
            .setIngressNode("node")
            .setIngressVrf("vrf")
            .setIpProtocol(IpProtocol.ICMP)
            .setIcmpCode(0)
            .setIcmpType(0)
            .build();
    assertNotNull(
        buildFirewallSessionTraceInfo(
            "", flow, flow, Accept.INSTANCE, new OriginatingSessionScope("vrf")));
  }

  @Test
  public void testBuildFirewallSessionTraceInfo_protocolWithSessions() {
    Flow flow =
        Flow.builder()
            .setIngressNode("node")
            .setIngressVrf("vrf")
            .setIpProtocol(IpProtocol.TCP)
            .setDstPort(100)
            .setSrcPort(20)
            .build();
    assertNotNull(
        buildFirewallSessionTraceInfo(
            "", flow, flow, Accept.INSTANCE, new OriginatingSessionScope("vrf")));
  }

  @Test
  public void testGetSessionAction() {
    NodeInterfacePair lastHopNodeAndOutgoingInterface = NodeInterfacePair.of("node", "iface");
    String ingressIface = "ingressIface";

    // Fib lookup true: action should always be fib lookup
    assertThat(
        getSessionAction(true, null, lastHopNodeAndOutgoingInterface), equalTo(FibLookup.INSTANCE));
    assertThat(
        getSessionAction(true, ingressIface, lastHopNodeAndOutgoingInterface),
        equalTo(FibLookup.INSTANCE));

    // Ingress interface defined: action should be forward to last hop node and interface
    assertThat(
        getSessionAction(false, ingressIface, lastHopNodeAndOutgoingInterface),
        equalTo(new ForwardOutInterface(ingressIface, lastHopNodeAndOutgoingInterface)));

    // Ingress interface null: action should be accept (flow that set up session originated here)
    assertThat(
        getSessionAction(false, null, lastHopNodeAndOutgoingInterface), equalTo(Accept.INSTANCE));
  }

  @Test
  public void testMatchSessionReturnFlow() {
    BDDPacket pkt = new BDDPacket();
    MemoizedIpAccessListToBdd toBdd =
        new MemoizedIpAccessListToBdd(
            pkt, BDDSourceManager.empty(pkt), ImmutableMap.of(), ImmutableMap.of());

    Ip dstIp = Ip.parse("1.1.1.1");
    Ip srcIp = Ip.parse("2.2.2.2");

    Flow.Builder fb =
        Flow.builder().setIngressNode("node").setIngressVrf("vrf").setDstIp(dstIp).setSrcIp(srcIp);

    BDD returnFlowDstIpBdd = pkt.getDstIp().value(srcIp.asLong());
    BDD returnFlowSrcIpBdd = pkt.getSrcIp().value(dstIp.asLong());

    // TCP
    {
      Flow flow = fb.setIpProtocol(IpProtocol.TCP).setDstPort(100).setSrcPort(20).build();
      BDD returnFlowBdd = toBdd.toBdd(matchSessionReturnFlow(flow).toAclLineMatchExpr());
      assertEquals(
          returnFlowBdd,
          BDDOps.andNull(
              returnFlowDstIpBdd,
              returnFlowSrcIpBdd,
              pkt.getDstPort().value(flow.getSrcPort()),
              pkt.getSrcPort().value(flow.getDstPort()),
              pkt.getIpProtocol().value(flow.getIpProtocol())));
    }

    // UDP
    {
      Flow flow = fb.setIpProtocol(IpProtocol.UDP).setDstPort(100).setSrcPort(20).build();
      BDD returnFlowBdd = toBdd.toBdd(matchSessionReturnFlow(flow).toAclLineMatchExpr());
      assertEquals(
          returnFlowBdd,
          BDDOps.andNull(
              returnFlowDstIpBdd,
              returnFlowSrcIpBdd,
              pkt.getDstPort().value(flow.getSrcPort()),
              pkt.getSrcPort().value(flow.getDstPort()),
              pkt.getIpProtocol().value(flow.getIpProtocol())));
    }

    // ICMP
    {
      Flow flow = fb.setIpProtocol(IpProtocol.ICMP).setIcmpType(100).setIcmpCode(20).build();
      BDD returnFlowBdd = toBdd.toBdd(matchSessionReturnFlow(flow).toAclLineMatchExpr());
      assertEquals(
          returnFlowBdd,
          BDDOps.andNull(
              returnFlowDstIpBdd,
              returnFlowSrcIpBdd,
              pkt.getIpProtocol().value(flow.getIpProtocol())));
    }
  }

  @Test
  public void testBuildRoutingStepFibForward() {
    Prefix prefix = Prefix.parse("12.12.12.12/30");
    FibForward fibForward = new FibForward(Ip.parse("1.1.1.1"), "iface1");
    Set<FibEntry> fibEntries =
        ImmutableSet.of(
            new FibEntry(
                fibForward,
                ImmutableList.of(
                    StaticRoute.builder()
                        .setNextHopIp(Ip.parse("2.2.2.2"))
                        .setNetwork(prefix)
                        .setAdministrativeCost(1)
                        .build())));

    RoutingStep routingStep = buildRoutingStep(fibForward, fibEntries);

    assertThat(routingStep.getAction(), equalTo(StepAction.FORWARDED));
    assertThat(
        routingStep.getDetail().getRoutes(),
        equalTo(
            ImmutableList.of(
                new RouteInfo(RoutingProtocol.STATIC, prefix, Ip.parse("2.2.2.2"), null))));
    assertThat(routingStep.getDetail().getArpIp(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(routingStep.getDetail().getOutputInterface(), equalTo("iface1"));
  }

  @Test
  public void testBuildRoutingStepFibNextVrf() {
    Prefix prefix = Prefix.parse("12.12.12.12/30");
    FibNextVrf fibNextVrf = new FibNextVrf("iface1");
    Set<FibEntry> fibEntries =
        ImmutableSet.of(
            new FibEntry(
                fibNextVrf,
                ImmutableList.of(
                    StaticRoute.builder()
                        .setNextHopIp(Ip.parse("2.2.2.2"))
                        .setNetwork(prefix)
                        .setAdministrativeCost(1)
                        .build())));

    RoutingStep routingStep = buildRoutingStep(fibNextVrf, fibEntries);

    assertThat(routingStep.getAction(), equalTo(StepAction.FORWARDED_TO_NEXT_VRF));
    assertThat(
        routingStep.getDetail().getRoutes(),
        equalTo(
            ImmutableList.of(
                new RouteInfo(RoutingProtocol.STATIC, prefix, Ip.parse("2.2.2.2"), null))));
    assertThat(routingStep.getDetail().getArpIp(), nullValue());
    assertThat(routingStep.getDetail().getOutputInterface(), nullValue());
  }

  @Test
  public void testBuildRoutingStepFibNullRouted() {
    Prefix prefix = Prefix.parse("12.12.12.12/30");
    FibNullRoute fibNullRoute = FibNullRoute.INSTANCE;
    Set<FibEntry> fibEntries =
        ImmutableSet.of(
            new FibEntry(
                fibNullRoute,
                ImmutableList.of(
                    StaticRoute.builder()
                        .setNextHopIp(Ip.parse("2.2.2.2"))
                        .setNetwork(prefix)
                        .setAdministrativeCost(1)
                        .build())));

    RoutingStep routingStep = buildRoutingStep(fibNullRoute, fibEntries);

    assertThat(routingStep.getAction(), equalTo(StepAction.NULL_ROUTED));
    assertThat(
        routingStep.getDetail().getRoutes(),
        equalTo(
            ImmutableList.of(
                new RouteInfo(RoutingProtocol.STATIC, prefix, Ip.parse("2.2.2.2"), null))));
    assertThat(routingStep.getDetail().getArpIp(), nullValue());
    assertThat(routingStep.getDetail().getOutputInterface(), nullValue());
  }

  @Test
  public void testBuildDispositionStep() {
    String node = "node";
    String iface = "iface";
    Ip ip = Ip.parse("1.1.1.1");

    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname(node)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();

    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    Flow flow =
        Flow.builder()
            .setDstIp(ip)
            .setIngressNode(c.getHostname())
            .setIngressVrf(vrf.getName())
            .build();

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setConfigs(ImmutableMap.of(c.getHostname(), c)).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false);

    List<TraceAndReverseFlow> traces = new ArrayList<>();
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            c,
            null,
            new Node(c.getHostname()),
            traces::add,
            NodeInterfacePair.of(node, iface),
            ImmutableSet.of(),
            flow,
            vrf.getName(),
            new ArrayList<>(),
            ImmutableList.of(),
            new Stack<>(),
            flow);

    {
      FlowDisposition disposition = FlowDisposition.INSUFFICIENT_INFO;
      Step<?> step = flowTracer.buildArpFailureStep(iface, ip, disposition);
      assertThat(step, instanceOf(ArpErrorStep.class));
      ArpErrorStep dispositionStep = (ArpErrorStep) step;
      assertThat(dispositionStep.getAction(), equalTo(StepAction.INSUFFICIENT_INFO));
      assertThat(
          dispositionStep.getDetail().getOutputInterface(),
          equalTo(NodeInterfacePair.of(node, iface)));
      assertThat(dispositionStep.getDetail().getResolvedNexthopIp(), equalTo(ip));
    }

    {
      FlowDisposition disposition = FlowDisposition.NEIGHBOR_UNREACHABLE;
      Step<?> step = flowTracer.buildArpFailureStep(iface, ip, disposition);
      assertThat(step, instanceOf(ArpErrorStep.class));
      ArpErrorStep dispositionStep = (ArpErrorStep) step;
      assertThat(dispositionStep.getAction(), equalTo(StepAction.NEIGHBOR_UNREACHABLE));
      assertThat(
          dispositionStep.getDetail().getOutputInterface(),
          equalTo(NodeInterfacePair.of(node, iface)));
      assertThat(dispositionStep.getDetail().getResolvedNexthopIp(), equalTo(ip));
    }

    {
      FlowDisposition disposition = FlowDisposition.DELIVERED_TO_SUBNET;
      Step<?> step = flowTracer.buildArpFailureStep(iface, ip, disposition);
      assertThat(step, instanceOf(DeliveredStep.class));
      DeliveredStep dispositionStep = (DeliveredStep) step;
      assertThat(dispositionStep.getAction(), equalTo(StepAction.DELIVERED_TO_SUBNET));
      assertThat(
          dispositionStep.getDetail().getOutputInterface(),
          equalTo(NodeInterfacePair.of(node, iface)));
      assertThat(dispositionStep.getDetail().getResolvedNexthopIp(), equalTo(ip));
    }

    {
      FlowDisposition disposition = FlowDisposition.EXITS_NETWORK;
      Step<?> step = flowTracer.buildArpFailureStep(iface, ip, disposition);
      assertThat(step, instanceOf(DeliveredStep.class));
      DeliveredStep dispositionStep = (DeliveredStep) step;
      assertThat(dispositionStep.getAction(), equalTo(StepAction.EXITS_NETWORK));
      assertThat(
          dispositionStep.getDetail().getOutputInterface(),
          equalTo(NodeInterfacePair.of(node, iface)));
      assertThat(dispositionStep.getDetail().getResolvedNexthopIp(), equalTo(ip));
    }
  }

  @Test
  public void testForkTracerSameNode_transformation() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();

    Ip dstIp1 = Ip.parse("1.1.1.1");
    Flow flow =
        Flow.builder()
            .setDstIp(dstIp1)
            .setIngressNode(c.getHostname())
            .setIngressVrf(vrf.getName())
            .build();

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setConfigs(ImmutableMap.of(c.getHostname(), c)).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false);
    String node = c.getHostname();
    Configuration currentConfig = ctxt.getConfigurations().get(node);
    Stack<Breadcrumb> breadcrumbs = new Stack<>();
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            currentConfig,
            null,
            new Node(node),
            traceAndReverseFlow -> {},
            null,
            new HashSet<>(),
            flow,
            vrf.getName(),
            new ArrayList<>(),
            new ArrayList<>(),
            breadcrumbs,
            flow);

    Ip dstIp2 = Ip.parse("2.2.2.2");
    flowTracer.applyTransformation(
        Transformation.always().apply(assignDestinationIp(dstIp2)).build());

    // must add a breadcrumb before forking
    breadcrumbs.push(new Breadcrumb(c.getHostname(), vrf.getName(), flow));
    FlowTracer flowTracer2 = flowTracer.forkTracerSameNode();

    // only current flow is transformed
    assertThat(flowTracer2.getOriginalFlow().getDstIp(), equalTo(dstIp1));
    assertThat(flowTracer2.getCurrentFlow().getDstIp(), equalTo(dstIp2));
  }

  @Test
  public void testForkTracerFollowEdge_transformation() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    Interface i1 = nf.interfaceBuilder().setOwner(c1).setVrf(v1).build();

    Ip dstIp1 = Ip.parse("1.1.1.1");
    Flow flow =
        Flow.builder()
            .setDstIp(dstIp1)
            .setIngressNode(c1.getHostname())
            .setIngressVrf(v1.getName())
            .build();

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setConfigs(ImmutableMap.of(c1.getHostname(), c1)).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false);
    FlowTracer flowTracer =
        initialFlowTracer(ctxt, c1.getHostname(), null, flow, traceAndReverseFlow -> {});

    Ip dstIp2 = Ip.parse("2.2.2.2");
    flowTracer.applyTransformation(
        Transformation.always().apply(assignDestinationIp(dstIp2)).build());

    NodeInterfacePair nip = NodeInterfacePair.of(i1);
    FlowTracer flowTracer2 = flowTracer.forkTracerFollowEdge(nip, nip);

    // both original and current flows are transformed
    assertThat(flowTracer2.getOriginalFlow().getDstIp(), equalTo(dstIp2));
    assertThat(flowTracer2.getCurrentFlow().getDstIp(), equalTo(dstIp2));
  }
}
