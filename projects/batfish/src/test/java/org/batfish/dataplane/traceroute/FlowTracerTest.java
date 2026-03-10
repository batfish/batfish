package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.DENIED_OUT;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.LOOP;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.FlowDisposition.NULL_ROUTED;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.matchers.HopMatchers.hasNodeName;
import static org.batfish.datamodel.matchers.TraceAndReverseFlowMatchers.hasTrace;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.TraceMatchers.hasHops;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Interners;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.batfish.datamodel.FirewallSessionVrfInfo;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceForwardingBehavior;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.MockDataPlane;
import org.batfish.datamodel.MockFib;
import org.batfish.datamodel.MockForwardingAnalysis;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrfForwardingBehavior;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Accept;
import org.batfish.datamodel.flow.ArpErrorStep;
import org.batfish.datamodel.flow.DeliveredStep;
import org.batfish.datamodel.flow.EnterInputIfaceStep;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.ForwardOutInterface;
import org.batfish.datamodel.flow.ForwardedOutInterface;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.InboundStep;
import org.batfish.datamodel.flow.IncomingSessionScope;
import org.batfish.datamodel.flow.LoopStep;
import org.batfish.datamodel.flow.MatchSessionStep;
import org.batfish.datamodel.flow.OriginatingSessionScope;
import org.batfish.datamodel.flow.PostNatFibLookup;
import org.batfish.datamodel.flow.PreNatFibLookup;
import org.batfish.datamodel.flow.RouteInfo;
import org.batfish.datamodel.flow.RoutingStep;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.batfish.datamodel.flow.SessionMatchExpr;
import org.batfish.datamodel.flow.SetupSessionStep;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.flow.TransformationStep;
import org.batfish.datamodel.flow.TransformationStep.TransformationStepDetail;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link FlowTracer}. */
public final class FlowTracerTest {
  @Rule public TemporaryFolder _temporaryFolder = new TemporaryFolder();

  /** Creates an initial {@link FlowTracer} for a new traceroute. */
  static @Nonnull FlowTracer testFlowTracer(
      TracerouteEngineImplContext tracerouteContext,
      String node,
      @Nullable String ingressInterface,
      Flow originalFlow,
      Consumer<TraceAndReverseFlow> consumer) {
    return initialFlowTracer(
        tracerouteContext, node, ingressInterface, originalFlow, new LegacyTraceRecorder(consumer));
  }

  private TraceAndReverseFlow getAcceptTraceWithOriginatingSession(
      boolean fibLookup,
      String hostname,
      String vrfName,
      String acceptingInterface,
      NodeInterfacePair lastHopNodeAndOutgoingInterface,
      Flow flow) {
    checkArgument(flow.getIngressNode().equals(hostname), "Flow must originate at input hostname");
    checkArgument(flow.getIngressInterface() != null, "Flow must enter an interface");

    /* Simulates an incoming flow that is accepted and causes a session to be set up. */
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(hostname)
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).setName(vrfName).build();
    vrf.setFirewallSessionVrfInfo(new FirewallSessionVrfInfo(fibLookup));
    Interface ingressIface =
        nf.interfaceBuilder().setOwner(c).setVrf(vrf).setName(flow.getIngressInterface()).build();

    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);
    // Accepting interface should be the one that owns the dst IP, not necessarily ingress interface
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setForwardingAnalysis(
                    MockForwardingAnalysis.withAcceptedIps(
                        c.getHostname(),
                        vrf.getName(),
                        acceptingInterface,
                        flow.getDstIp().toIpSpace()))
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false,
            configs);
    List<TraceAndReverseFlow> traces = new ArrayList<>();
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            c,
            ingressIface.getName(),
            new Node(c.getHostname()),
            new LegacyTraceRecorder(traces::add),
            lastHopNodeAndOutgoingInterface,
            new ArrayList<>(),
            flow,
            vrf.getName(),
            new ArrayList<>(),
            new ArrayList<>(),
            new Stack<>(),
            flow,
            0,
            0,
            Interners.newStrongInterner());
    flowTracer.buildAcceptTrace();
    return Iterables.getOnlyElement(traces);
  }

  @Test
  public void testBuildAcceptTraceNewSessions() {
    String hostname = "hostname";
    String vrfName = "vrf";
    String ifaceName = "iface";
    String acceptingIface = "acceptingIface";

    Ip srcIp = Ip.parse("1.1.1.1");
    Ip dstIp = Ip.parse("2.2.2.2");
    int srcPort = 22;
    int dstPort = 40;
    Flow flow =
        Flow.builder()
            .setSrcIp(srcIp)
            .setDstIp(dstIp)
            .setIngressNode(hostname)
            .setIngressInterface(ifaceName)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(srcPort)
            .setDstPort(dstPort)
            .build();

    NodeInterfacePair lastHopNodeAndOutgoingInterface = NodeInterfacePair.of("node", "iface");

    // To match session, return flow must have same protocol and port and swapped src/dst IPs
    SessionMatchExpr flowMatchingNewSession =
        new SessionMatchExpr(IpProtocol.TCP, dstIp, srcIp, dstPort, srcPort);

    // test trace
    {
      TraceAndReverseFlow traceAndReverseFlow =
          getAcceptTraceWithOriginatingSession(
              true, hostname, vrfName, acceptingIface, lastHopNodeAndOutgoingInterface, flow);
      Trace trace = traceAndReverseFlow.getTrace();
      assertThat(trace, hasDisposition(ACCEPTED));
      assertThat(
          trace.getHops().get(0).getSteps(),
          contains(
              // Trace should include a SetupSessionStep
              instanceOf(SetupSessionStep.class),
              // Inbound step should show accepting interface, not ingress interface
              hasProperty("detail", hasProperty("interface", equalTo(acceptingIface)))));
    }

    // test session created when fibLookup = true
    {
      TraceAndReverseFlow traceAndReverseFlow =
          getAcceptTraceWithOriginatingSession(
              true, hostname, vrfName, acceptingIface, lastHopNodeAndOutgoingInterface, flow);
      Set<FirewallSessionTraceInfo> newSessions = traceAndReverseFlow.getNewFirewallSessions();
      FirewallSessionTraceInfo expectedNewSession =
          new FirewallSessionTraceInfo(
              hostname,
              PostNatFibLookup.INSTANCE,
              new OriginatingSessionScope(vrfName),
              flowMatchingNewSession,
              null);
      assertThat(newSessions, contains(expectedNewSession));
    }

    // test session created when fibLookup = false
    {
      TraceAndReverseFlow traceAndReverseFlow =
          getAcceptTraceWithOriginatingSession(
              false, hostname, vrfName, acceptingIface, lastHopNodeAndOutgoingInterface, flow);
      Set<FirewallSessionTraceInfo> newSessions = traceAndReverseFlow.getNewFirewallSessions();
      FirewallSessionTraceInfo expectedNewSession =
          new FirewallSessionTraceInfo(
              hostname,
              new ForwardOutInterface(flow.getIngressInterface(), lastHopNodeAndOutgoingInterface),
              new OriginatingSessionScope(vrfName),
              flowMatchingNewSession,
              null);
      assertThat(newSessions, contains(expectedNewSession));
    }
  }

  @Test
  public void testBuildAcceptTrace_intranodeTraffic() {
    /* Simulates an intranode flow that is accepted. No session should be set up. */
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    vrf.setFirewallSessionVrfInfo(new FirewallSessionVrfInfo(true));

    Ip srcIp = Ip.parse("1.1.1.1");
    Ip dstIp = Ip.parse("2.2.2.2");
    int srcPort = 22;
    int dstPort = 40;
    Flow flow =
        Flow.builder()
            .setSrcIp(srcIp)
            .setDstIp(dstIp)
            .setIngressNode(c.getHostname())
            .setIngressVrf(vrf.getName())
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(srcPort)
            .setDstPort(dstPort)
            .build();

    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);

    // Accepting interface should be the one that owns the dst IP
    String acceptingIfaceName = "acceptingIface";
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setForwardingAnalysis(
                    MockForwardingAnalysis.withAcceptedIps(
                        c.getHostname(), vrf.getName(), acceptingIfaceName, dstIp.toIpSpace()))
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false,
            configs);
    List<TraceAndReverseFlow> traces = new ArrayList<>();
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            c,
            null,
            new Node(c.getHostname()),
            new LegacyTraceRecorder(traces::add),
            null,
            new ArrayList<>(),
            flow,
            vrf.getName(),
            new ArrayList<>(),
            new ArrayList<>(),
            new Stack<>(),
            flow,
            0,
            0,
            Interners.newStrongInterner());

    flowTracer.buildAcceptTrace();
    TraceAndReverseFlow traceAndReverseFlow = Iterables.getOnlyElement(traces);
    assertThat(traceAndReverseFlow.getTrace(), hasDisposition(ACCEPTED));
    assertThat(traceAndReverseFlow.getNewFirewallSessions(), empty());
  }

  @Test
  public void testOriginatingFlowMatchesInboundSession() {
    /* Simulates a reverse flow originating in a VRF where a session has been set up. */
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    vrf.setFirewallSessionVrfInfo(new FirewallSessionVrfInfo(true));
    String ifaceName = "ifaceName";
    nf.interfaceBuilder()
        .setOwner(c)
        .setVrf(vrf)
        .setName(ifaceName)
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ImmutableSet.of(ifaceName), null, null))
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

    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setForwardingAnalysis(
                    MockForwardingAnalysis.withVrfForwardingBehavior(
                        c.getHostname(), vrf.getName(), VrfForwardingBehavior.builder().build()))
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(inboundSession),
            ImmutableSet.of(),
            ImmutableMap.of(
                c.getHostname(), ImmutableMap.of(vrf.getName(), MockFib.builder().build())),
            false,
            configs);

    {
      // Return flow matches session
      List<TraceAndReverseFlow> traces = new ArrayList<>();
      FlowTracer flowTracer =
          new FlowTracer(
              ctxt,
              c,
              null,
              new Node(c.getHostname()),
              new LegacyTraceRecorder(traces::add),
              null,
              new ArrayList<>(),
              returnFlow,
              vrf.getName(),
              new ArrayList<>(),
              new ArrayList<>(),
              new Stack<>(),
              returnFlow,
              0,
              0,
              Interners.newStrongInterner());
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
              new LegacyTraceRecorder(traces::add),
              null,
              new ArrayList<>(),
              nonMatchingReturnFlow,
              vrf.getName(),
              new ArrayList<>(),
              new ArrayList<>(),
              new Stack<>(),
              nonMatchingReturnFlow,
              0,
              0,
              Interners.newStrongInterner());
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
  public void testFlowMatchingSessionCanBeAccepted() {
    // Accept traces should be the same whether ingress interface does routing before or after NAT
    testFlowMatchingSessionCanBeAccepted(Action.PRE_NAT_FIB_LOOKUP, true);
    testFlowMatchingSessionCanBeAccepted(Action.POST_NAT_FIB_LOOKUP, false);
  }

  /**
   * Simulates a reverse flow to a device where a session has been set up. Tests that the device
   * accepts session-matching return flows at the right point in the NAT pipeline.
   *
   * @param ingressIfaceSessionAction NAT action that matching flows should take
   * @param acceptBeforeNat Whether the device is supposed to accept return flows before
   *     transforming them (depends on session action)
   */
  private void testFlowMatchingSessionCanBeAccepted(
      Action ingressIfaceSessionAction, boolean acceptBeforeNat) {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    String ifaceName = "ifaceName";
    nf.interfaceBuilder()
        .setOwner(c)
        .setVrf(vrf)
        .setName(ifaceName)
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                ingressIfaceSessionAction, ImmutableSet.of(ifaceName), null, null))
        .build();

    // Fields for return flow. Return flow's dst will be translated.
    Ip srcIp = Ip.parse("1.1.1.1");
    Ip preNatDstIp = Ip.parse("2.2.2.2");
    Ip postNatDstIp = Ip.parse("3.3.3.3");
    int srcPort = 22;
    int dstPort = 40;
    Flow returnFlow =
        Flow.builder()
            .setSrcIp(srcIp)
            .setDstIp(preNatDstIp)
            .setIngressNode(c.getHostname())
            .setIngressInterface(ifaceName)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(dstPort)
            .setDstPort(srcPort)
            .build();
    Transformation transformation =
        Transformation.always().apply(assignDestinationIp(postNatDstIp, postNatDstIp)).build();
    SessionMatchExpr flowMatchingNewSession =
        new SessionMatchExpr(IpProtocol.TCP, srcIp, preNatDstIp, dstPort, srcPort);
    FirewallSessionTraceInfo incomingSession =
        new FirewallSessionTraceInfo(
            c.getHostname(),
            ingressIfaceSessionAction.toSessionAction("originalIngressIface", null),
            new IncomingSessionScope(ImmutableSet.of(ifaceName)),
            flowMatchingNewSession,
            transformation);

    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);

    // Make NAT node capable of accepting both pre- and post-NAT return flows
    IpSpace acceptedIps = AclIpSpace.union(preNatDstIp.toIpSpace(), postNatDstIp.toIpSpace());
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setForwardingAnalysis(
                    MockForwardingAnalysis.withAcceptedIps(
                        c.getHostname(), vrf.getName(), ifaceName, acceptedIps))
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(incomingSession),
            ImmutableSet.of(),
            ImmutableMap.of(
                c.getHostname(), ImmutableMap.of(vrf.getName(), MockFib.builder().build())),
            false,
            configs);

    {
      // Return flow matches session
      List<TraceAndReverseFlow> traces = new ArrayList<>();
      FlowTracer flowTracer =
          new FlowTracer(
              ctxt,
              c,
              ifaceName,
              new Node(c.getHostname()),
              new LegacyTraceRecorder(traces::add),
              null,
              new ArrayList<>(),
              returnFlow,
              vrf.getName(),
              new ArrayList<>(),
              new ArrayList<>(),
              new Stack<>(),
              returnFlow,
              0,
              0,
              Interners.newStrongInterner());
      flowTracer.processHop();

      // Reverse trace should match session and get accepted.
      TraceAndReverseFlow traceAndReverseFlow = Iterables.getOnlyElement(traces);
      Trace trace = traceAndReverseFlow.getTrace();
      assertThat(trace, hasDisposition(ACCEPTED));
      if (acceptBeforeNat) {
        // If the flow was accepted before NAT was applied, there should be no transformation step.
        // TODO Should there even be a MatchSessionStep in this case?
        assertThat(
            trace.getHops().get(0).getSteps(),
            contains(
                instanceOf(EnterInputIfaceStep.class),
                instanceOf(MatchSessionStep.class),
                instanceOf(InboundStep.class)));
      } else {
        assertThat(
            trace.getHops().get(0).getSteps(),
            contains(
                instanceOf(EnterInputIfaceStep.class),
                instanceOf(MatchSessionStep.class),
                instanceOf(TransformationStep.class),
                instanceOf(InboundStep.class)));
      }
      assertThat(traceAndReverseFlow.getNewFirewallSessions(), empty());
    }

    {
      // Return flow does not match session or accepted IPs
      Flow nonMatchingReturnFlow = returnFlow.toBuilder().setDstIp(Ip.parse("1.1.1.2")).build();
      List<TraceAndReverseFlow> traces = new ArrayList<>();
      FlowTracer flowTracer =
          new FlowTracer(
              ctxt,
              c,
              ifaceName,
              new Node(c.getHostname()),
              new LegacyTraceRecorder(traces::add),
              null,
              new ArrayList<>(),
              nonMatchingReturnFlow,
              vrf.getName(),
              new ArrayList<>(),
              new ArrayList<>(),
              new Stack<>(),
              nonMatchingReturnFlow,
              0,
              0,
              Interners.newStrongInterner());
      flowTracer.processHop();

      // Reverse trace should not match session, so should be dropped (FIB has no routes)
      TraceAndReverseFlow traceAndReverseFlow = Iterables.getOnlyElement(traces);
      Trace trace = traceAndReverseFlow.getTrace();
      assertThat(trace, hasDisposition(NO_ROUTE));
      assertThat(
          trace.getHops().get(0).getSteps(),
          contains(instanceOf(EnterInputIfaceStep.class), instanceOf(RoutingStep.class)));
      assertThat(traceAndReverseFlow.getNewFirewallSessions(), empty());
    }
  }

  @Test
  public void testFlowMatchingSessionRoutedCorrectly() {
    // Test that return flows are routed correctly using PRE_NAT_FIB_LOOKUP or POST_NAT_FIB_LOOKUP.
    testFlowMatchingSessionRoutedCorrectly(Action.PRE_NAT_FIB_LOOKUP, false);
    testFlowMatchingSessionRoutedCorrectly(Action.POST_NAT_FIB_LOOKUP, true);
  }

  /**
   * Tests that a return flow matching a session on an interface with session action {@code
   * ingressIfaceSessionAction} will apply NAT in the correct order relative to routing.
   */
  private void testFlowMatchingSessionRoutedCorrectly(
      Action ingressIfaceSessionAction, boolean natBeforeRouting) {
    /*
    We will process a return flow arriving with dst IP 1.1.1.1, which will be transformed to 2.2.2.2.
    The FIB has two entries:
     - Traffic to 1.1.1.1/32 is routed out interface preNatEgressIface
     - Traffic to 2.2.2.2/32 is routed out interface postNatEgressIface
    Which route is selected will depend on whether routing happens before or after NAT.
     */
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    String ingressIface = "ingressIface";
    String preNatEgressIface = "preNatEgressIface";
    String postNatEgressIface = "postNatEgressIface";
    Interface.Builder ifaceBuilder = nf.interfaceBuilder().setOwner(c).setVrf(vrf);
    ifaceBuilder.setName(preNatEgressIface).build();
    ifaceBuilder.setName(postNatEgressIface).build();
    ifaceBuilder
        .setName(ingressIface)
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(
                Action.PRE_NAT_FIB_LOOKUP, ImmutableSet.of(ingressIface), null, null))
        .build();

    Ip preNatDstIp = Ip.parse("1.1.1.1"); // return flow dst IP before NAT
    Ip postNatDstIp = Ip.parse("2.2.2.2"); // return flow dst IP after NAT
    Ip srcIp = Ip.parse("10.10.10.10");
    int srcPort = 40;
    int dstPort = 22;
    Flow returnFlow =
        Flow.builder()
            .setSrcIp(srcIp)
            .setDstIp(preNatDstIp)
            .setIngressNode(c.getHostname())
            .setIngressInterface(ingressIface)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(srcPort)
            .setDstPort(dstPort)
            .build();

    SessionMatchExpr flowMatchingNewSession =
        new SessionMatchExpr(IpProtocol.TCP, srcIp, preNatDstIp, srcPort, dstPort);
    Transformation dstTransform =
        Transformation.always().apply(assignDestinationIp(postNatDstIp, postNatDstIp)).build();
    FirewallSessionTraceInfo incomingSession =
        new FirewallSessionTraceInfo(
            c.getHostname(),
            ingressIfaceSessionAction.toSessionAction("originalIngressIface", null),
            new IncomingSessionScope(ImmutableSet.of(ingressIface)),
            flowMatchingNewSession,
            dstTransform);

    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);

    // Create separate FIB entries for traffic routed to the pre- and post-NAT dst IPs.
    // Routes and NHIPs don't really matter; we will differentiate based on egress interface used.
    FibEntry origDstFibEntry =
        new FibEntry(
            FibForward.of(Ip.parse("12.12.12.12"), preNatEgressIface),
            ImmutableList.of(StaticRoute.testBuilder().setNetwork(preNatDstIp.toPrefix()).build()));
    FibEntry transformedDstFibEntry =
        new FibEntry(
            FibForward.of(Ip.parse("13.13.13.13"), postNatEgressIface),
            ImmutableList.of(
                StaticRoute.testBuilder().setNetwork(postNatDstIp.toPrefix()).build()));
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setForwardingAnalysis(
                    MockForwardingAnalysis
                        // Transformed return flow out either egress iface is delivered to subnet
                        .withVrfForwardingBehavior(
                        c.getHostname(),
                        vrf.getName(),
                        VrfForwardingBehavior.withInterfaceForwardingBehavior(
                            ImmutableMap.of(
                                // Use postNatDstIp for both since the flow will be
                                // transformed by the time ARP happens.
                                preNatEgressIface,
                                InterfaceForwardingBehavior.withDeliveredToSubnet(
                                    postNatDstIp.toIpSpace()),
                                postNatEgressIface,
                                InterfaceForwardingBehavior.withDeliveredToSubnet(
                                    postNatDstIp.toIpSpace())))))
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(incomingSession),
            ImmutableSet.of(),
            ImmutableMap.of(
                c.getHostname(),
                ImmutableMap.of(
                    vrf.getName(),
                    MockFib.builder()
                        .setFibEntries(
                            ImmutableMap.of(
                                // If routing happens before NAT, use FIB entry for original dst
                                preNatDstIp,
                                ImmutableSet.of(origDstFibEntry),
                                // If routing happens after NAT, use FIB entry for transformed dst
                                postNatDstIp,
                                ImmutableSet.of(transformedDstFibEntry)))
                        .build())),
            false,
            configs);
    // Flow to interface where routing happens before NAT should use 10.10.10.1 route
    List<TraceAndReverseFlow> traces = new ArrayList<>();
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            c,
            ingressIface,
            new Node(c.getHostname()),
            new LegacyTraceRecorder(traces::add),
            null,
            new ArrayList<>(),
            returnFlow,
            vrf.getName(),
            new ArrayList<>(),
            new ArrayList<>(),
            new Stack<>(),
            returnFlow,
            0,
            0,
            Interners.newStrongInterner());
    flowTracer.processHop();

    TraceAndReverseFlow traceAndReverseFlow = Iterables.getOnlyElement(traces);
    Hop hop = Iterables.getOnlyElement(traceAndReverseFlow.getTrace().getHops());
    assertThat(
        hop.getSteps(),
        contains(
            instanceOf(EnterInputIfaceStep.class),
            instanceOf(MatchSessionStep.class),
            natBeforeRouting ? instanceOf(TransformationStep.class) : instanceOf(RoutingStep.class),
            natBeforeRouting ? instanceOf(RoutingStep.class) : instanceOf(TransformationStep.class),
            instanceOf(ExitOutputIfaceStep.class),
            instanceOf(DeliveredStep.class)));
    assertThat(
        ((ExitOutputIfaceStep) (hop.getSteps().get(4)))
            .getDetail()
            .getOutputInterface()
            .getInterface(),
        equalTo(natBeforeRouting ? postNatEgressIface : preNatEgressIface));
  }

  @Test
  public void testSessionSetupForIngressInterfaces() {
    /*
     * Test that sessions are set up correctly according to egress interface's
     * FirewallSessionInterfaceInfo, which controls whether sessions can be set up by flows from a
     * given source interface and whether they can be set up by flows originating from the device.
     */
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    Interface.Builder ifaceBuilder = nf.interfaceBuilder().setOwner(c).setVrf(vrf);
    Interface eth1 = ifaceBuilder.setName("eth1").build();
    Interface eth2 = ifaceBuilder.setName("eth2").build();
    Interface eth3 = ifaceBuilder.setName("eth3").build();

    // Make a TCP flow with dst IP 1.1.1.1 (must set protocol for sessions to be set up).
    // Create traceroute context where that IP will be forwarded out eth3.
    Ip dstIp = Ip.parse("1.1.1.1");
    Flow.Builder flowBuilder =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(22)
            .setDstPort(22)
            .setDstIp(dstIp);
    StaticRoute route =
        StaticRoute.testBuilder()
            .setAdmin(1)
            .setNetwork(dstIp.toPrefix())
            .setNextHopInterface(eth3.getName())
            .build();
    DataPlane mockDataPlane =
        MockDataPlane.builder()
            .setForwardingAnalysis(
                MockForwardingAnalysis.withDeliveredToSubnetIps(
                    c.getHostname(), vrf.getName(), eth3.getName(), dstIp.toIpSpace()))
            .build();
    Fib fib =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(
                            FibForward.of(dstIp, eth3.getName()), ImmutableList.of(route)))))
            .build();
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            mockDataPlane,
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(c.getHostname(), ImmutableMap.of(vrf.getName(), fib)),
            false,
            ImmutableMap.of(c.getHostname(), c));

    // Create test flows
    Flow fromEth1 = flowBuilder.setIngressInterface(eth1.getName()).build();
    Flow fromEth2 = flowBuilder.setIngressInterface(eth2.getName()).build();
    Flow fromDevice = flowBuilder.setIngressInterface(null).setIngressVrf(vrf.getName()).build();

    {
      // eth3 should set up sessions for flows from any ingress interface or originating from device
      eth3.setFirewallSessionInterfaceInfo(
          new FirewallSessionInterfaceInfo(
              Action.PRE_NAT_FIB_LOOKUP, ImmutableSet.of(eth3.getName()), null, null, null));
      assertTrue(setsUpNewSession(c, vrf.getName(), eth1.getName(), fromEth1, ctxt));
      assertTrue(setsUpNewSession(c, vrf.getName(), eth2.getName(), fromEth2, ctxt));
      assertTrue(setsUpNewSession(c, vrf.getName(), null, fromDevice, ctxt));
    }
    {
      // eth3 should set up sessions for flows from eth1 only
      eth3.setFirewallSessionInterfaceInfo(
          new FirewallSessionInterfaceInfo(
              Action.PRE_NAT_FIB_LOOKUP,
              ImmutableSet.of(eth3.getName()),
              ImmutableSet.of(eth1.getName()),
              null,
              null));
      assertTrue(setsUpNewSession(c, vrf.getName(), eth1.getName(), fromEth1, ctxt));
      assertFalse(setsUpNewSession(c, vrf.getName(), eth2.getName(), fromEth2, ctxt));
      assertFalse(setsUpNewSession(c, vrf.getName(), null, fromDevice, ctxt));
    }
    {
      // eth3 should set up sessions for flows originating from device only
      eth3.setFirewallSessionInterfaceInfo(
          new FirewallSessionInterfaceInfo(
              Action.PRE_NAT_FIB_LOOKUP,
              ImmutableSet.of(eth3.getName()),
              ImmutableSet.of(SOURCE_ORIGINATING_FROM_DEVICE),
              null,
              null));
      assertFalse(setsUpNewSession(c, vrf.getName(), eth1.getName(), fromEth1, ctxt));
      assertFalse(setsUpNewSession(c, vrf.getName(), eth2.getName(), fromEth2, ctxt));
      assertTrue(setsUpNewSession(c, vrf.getName(), null, fromDevice, ctxt));
    }
  }

  /**
   * Returns true if processing {@code flow} in the given {@code ctxt} sets up at least one new
   * session. Assumes only one trace is produced.
   */
  private boolean setsUpNewSession(
      Configuration c,
      String vrf,
      @Nullable String ingressIface, // null if originating from device
      Flow flow,
      TracerouteEngineImplContext ctxt) {
    List<TraceAndReverseFlow> traces = new ArrayList<>();
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            c,
            ingressIface,
            new Node(c.getHostname()),
            new LegacyTraceRecorder(traces::add),
            null,
            new ArrayList<>(),
            flow,
            vrf,
            new ArrayList<>(),
            new ArrayList<>(),
            new Stack<>(),
            flow,
            0,
            0,
            Interners.newStrongInterner());
    flowTracer.processHop();
    return !Iterables.getOnlyElement(traces).getNewFirewallSessions().isEmpty();
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
                                StaticRoute.testBuilder()
                                    .setAdmin(1)
                                    .setNetwork(Prefix.ZERO)
                                    .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
                                    .build())))))
            .build();

    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(hostname, ImmutableMap.of(srcVrfName, srcFib)),
            false,
            configs);
    FlowTracer flowTracer = testFlowTracer(ctxt, hostname, null, flow, traces::add);
    flowTracer.fibLookup(dstIp, hostname, srcVrfName, srcFib);
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
        StaticRoute.testBuilder()
            .setAdmin(1)
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .setNextHop(org.batfish.datamodel.route.nh.NextHopVrf.of(nextVrfName))
            .build();
    StaticRoute nullRoute =
        StaticRoute.testBuilder()
            .setAdmin(1)
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .build();
    Fib srcFib =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(FibNextVrf.of(nextVrfName), ImmutableList.of(nextVrfRoute)))))
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
    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setForwardingAnalysis(
                    MockForwardingAnalysis.builder()
                        .setVrfForwardingBehavior(
                            ImmutableMap.of(
                                hostname,
                                ImmutableMap.of(
                                    srcVrfName, VrfForwardingBehavior.builder().build(),
                                    nextVrfName, VrfForwardingBehavior.builder().build())))
                        .build())
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(hostname, ImmutableMap.of(srcVrfName, srcFib, nextVrfName, nextFib)),
            false,
            configs);
    FlowTracer flowTracer = testFlowTracer(ctxt, hostname, null, flow, traces::add);
    flowTracer.fibLookup(dstIp, hostname, srcVrfName, srcFib);

    // Should be delegated from srcFib to nextFib and eventually NULL_ROUTED
    assertThat(traces, contains(hasTrace(hasDisposition(NULL_ROUTED))));

    List<Hop> hops = traces.get(0).getTrace().getHops();

    // There should be a single hop
    assertThat(hops, hasSize(1));

    List<Step<?>> steps = hops.iterator().next().getSteps();

    // There should be 2 routing steps and an exit-output-iface step;
    // - next-vr should occur in the first step, in src vrf
    // - null-route should occur in the second step, in next-hop vrf
    // - second step should have null-route action

    assertThat(steps, contains(instanceOf(RoutingStep.class), instanceOf(RoutingStep.class)));
    RoutingStep firstRouting = (RoutingStep) steps.get(0);
    assertThat(firstRouting.getDetail().getVrf(), equalTo(srcVrfName));
    assertThat(firstRouting.getDetail().getRoutes().get(0).getNextVrf(), equalTo(nextVrfName));
    assertThat(firstRouting.getAction(), equalTo(StepAction.FORWARDED_TO_NEXT_VRF));

    RoutingStep secondRouting = (RoutingStep) steps.get(1);
    assertThat(secondRouting.getDetail().getVrf(), equalTo(nextVrfName));
    assertThat(secondRouting.getDetail().getRoutes().get(0).getNextHopIp(), equalTo(Ip.AUTO));
    assertThat(secondRouting.getAction(), equalTo(StepAction.NULL_ROUTED));
  }

  @Test
  public void testFibLookupNextVrfAccepted() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    String hostname = c.getHostname();
    Vrf.Builder vb = nf.vrfBuilder().setOwner(c);
    Vrf srcVrf = vb.build();
    String srcVrfName = srcVrf.getName();
    Vrf nextVrf = vb.build();
    Ip dstIp = Ip.parse("1.1.1.1");
    String ifaceName = "lo";
    nf.interfaceBuilder()
        .setOwner(c)
        .setVrf(nextVrf)
        .setAddress(ConcreteInterfaceAddress.create(dstIp, Prefix.MAX_PREFIX_LENGTH))
        .setName(ifaceName)
        .build();
    String nextVrfName = nextVrf.getName();
    Flow flow =
        Flow.builder()
            .setDstIp(dstIp)
            .setIngressNode(c.getHostname())
            .setIngressVrf(srcVrfName)
            .build();
    StaticRoute nextVrfRoute =
        StaticRoute.testBuilder()
            .setAdmin(1)
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .setNextHop(org.batfish.datamodel.route.nh.NextHopVrf.of(nextVrfName))
            .build();
    StaticRoute nullRoute =
        StaticRoute.testBuilder()
            .setAdmin(1)
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .build();
    Fib srcFib =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(FibNextVrf.of(nextVrfName), ImmutableList.of(nextVrfRoute)))))
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
    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setForwardingAnalysis(
                    MockForwardingAnalysis.withAcceptedIps(
                        hostname, nextVrfName, ifaceName, dstIp.toIpSpace()))
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(hostname, ImmutableMap.of(srcVrfName, srcFib, nextVrfName, nextFib)),
            false,
            configs);
    FlowTracer flowTracer = testFlowTracer(ctxt, hostname, null, flow, traces::add);
    flowTracer.fibLookup(dstIp, hostname, srcVrfName, srcFib);

    // Should be delegated from srcFib to nextFib and eventually ACCEPTED
    assertThat(traces, contains(hasTrace(hasDisposition(ACCEPTED))));

    List<Hop> hops = traces.get(0).getTrace().getHops();

    // There should be a single hop
    assertThat(hops, hasSize(1));

    List<Step<?>> steps = hops.iterator().next().getSteps();

    // There should be next-vrf routing step and an accepted step
    assertThat(steps, contains(instanceOf(RoutingStep.class), instanceOf(InboundStep.class)));
    assertThat(
        ((RoutingStep) steps.get(0)).getDetail().getRoutes().get(0).getNextVrf(),
        equalTo(nextVrfName));
    assertThat((steps.get(0)).getAction(), equalTo(StepAction.FORWARDED_TO_NEXT_VRF));
    assertThat((steps.get(1)).getAction(), equalTo(StepAction.ACCEPTED));
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
        StaticRoute.testBuilder()
            .setAdmin(1)
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .setNextHop(org.batfish.datamodel.route.nh.NextHopVrf.of(vrf2Name))
            .build();
    StaticRoute vrf2NextVrfRoute =
        StaticRoute.testBuilder()
            .setAdmin(1)
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .setNextHop(org.batfish.datamodel.route.nh.NextHopVrf.of(vrf1Name))
            .build();
    Fib fib1 =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(FibNextVrf.of(vrf2Name), ImmutableList.of(vrf1NextVrfRoute)))))
            .build();
    Fib fib2 =
        MockFib.builder()
            .setFibEntries(
                ImmutableMap.of(
                    dstIp,
                    ImmutableSet.of(
                        new FibEntry(FibNextVrf.of(vrf1Name), ImmutableList.of(vrf2NextVrfRoute)))))
            .build();
    List<TraceAndReverseFlow> traces = new ArrayList<>();
    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setForwardingAnalysis(
                    MockForwardingAnalysis.builder()
                        .setVrfForwardingBehavior(
                            ImmutableMap.of(
                                hostname,
                                ImmutableMap.of(
                                    vrf1Name, VrfForwardingBehavior.builder().build(),
                                    vrf2Name, VrfForwardingBehavior.builder().build())))
                        .build())
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(hostname, ImmutableMap.of(vrf1Name, fib1, vrf2Name, fib2)),
            false,
            configs);
    FlowTracer flowTracer = testFlowTracer(ctxt, hostname, null, flow, traces::add);
    flowTracer.fibLookup(dstIp, hostname, vrf1Name, fib1);

    // Should be delegated from fib1 to fib2 and then looped back to fib1
    assertThat(traces, contains(hasTrace(hasDisposition(LOOP))));

    List<Hop> hops = traces.get(0).getTrace().getHops();

    // There should be a single hop
    assertThat(hops, hasSize(1));

    List<Step<?>> steps = hops.iterator().next().getSteps();

    // There should be 2 routing steps, with no action due to loop.
    // - next-vr should occur in the first step
    // - next-vr should occur in the second step

    assertThat(
        steps,
        contains(
            instanceOf(RoutingStep.class),
            instanceOf(RoutingStep.class),
            instanceOf(LoopStep.class)));
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
                            FibForward.of(finalNhip, finalNhif),
                            ImmutableList.of(
                                StaticRoute.testBuilder()
                                    .setAdmin(1)
                                    .setNetwork(Prefix.ZERO)
                                    .setNextHopIp(Ip.parse("1.2.3.4"))
                                    .build())),
                        new FibEntry(
                            FibForward.of(finalNhip, finalNhif),
                            ImmutableList.of(
                                StaticRoute.testBuilder()
                                    .setAdmin(1)
                                    .setNetwork(Prefix.ZERO)
                                    .setNextHopIp(Ip.parse("2.3.4.5"))
                                    .build())))))
            .build();
    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder()
                .setForwardingAnalysis(
                    MockForwardingAnalysis.withDeliveredToSubnetIps(
                        c.getHostname(), srcVrf.getName(), "iface1", dstIp.toIpSpace()))
                .build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(hostname, ImmutableMap.of(srcVrfName, srcFib)),
            false,
            configs);
    FlowTracer flowTracer = testFlowTracer(ctxt, hostname, null, flow, traces::add);
    flowTracer.fibLookup(dstIp, hostname, srcVrfName, srcFib);
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
                .setVrf(srcVrfName)
                .setForwardingDetail(ForwardedOutInterface.of(finalNhif, finalNhip))
                .setOutputInterface(finalNhif)
                .setArpIp(finalNhip)
                .setRoutes(
                    ImmutableList.of(
                        new RouteInfo(
                            RoutingProtocol.STATIC,
                            Prefix.ZERO,
                            NextHopIp.of(Ip.parse("1.2.3.4")),
                            1,
                            0),
                        new RouteInfo(
                            RoutingProtocol.STATIC,
                            Prefix.ZERO,
                            NextHopIp.of(Ip.parse("2.3.4.5")),
                            1,
                            0)))
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

    ImmutableMap<String, Configuration> configs =
        ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2);

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false,
            configs);
    Flow.Builder fb =
        Flow.builder()
            .setIngressNode(c1.getHostname())
            .setIngressVrf(Configuration.DEFAULT_VRF_NAME);

    // 1. evaluate dstIp=ip1 on c1. Should be transformed to ip3
    {
      Flow flow = fb.setDstIp(ip1).build();
      FlowTracer flowTracer = testFlowTracer(ctxt, c1.getHostname(), null, flow, tarf -> {});
      assertThat(flowTracer.eval(transformation).getOutputFlow().getDstIp(), equalTo(ip3));
    }

    // 2. evaluate dstIp=ip2 on c1. Should not be transformed.
    {
      Flow flow = fb.setDstIp(ip2).build();
      FlowTracer flowTracer = testFlowTracer(ctxt, c1.getHostname(), null, flow, tarf -> {});
      assertThat(flowTracer.eval(transformation).getOutputFlow().getDstIp(), equalTo(ip2));
    }

    // 3. evaluate dstIp=ip1 after forking to c2. Should not be transformed
    {
      Flow flow = fb.setDstIp(ip1).build();
      FlowTracer flowTracer = testFlowTracer(ctxt, c1.getHostname(), null, flow, tarf -> {});
      flowTracer =
          flowTracer.forkTracer(c2, null, new ArrayList<>(), null, Configuration.DEFAULT_VRF_NAME);
      assertThat(flowTracer.eval(transformation).getOutputFlow().getDstIp(), equalTo(ip1));
    }

    // 4. evaluate dstIp=ip2 after forking to c2. Should be transformed to ip3
    {
      Flow flow = fb.setDstIp(ip2).build();
      FlowTracer flowTracer = testFlowTracer(ctxt, c1.getHostname(), null, flow, tarf -> {});
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

    // Pre- or post- NAT FIB lookup: action should always be the corresponding FIB lookup
    assertThat(
        getSessionAction(Action.PRE_NAT_FIB_LOOKUP, null, lastHopNodeAndOutgoingInterface),
        equalTo(PreNatFibLookup.INSTANCE));
    assertThat(
        getSessionAction(Action.POST_NAT_FIB_LOOKUP, null, lastHopNodeAndOutgoingInterface),
        equalTo(PostNatFibLookup.INSTANCE));
    assertThat(
        getSessionAction(Action.PRE_NAT_FIB_LOOKUP, ingressIface, lastHopNodeAndOutgoingInterface),
        equalTo(PreNatFibLookup.INSTANCE));
    assertThat(
        getSessionAction(Action.POST_NAT_FIB_LOOKUP, ingressIface, lastHopNodeAndOutgoingInterface),
        equalTo(PostNatFibLookup.INSTANCE));

    // Ingress interface defined: action should be forward to last hop node and interface
    assertThat(
        getSessionAction(Action.FORWARD_OUT_IFACE, ingressIface, lastHopNodeAndOutgoingInterface),
        equalTo(new ForwardOutInterface(ingressIface, lastHopNodeAndOutgoingInterface)));

    // Ingress interface null: action should be accept (flow that set up session originated here)
    assertThat(
        getSessionAction(Action.FORWARD_OUT_IFACE, null, lastHopNodeAndOutgoingInterface),
        equalTo(Accept.INSTANCE));
  }

  @Test
  public void testMatchSessionReturnFlow() {
    BDDPacket pkt = new BDDPacket();
    IpAccessListToBdd toBdd =
        new IpAccessListToBddImpl(
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
          pkt.getFactory()
              .andAll(
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
          pkt.getFactory()
              .andAll(
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
          pkt.getFactory()
              .andAll(
                  returnFlowDstIpBdd,
                  returnFlowSrcIpBdd,
                  pkt.getIpProtocol().value(flow.getIpProtocol())));
    }
  }

  @Test
  public void testBuildRoutingStepFibForward() {
    Prefix prefix = Prefix.parse("12.12.12.12/30");
    FibForward fibForward = FibForward.of(Ip.parse("1.1.1.1"), "iface1");
    Set<FibEntry> fibEntries =
        ImmutableSet.of(
            new FibEntry(
                fibForward,
                ImmutableList.of(
                    StaticRoute.testBuilder()
                        .setNextHopIp(Ip.parse("2.2.2.2"))
                        .setNetwork(prefix)
                        .setAdministrativeCost(1)
                        .build())));

    RoutingStep routingStep = buildRoutingStep("myvrf", fibForward, fibEntries);

    assertThat(routingStep.getAction(), equalTo(StepAction.FORWARDED));
    assertThat(routingStep.getDetail().getVrf(), equalTo("myvrf"));
    assertThat(
        routingStep.getDetail().getRoutes(),
        equalTo(
            ImmutableList.of(
                new RouteInfo(
                    RoutingProtocol.STATIC, prefix, NextHopIp.of(Ip.parse("2.2.2.2")), 1, 0))));
    assertThat(routingStep.getDetail().getArpIp(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(routingStep.getDetail().getOutputInterface(), equalTo("iface1"));
  }

  @Test
  public void testBuildRoutingStepFibNextVrf() {
    Prefix prefix = Prefix.parse("12.12.12.12/30");
    FibNextVrf fibNextVrf = FibNextVrf.of("iface1");
    Set<FibEntry> fibEntries =
        ImmutableSet.of(
            new FibEntry(
                fibNextVrf,
                ImmutableList.of(
                    StaticRoute.testBuilder()
                        .setNextHopIp(Ip.parse("2.2.2.2"))
                        .setNetwork(prefix)
                        .setAdministrativeCost(1)
                        .build())));

    RoutingStep routingStep = buildRoutingStep("vrf", fibNextVrf, fibEntries);

    assertThat(routingStep.getAction(), equalTo(StepAction.FORWARDED_TO_NEXT_VRF));
    assertThat(routingStep.getDetail().getVrf(), equalTo("vrf"));
    assertThat(
        routingStep.getDetail().getRoutes(),
        equalTo(
            ImmutableList.of(
                new RouteInfo(
                    RoutingProtocol.STATIC, prefix, NextHopIp.of(Ip.parse("2.2.2.2")), 1, 0))));
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
                    StaticRoute.testBuilder()
                        .setNextHopIp(Ip.parse("2.2.2.2"))
                        .setNetwork(prefix)
                        .setAdministrativeCost(1)
                        .build())));

    RoutingStep routingStep = buildRoutingStep("vrf", fibNullRoute, fibEntries);

    assertThat(routingStep.getAction(), equalTo(StepAction.NULL_ROUTED));
    assertThat(routingStep.getDetail().getVrf(), equalTo("vrf"));
    assertThat(
        routingStep.getDetail().getRoutes(),
        equalTo(
            ImmutableList.of(
                new RouteInfo(
                    RoutingProtocol.STATIC, prefix, NextHopIp.of(Ip.parse("2.2.2.2")), 1, 0))));
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

    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false,
            configs);

    List<TraceAndReverseFlow> traces = new ArrayList<>();
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            c,
            null,
            new Node(c.getHostname()),
            new LegacyTraceRecorder(traces::add),
            NodeInterfacePair.of(node, iface),
            new ArrayList<>(),
            flow,
            vrf.getName(),
            new ArrayList<>(),
            ImmutableList.of(),
            new Stack<>(),
            flow,
            0,
            0,
            Interners.newStrongInterner());

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

  /**
   * Builds a 2-node network where n1[i1] -> n2 -> n1[i2] have static routes forcing a loop.
   * However, n1's egress ACL denies all flows that come in via i2, so no loops should actually be
   * possible - instead, the flow should be DENIED_OUT.
   */
  @Test
  public void testDeniedOutNotLoop() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration n1 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();
    Interface i1 =
        nf.interfaceBuilder()
            .setOwner(n1)
            .setVrf(v1)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/31"))
            .build();
    List<AclLine> lines =
        ImmutableList.<AclLine>builder()
            .add(
                new ExprAclLine(
                    LineAction.DENY,
                    AclLineMatchExprs.matchSrcInterface(i1.getName()),
                    "deny-from-i1"))
            .add(new ExprAclLine(LineAction.PERMIT, TrueExpr.INSTANCE, "permit-all"))
            .build();
    Interface i2 =
        nf.interfaceBuilder()
            .setOwner(n1)
            .setOutgoingFilter(
                IpAccessList.builder().setOwner(n1).setName("Deny-from-i1").setLines(lines).build())
            .setVrf(v1)
            .setAddress(ConcreteInterfaceAddress.parse("2.0.0.0/31"))
            .build();
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setAdministrativeCost(1)
                .setNetwork(Prefix.ZERO)
                .setNextHopIp(Ip.parse("2.0.0.1"))
                .build()));
    Configuration n2 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf v2 = nf.vrfBuilder().setOwner(n2).build();
    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setAdministrativeCost(1)
                .setNetwork(Prefix.ZERO)
                .setNextHopIp(Ip.parse("1.0.0.0"))
                .build()));
    nf.interfaceBuilder()
        .setOwner(n2)
        .setVrf(v2)
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/31"))
        .build();
    nf.interfaceBuilder()
        .setOwner(n2)
        .setVrf(v2)
        .setAddress(ConcreteInterfaceAddress.parse("2.0.0.1/31"))
        .build();

    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(n1.getHostname(), n1, n2.getHostname(), n2);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, _temporaryFolder);
    batfish.computeDataPlane(batfish.getSnapshot());

    Flow fromI1 =
        Flow.builder()
            .setIngressNode(n1.getHostname())
            .setIngressVrf(i1.getVrf().getName())
            .setSrcIp(Ip.parse("4.4.4.4"))
            .setDstIp(Ip.parse("8.8.8.8"))
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(NamedPort.HTTPS.number())
            .setTcpFlags(TcpFlags.builder().setSyn(true).build())
            .build();
    Flow enteringI1 =
        fromI1.toBuilder().setIngressInterface(i1.getName()).setIngressVrf(null).build();
    Flow enteringI2 = fromI1.toBuilder().setIngressInterface(i2.getName()).build();
    Map<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(batfish.getSnapshot())
            .computeTraces(ImmutableSet.of(enteringI1, enteringI2, fromI1), false);
    {
      // Entering n1[i1] is denied_out immediately upon being routed towards n2.
      Trace enteringTrace = Iterables.getOnlyElement(traces.get(enteringI1));
      assertThat(
          enteringTrace,
          allOf(hasHops(contains(hasNodeName(n1.getHostname()))), hasDisposition(DENIED_OUT)));
    }
    {
      // Originating from n1[v1] gets forwarded to n2, then back to n1, then denied_out before
      // heading back to n2.
      Trace fromTrace = Iterables.getOnlyElement(traces.get(fromI1));
      assertThat(
          fromTrace,
          allOf(
              hasHops(
                  contains(
                      hasNodeName(n1.getHostname()),
                      hasNodeName(n2.getHostname()),
                      hasNodeName(n1.getHostname()))),
              hasDisposition(DENIED_OUT)));
    }
    {
      // Entering n1[i2] is like originating from n1[v1].
      Trace enteringTrace = Iterables.getOnlyElement(traces.get(enteringI2));
      assertThat(
          enteringTrace,
          allOf(
              hasHops(
                  contains(
                      hasNodeName(n1.getHostname()),
                      hasNodeName(n2.getHostname()),
                      hasNodeName(n1.getHostname()))),
              hasDisposition(DENIED_OUT)));
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

    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false,
            configs);
    String node = c.getHostname();
    Configuration currentConfig = ctxt.getConfigurations().get(node);
    Stack<Breadcrumb> breadcrumbs = new Stack<>();
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            currentConfig,
            null,
            new Node(node),
            new LegacyTraceRecorder(traceAndReverseFlow -> {}),
            null,
            new ArrayList<>(),
            flow,
            vrf.getName(),
            new ArrayList<>(),
            new ArrayList<>(),
            breadcrumbs,
            flow,
            0,
            0,
            Interners.newStrongInterner());

    Ip dstIp2 = Ip.parse("2.2.2.2");
    flowTracer.applyTransformation(
        Transformation.always().apply(assignDestinationIp(dstIp2)).build());

    // must add a breadcrumb before forking
    breadcrumbs.push(new Breadcrumb(c.getHostname(), vrf.getName(), null, flow));
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

    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c1.getHostname(), c1);

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false,
            configs);
    FlowTracer flowTracer =
        testFlowTracer(ctxt, c1.getHostname(), null, flow, traceAndReverseFlow -> {});

    Ip dstIp2 = Ip.parse("2.2.2.2");
    flowTracer.applyTransformation(
        Transformation.always().apply(assignDestinationIp(dstIp2)).build());

    NodeInterfacePair nip = NodeInterfacePair.of(i1);
    FlowTracer flowTracer2 = flowTracer.forkTracerFollowEdge(nip, nip);

    // both original and current flows are transformed
    assertThat(flowTracer2.getOriginalFlow().getDstIp(), equalTo(dstIp2));
    assertThat(flowTracer2.getCurrentFlow().getDstIp(), equalTo(dstIp2));
  }

  @Test
  public void testOutgoingOriginalFlowFilter() {
    // Tests that outgoing original flow filter really acts on the original flow and not current
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    Interface iface = nf.interfaceBuilder().setOwner(c).setVrf(vrf).build();

    Ip blockedSrcIp = Ip.parse("1.1.1.1");
    Ip permittedSrcIp = Ip.parse("1.1.1.2");
    Ip dstIp = Ip.parse("2.2.2.2");

    // Filter denies src IP blockedSrcIp, permits everything else
    IpAccessList filter =
        nf.aclBuilder()
            .setOwner(c)
            .setLines(
                ExprAclLine.rejecting(AclLineMatchExprs.matchSrc(blockedSrcIp)),
                ExprAclLine.ACCEPT_ALL)
            .build();
    iface.setOutgoingOriginalFlowFilter(filter);

    Flow flowWithBlockedSrc =
        Flow.builder()
            .setSrcIp(blockedSrcIp)
            .setDstIp(dstIp)
            .setIngressNode(c.getHostname())
            .setIngressVrf(vrf.getName())
            .build();
    Flow flowWithPermittedSrc = flowWithBlockedSrc.toBuilder().setSrcIp(permittedSrcIp).build();

    // Set up forwarding analysis to consider flows to dstIp delivered to subnet
    ForwardingAnalysis forwardingAnalysis =
        MockForwardingAnalysis.withDeliveredToSubnetIps(
            c.getHostname(), vrf.getName(), iface.getName(), dstIp.toIpSpace());
    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c.getHostname(), c);

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setForwardingAnalysis(forwardingAnalysis).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false,
            configs);

    // There's a sanity check in FlowTracer constructor that requires there to be a PolicyStep or
    // TransformationStep if the current and original flows don't match. Doesn't matter if this
    // doesn't accurately represent the transformation.
    FlowDiff flowDiff = FlowDiff.flowDiff(IpField.SOURCE, blockedSrcIp, permittedSrcIp);
    TransformationStep placeholderTransformationStep =
        new TransformationStep(
            new TransformationStepDetail(
                TransformationType.SOURCE_NAT, ImmutableSortedSet.of(flowDiff)),
            StepAction.TRANSFORMED);

    {
      // When original flow has the blocked src IP, it should get denied out
      List<Step<?>> steps = new ArrayList<>();
      steps.add(placeholderTransformationStep);
      List<TraceAndReverseFlow> traces = new ArrayList<>();
      FlowTracer flowTracer =
          new FlowTracer(
              ctxt,
              c,
              null,
              new Node(c.getHostname()),
              new LegacyTraceRecorder(traces::add),
              NodeInterfacePair.of("node", "iface"),
              new ArrayList<>(),
              flowWithBlockedSrc, // original flow
              vrf.getName(),
              new ArrayList<>(),
              steps,
              new Stack<>(),
              flowWithPermittedSrc, // current flow
              0,
              0,
              Interners.newStrongInterner());

      flowTracer.forwardOutInterface(iface, dstIp, null);
      assertThat(traces, hasSize(1));
      assertThat(traces.get(0).getTrace(), hasDisposition(DENIED_OUT));
    }

    {
      // When current flow has the blocked src IP (but original doesn't), it should succeed
      List<Step<?>> steps = new ArrayList<>();
      steps.add(placeholderTransformationStep);
      List<TraceAndReverseFlow> traces = new ArrayList<>();
      FlowTracer flowTracer =
          new FlowTracer(
              ctxt,
              c,
              null,
              new Node(c.getHostname()),
              new LegacyTraceRecorder(traces::add),
              NodeInterfacePair.of("node", "iface"),
              new ArrayList<>(),
              flowWithPermittedSrc, // original flow
              vrf.getName(),
              new ArrayList<>(),
              steps,
              new Stack<>(),
              flowWithBlockedSrc, // current flow
              0,
              0,
              Interners.newStrongInterner());

      flowTracer.forwardOutInterface(iface, dstIp, null);
      assertThat(traces, hasSize(1));
      assertThat(traces.get(0).getTrace(), hasDisposition(DELIVERED_TO_SUBNET));
    }
  }

  /**
   * A {@link TraceRecorder} that only records complete traces, and passes them to a {@link
   * Consumer}.
   */
  public static final class LegacyTraceRecorder implements TraceRecorder {

    private final Consumer<TraceAndReverseFlow> _consumer;

    LegacyTraceRecorder(Consumer<TraceAndReverseFlow> consumer) {
      _consumer = consumer;
    }

    @Override
    public void recordTrace(List<HopInfo> hopInfos) {
      HopInfo lastHop = hopInfos.get(hopInfos.size() - 1);
      FlowDisposition disposition = lastHop.getDisposition();
      checkArgument(disposition != null, "Last hop of a complete trace must have a disposition");
      List<Hop> hops =
          hopInfos.stream().map(HopInfo::getHop).collect(ImmutableList.toImmutableList());
      Set<FirewallSessionTraceInfo> newSessions =
          hopInfos.stream()
              .map(HopInfo::getFirewallSessionTraceInfo)
              .filter(Objects::nonNull)
              .collect(ImmutableSet.toImmutableSet());
      _consumer.accept(
          new TraceAndReverseFlow(
              new Trace(disposition, hops), lastHop.getReturnFlow(), newSessions));
    }

    @Override
    public boolean tryRecordPartialTrace(List<HopInfo> hops) {
      return false;
    }
  }
}
