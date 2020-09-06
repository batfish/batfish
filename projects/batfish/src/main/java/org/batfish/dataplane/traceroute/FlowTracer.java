package org.batfish.dataplane.traceroute;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Comparator.comparing;
import static org.batfish.datamodel.FlowDiff.flowDiffs;
import static org.batfish.datamodel.FlowDiff.returnFlowDiffs;
import static org.batfish.datamodel.flow.FilterStep.FilterType.EGRESS_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.EGRESS_ORIGINAL_FLOW_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.INGRESS_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.POST_TRANSFORMATION_INGRESS_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.PRE_TRANSFORMATION_EGRESS_FILTER;
import static org.batfish.datamodel.flow.StepAction.DENIED;
import static org.batfish.datamodel.flow.StepAction.FORWARDED;
import static org.batfish.datamodel.flow.StepAction.FORWARDED_TO_NEXT_VRF;
import static org.batfish.datamodel.flow.StepAction.NULL_ROUTED;
import static org.batfish.datamodel.flow.StepAction.PERMITTED;
import static org.batfish.datamodel.flow.StepAction.TRANSMITTED;
import static org.batfish.dataplane.traceroute.HopInfo.failureHop;
import static org.batfish.dataplane.traceroute.HopInfo.forwardedHop;
import static org.batfish.dataplane.traceroute.HopInfo.loopHop;
import static org.batfish.dataplane.traceroute.HopInfo.successHop;
import static org.batfish.dataplane.traceroute.TracerouteUtils.buildEnterSrcIfaceStep;
import static org.batfish.dataplane.traceroute.TracerouteUtils.createFilterStep;
import static org.batfish.dataplane.traceroute.TracerouteUtils.fibEntriesToRouteInfos;
import static org.batfish.dataplane.traceroute.TracerouteUtils.getFinalActionForDisposition;
import static org.batfish.dataplane.traceroute.TracerouteUtils.returnFlow;
import static org.batfish.dataplane.traceroute.TracerouteUtils.sessionTransformation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibAction;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionVrfInfo;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.acl.Evaluator;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Accept;
import org.batfish.datamodel.flow.ArpErrorStep;
import org.batfish.datamodel.flow.ArpErrorStep.ArpErrorStepDetail;
import org.batfish.datamodel.flow.DeliveredStep;
import org.batfish.datamodel.flow.DeliveredStep.DeliveredStepDetail;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;
import org.batfish.datamodel.flow.FilterStep;
import org.batfish.datamodel.flow.FilterStep.FilterType;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.ForwardOutInterface;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.InboundStep;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;
import org.batfish.datamodel.flow.IncomingSessionScope;
import org.batfish.datamodel.flow.LoopStep;
import org.batfish.datamodel.flow.MatchSessionStep;
import org.batfish.datamodel.flow.MatchSessionStep.MatchSessionStepDetail;
import org.batfish.datamodel.flow.OriginateStep;
import org.batfish.datamodel.flow.OriginateStep.OriginateStepDetail;
import org.batfish.datamodel.flow.OriginatingSessionScope;
import org.batfish.datamodel.flow.PolicyStep;
import org.batfish.datamodel.flow.PolicyStep.PolicyStepDetail;
import org.batfish.datamodel.flow.RoutingStep;
import org.batfish.datamodel.flow.RoutingStep.Builder;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.batfish.datamodel.flow.SessionAction;
import org.batfish.datamodel.flow.SessionMatchExpr;
import org.batfish.datamodel.flow.SessionScope;
import org.batfish.datamodel.flow.SetupSessionStep;
import org.batfish.datamodel.flow.SetupSessionStep.SetupSessionStepDetail;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.flow.TransformationStep;
import org.batfish.datamodel.packet_policy.ActionVisitor;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.FibLookupOverrideLookupIp;
import org.batfish.datamodel.packet_policy.FlowEvaluator;
import org.batfish.datamodel.packet_policy.FlowEvaluator.FlowResult;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.VrfExprNameExtractor;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationEvaluator;
import org.batfish.datamodel.transformation.TransformationEvaluator.TransformationResult;
import org.batfish.datamodel.visitors.FibActionVisitor;
import org.batfish.datamodel.visitors.SessionActionVisitor;

/**
 * Generates {@link Trace Traces} for a particular flow. Does depth-first search over all ECMP paths
 * for the flow. When the path terminates (for whatever reason), builds a trace and passes to a
 * {@link Consumer}.
 *
 * <p>Maintains some state about the current path: the current flow, breadcrumbs for loop detection,
 * new sessions setup by the path, {@link Trace trace} {@link Hop hops} for previously visited
 * nodes, etc.
 *
 * <p>Also contains contextual information about the current hop: its {@link Configuration}, which
 * node it entered (if any), the previously exited node/interface (if any), etc.
 */
class FlowTracer {

  /* Comparator used to deterministically order FibAction branches to visit */
  private static final class FibActionComparator implements Comparator<FibAction> {

    private static final Comparator<FibAction> INSTANCE = new FibActionComparator();
    private static final Comparator<FibForward> FIB_FORWARD_COMPARATOR =
        comparing(FibForward::getInterfaceName).thenComparing(FibForward::getArpIp);

    /**
     * new FibActionSameTypeComparator(a).visit(b) compares a and b and requires that a.getClass()
     * == b.getClass()
     */
    private static final class FibActionSameTypeComparator implements FibActionVisitor<Integer> {

      private final FibAction _rhs;

      private FibActionSameTypeComparator(FibAction rhs) {
        _rhs = rhs;
      }

      @Override
      public Integer visitFibForward(FibForward fibForward) {
        return FIB_FORWARD_COMPARATOR.compare(fibForward, (FibForward) _rhs);
      }

      @Override
      public Integer visitFibNextVrf(FibNextVrf fibNextVrf) {
        return fibNextVrf.getNextVrf().compareTo(((FibNextVrf) _rhs).getNextVrf());
      }

      @Override
      public Integer visitFibNullRoute(FibNullRoute fibNullRoute) {
        // guarantee type correctness
        FibNullRoute.class.cast(_rhs);
        return 0;
      }
    }

    private static final FibActionVisitor<Integer> FIB_ACTION_TYPE_PRECEDENCE =
        new FibActionVisitor<Integer>() {
          @Override
          public Integer visitFibForward(FibForward fibForward) {
            return 1;
          }

          @Override
          public Integer visitFibNextVrf(FibNextVrf fibNextVrf) {
            return 2;
          }

          @Override
          public Integer visitFibNullRoute(FibNullRoute fibNullRoute) {
            return 3;
          }
        };

    @Override
    public int compare(@Nonnull FibAction lhs, @Nonnull FibAction rhs) {
      int ret =
          Integer.compare(
              lhs.accept(FIB_ACTION_TYPE_PRECEDENCE), rhs.accept(FIB_ACTION_TYPE_PRECEDENCE));
      if (ret != 0) {
        return ret;
      }
      return lhs.accept(new FibActionSameTypeComparator(rhs));
    }
  }

  private final TracerouteEngineImplContext _tracerouteContext;
  private final Configuration _currentConfig;
  private final @Nullable String _ingressInterface;
  private final Node _currentNode;
  private final TraceRecorder _traceRecorder;
  private final @Nullable NodeInterfacePair _lastHopNodeAndOutgoingInterface;
  private final List<FirewallSessionTraceInfo> _newSessions;
  private final int _origNewSessionsSize; // size of _newSessions at construction
  private final Flow _originalFlow;
  private final @Nonnull String _vrfName;

  // Mutable list of hops in the current trace
  private final List<HopInfo> _hops;

  // Mutable list of steps in the current hop
  private final List<Step<?>> _steps;

  private final Stack<Breadcrumb> _breadcrumbs;
  private final int _origBreadcrumbsSize; // size of _breadcrumbs at construction

  // The current flow can change as we process the packet.
  private Flow _currentFlow;

  /** Creates an initial {@link FlowTracer} for a new traceroute. */
  @Nonnull
  static FlowTracer initialFlowTracer(
      TracerouteEngineImplContext tracerouteContext,
      String node,
      @Nullable String ingressInterface,
      Flow originalFlow,
      Consumer<TraceAndReverseFlow> consumer) {
    return initialFlowTracer(
        tracerouteContext, node, ingressInterface, originalFlow, new LegacyTraceRecorder(consumer));
  }

  /** Creates an initial {@link FlowTracer} for a new traceroute. */
  @Nonnull
  static FlowTracer initialFlowTracer(
      TracerouteEngineImplContext tracerouteContext,
      String node,
      @Nullable String ingressInterface,
      Flow originalFlow,
      TraceRecorder traceRecorder) {
    Configuration currentConfig = tracerouteContext.getConfigurations().get(node);
    return new FlowTracer(
        tracerouteContext,
        currentConfig,
        ingressInterface,
        new Node(node),
        traceRecorder,
        null,
        new ArrayList<>(),
        originalFlow,
        initVrfName(ingressInterface, currentConfig, originalFlow),
        new ArrayList<>(),
        new ArrayList<>(),
        new Stack<>(),
        originalFlow,
        0,
        0);
  }

  /**
   * Forks a {@link FlowTracer} that starts at {@code newIngressInterface} of {@code newVrfName} on
   * {@code newConfig}, optionally having come from {@code lastHopNodeAndOutgoingInterface} after
   * taking {@code initialSteps} since the beginning of the trace.
   */
  @VisibleForTesting
  FlowTracer forkTracer(
      Configuration newConfig,
      @Nullable String newIngressInterface,
      List<Step<?>> initialSteps,
      NodeInterfacePair lastHopNodeAndOutgoingInterface,
      String newVrfName) {

    // hops and sessions are per-trace.
    return new FlowTracer(
        _tracerouteContext,
        newConfig,
        newIngressInterface,
        new Node(newConfig.getHostname()),
        _traceRecorder,
        lastHopNodeAndOutgoingInterface,
        new ArrayList<>(_newSessions),
        _originalFlow,
        newVrfName,
        new ArrayList<>(_hops),
        new ArrayList<>(initialSteps),
        _breadcrumbs,
        _currentFlow,
        _newSessions.size(),
        _breadcrumbs.size());
  }

  private static @Nonnull String initVrfName(
      @Nullable String ingressInterface, Configuration currentConfig, Flow currentFlow) {
    String vrfName;
    if (ingressInterface == null) {
      checkState(
          currentFlow.getIngressNode().equals(currentConfig.getHostname()),
          "Not ingressNode but ingressInterface is null");
      vrfName = currentFlow.getIngressVrf();
    } else {
      vrfName = currentConfig.getAllInterfaces().get(ingressInterface).getVrfName();
    }
    checkNotNull(vrfName, "Missing VRF.");
    return vrfName;
  }

  /** Construct a {@link FlowTracer} with explicitly-provided fields. */
  @VisibleForTesting
  FlowTracer(
      TracerouteEngineImplContext tracerouteContext,
      Configuration currentConfig,
      @Nullable String ingressInterface,
      Node currentNode,
      TraceRecorder traceRecorder,
      @Nullable NodeInterfacePair lastHopNodeAndOutgoingInterface,
      List<FirewallSessionTraceInfo> newSessions,
      Flow originalFlow,
      @Nonnull String vrfName,
      List<HopInfo> hops,
      List<Step<?>> steps,
      Stack<Breadcrumb> breadcrumbs,
      Flow currentFlow,
      int origNewSessionsSize,
      int origBreadcrumbsSize) {
    assert originalFlow.equals(currentFlow)
            || steps.stream()
                .anyMatch(step -> step instanceof TransformationStep || step instanceof PolicyStep)
        : "Original flow and current flow must be equal unless there's a transformation step or a policy step";
    _tracerouteContext = tracerouteContext;
    _currentConfig = currentConfig;
    _ingressInterface = ingressInterface;
    _currentNode = currentNode;
    _traceRecorder = traceRecorder;
    _lastHopNodeAndOutgoingInterface = lastHopNodeAndOutgoingInterface;
    _newSessions = newSessions;
    _originalFlow = originalFlow;
    _vrfName = vrfName;
    _hops = hops;
    _steps = steps;
    _breadcrumbs = breadcrumbs;
    _currentFlow = currentFlow;
    _origNewSessionsSize = origNewSessionsSize;
    _origBreadcrumbsSize = origBreadcrumbsSize;
  }

  @Nullable
  Breadcrumb getVisitedBreadcrumb() {
    int breadcrumbsSize = _breadcrumbs.size();
    checkState(
        breadcrumbsSize == _origBreadcrumbsSize || breadcrumbsSize == _origBreadcrumbsSize + 1,
        "Breadcrumbs can only grow, and only by 1 breadcrumb per hop");
    return breadcrumbsSize == _origBreadcrumbsSize ? null : _breadcrumbs.peek();
  }

  @Nullable
  FirewallSessionTraceInfo getHopSessionInfo() {
    int newSessionsSize = _newSessions.size();
    checkState(
        newSessionsSize == _origNewSessionsSize || newSessionsSize == _origNewSessionsSize + 1,
        "new sessions can only grow, and only by 1 breadcrumb per hop");
    return newSessionsSize == _origNewSessionsSize ? null : _newSessions.get(newSessionsSize - 1);
  }

  /**
   * Return forked {@link FlowTracer} starting at {@code enterIface} having just come from {@code
   * exitIface} after a hop has been added.
   */
  @VisibleForTesting
  FlowTracer forkTracerFollowEdge(NodeInterfacePair exitIface, NodeInterfacePair enterIface) {
    checkState(
        _hops.size() == _breadcrumbs.size(), "Must have equal number of hops and breadcrumbs");
    // grab configuration-specific information from the node that owns enterIface
    String newHostname = enterIface.getHostname();
    Configuration newConfig = _tracerouteContext.getConfigurations().get(newHostname);
    checkArgument(
        newConfig != null, "Node %s is not in the network, cannot perform traceroute", newHostname);
    String newIngressInterface = enterIface.getInterface();

    // hops and sessions are per-trace.
    return new FlowTracer(
        _tracerouteContext,
        newConfig,
        newIngressInterface,
        new Node(newConfig.getHostname()),
        _traceRecorder,
        exitIface,
        new ArrayList<>(_newSessions),
        // the original flow of the next hop is the final (i.e. current) flow of this hop
        _currentFlow,
        initVrfName(newIngressInterface, newConfig, _currentFlow),
        new ArrayList<>(_hops),
        new ArrayList<>(ImmutableList.of()),
        _breadcrumbs,
        _currentFlow,
        _newSessions.size(),
        _breadcrumbs.size());
  }

  /** Return forked {@link FlowTracer} on same node and VRF. Used for taking ECMP actions. */
  @VisibleForTesting
  @Nonnull
  FlowTracer forkTracerSameNode() {
    return forkTracerSameNode(_vrfName);
  }

  /**
   * Return forked {@link FlowTracer} on same node and VRF identified by {@code newVrfName}. Used
   * for taking next-vrf action (different VRF) or ECMP action (same VRF).
   */
  private @Nonnull FlowTracer forkTracerSameNode(String newVrfName) {
    checkState(_hops.size() == _breadcrumbs.size() - 1, "Must be just ready to add another hop");

    // hops and sessions are per-trace.
    return new FlowTracer(
        _tracerouteContext,
        _currentConfig,
        _ingressInterface,
        new Node(_currentConfig.getHostname()),
        _traceRecorder,
        _lastHopNodeAndOutgoingInterface,
        new ArrayList<>(_newSessions),
        _originalFlow,
        newVrfName,
        new ArrayList<>(_hops),
        new ArrayList<>(_steps),
        _breadcrumbs,
        _currentFlow,
        _origNewSessionsSize,
        _origBreadcrumbsSize);
  }

  private void processOutgoingInterfaceEdges(
      String outgoingInterface, Ip nextHopIp, SortedSet<NodeInterfacePair> neighborIfaces) {
    checkArgument(!neighborIfaces.isEmpty(), "No neighbor interfaces.");
    checkState(
        _steps.get(_steps.size() - 1) instanceof ExitOutputIfaceStep,
        "ExitOutputIfaceStep needs to be added before calling this function");
    Ip arpIp =
        Route.UNSET_ROUTE_NEXT_HOP_IP.equals(nextHopIp) ? _currentFlow.getDstIp() : nextHopIp;

    SortedSet<NodeInterfacePair> interfacesThatReplyToArp =
        neighborIfaces.stream()
            .filter(
                iface ->
                    _tracerouteContext.repliesToArp(
                        iface.getHostname(), iface.getInterface(), arpIp))
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));

    if (interfacesThatReplyToArp.isEmpty()) {
      FlowDisposition disposition =
          _tracerouteContext.computeDisposition(
              _currentNode.getName(), outgoingInterface, _currentFlow.getDstIp());
      buildArpFailureTrace(outgoingInterface, arpIp, disposition);
      return;
    }

    Hop hop = new Hop(_currentNode, _steps);
    _hops.add(forwardedHop(hop, _originalFlow, getVisitedBreadcrumb(), getHopSessionInfo()));
    if (_traceRecorder.tryRecordPartialTrace(_hops)) {
      return;
    }

    NodeInterfacePair exitIface = NodeInterfacePair.of(_currentNode.getName(), outgoingInterface);
    interfacesThatReplyToArp.forEach(
        enterIface -> forkTracerFollowEdge(exitIface, enterIface).processHop());
  }

  @Nonnull
  private ExitOutputIfaceStep buildExitOutputIfaceStep(String outputIface) {
    return ExitOutputIfaceStep.builder()
        .setDetail(
            ExitOutputIfaceStepDetail.builder()
                .setOutputInterface(NodeInterfacePair.of(_currentNode.getName(), outputIface))
                .setTransformedFlow(TracerouteUtils.hopFlow(_originalFlow, _currentFlow))
                .build())
        .setAction(TRANSMITTED)
        .build();
  }

  @Nonnull
  private ArpErrorStep buildArpErrorStep(String outputIface, Ip nhIp, StepAction action) {
    return ArpErrorStep.builder()
        .setDetail(
            ArpErrorStepDetail.builder()
                .setOutputInterface(NodeInterfacePair.of(_currentNode.getName(), outputIface))
                .setResolvedNexthopIp(nhIp)
                .build())
        .setAction(action)
        .build();
  }

  @Nonnull
  private DeliveredStep buildDeliveredStep(String outputIface, Ip nhIp, StepAction action) {
    return DeliveredStep.builder()
        .setDetail(
            DeliveredStepDetail.builder()
                .setOutputInterface(NodeInterfacePair.of(_currentNode.getName(), outputIface))
                .setResolvedNexthopIp(nhIp)
                .build())
        .setAction(action)
        .build();
  }

  void processHop() {
    checkState(_steps.isEmpty(), "Steps must be empty when processHop is called");
    checkState(
        _hops.size() == _breadcrumbs.size(), "Must have equal number of hops and breadcrumbs");

    String currentNodeName = _currentNode.getName();

    // Trace was received on a source interface of this hop - this should always be first.
    if (_ingressInterface != null) {
      _steps.add(buildEnterSrcIfaceStep(_currentConfig, _ingressInterface));
    }

    if (processSessions()) {
      // flow was processed by a session, rest of pipeline including inbound ACL is skipped.
      return;
    }

    // trace was received on a source interface of this hop
    if (_ingressInterface != null) {
      // apply ingress filter
      Interface incomingInterface = _currentConfig.getAllInterfaces().get(_ingressInterface);
      // if defined, use routing/packet policy applied to the interface
      if (processPBR(incomingInterface)) {
        return;
      }

      // if wasn't processed by packet policy, just apply ingress filter
      IpAccessList inputFilter = incomingInterface.getIncomingFilter();
      if (inputFilter != null) {
        if (applyFilter(inputFilter, INGRESS_FILTER) == DENIED) {
          return;
        }
      }

      applyTransformation(incomingInterface.getIncomingTransformation());

      inputFilter = incomingInterface.getPostTransformationIncomingFilter();
      if (applyFilter(inputFilter, POST_TRANSFORMATION_INGRESS_FILTER) == DENIED) {
        return;
      }
    } else {
      // if inputIfaceName is not set for this hop, this is the originating step
      _steps.add(buildOriginateStep());
    }

    Ip dstIp = _currentFlow.getDstIp();

    // Accept if the flow is destined for this vrf on this host.
    if (_tracerouteContext.vrfAcceptsIp(currentNodeName, _vrfName, dstIp)) {
      buildAcceptTrace();
      return;
    }

    Fib fib = _tracerouteContext.getFib(currentNodeName, _vrfName).get();
    fibLookup(dstIp, currentNodeName, fib);
  }

  /**
   * Applies PBR to the flow at the given {@code incomingInterface} if a packet policy is present.
   * Returns true if PBR was applied.
   */
  private boolean processPBR(Interface incomingInterface) {

    // apply routing/packet policy applied to the interface, if defined.
    String policyName = incomingInterface.getRoutingPolicyName();
    if (policyName == null) {
      return false;
    }
    PacketPolicy policy = _currentConfig.getPacketPolicies().get(policyName);
    if (policy == null) {
      return false;
    }

    // Policy is present. If this point is reached, processPBR will return true.
    Configuration owner = incomingInterface.getOwner();
    FlowResult result =
        FlowEvaluator.evaluate(
            _currentFlow,
            incomingInterface.getName(),
            incomingInterface.getVrfName(),
            policy,
            owner.getIpAccessLists(),
            owner.getIpSpaces(),
            _tracerouteContext.getFibs(_currentNode.getName()));
    _currentFlow = result.getFinalFlow();

    new ActionVisitor<Void>() {

      /** Helper visitor to figure out in which VRF we need to do the FIB lookup */
      private VrfExprNameExtractor _vrfExprVisitor = new VrfExprNameExtractor(incomingInterface);

      @Override
      public Void visitDrop(@Nonnull Drop drop) {
        _steps.add(new PolicyStep(new PolicyStepDetail(policy.getName()), DENIED));
        buildDeniedTrace(FlowDisposition.DENIED_IN);
        return null;
      }

      @Override
      public Void visitFibLookup(@Nonnull FibLookup fibLookup) {
        makePermittedStep();
        Ip dstIp = result.getFinalFlow().getDstIp();

        // Accept if the flow is destined for this vrf on this host.
        if (isAcceptedAtCurrentVrf()) {
          return null;
        }

        String currentNodeName = _currentNode.getName();
        String lookupVrfName = fibLookup.getVrfExpr().accept(_vrfExprVisitor);
        Fib fib = _tracerouteContext.getFib(currentNodeName, lookupVrfName).get();
        fibLookup(dstIp, currentNodeName, fib);
        return null;
      }

      @Override
      public Void visitFibLookupOverrideLookupIp(
          @Nonnull FibLookupOverrideLookupIp fibLookupAction) {
        makePermittedStep();

        // Accept if the flow is destined for this vrf on this host.
        if (isAcceptedAtCurrentVrf()) {
          return null;
        }

        String currentNodeName = _currentNode.getName();
        String lookupVrfName = fibLookupAction.getVrfExpr().accept(_vrfExprVisitor);
        List<Ip> ips = fibLookupAction.getIps();
        // Determine the override next hop ip. This IP will be re-resolved in the FIB (and, if
        // applicable, used as ARP IP)
        Ip lookupIp =
            ips.stream()
                .filter(
                    ip -> {
                      // Intentionally forcing crashes with .get() here
                      Fib fib = _tracerouteContext.getFib(currentNodeName, lookupVrfName).get();
                      Set<FibEntry> entries =
                          fibLookupAction.requireConnected()
                              ? fib.get(ip).stream()
                                  // Filter entries if a directly connected hext hop is required
                                  .filter(
                                      entry -> entry.getTopLevelRoute() instanceof ConnectedRoute)
                                  .collect(ImmutableSet.toImmutableSet())
                              : fib.get(ip);
                      return !entries.isEmpty();
                    })
                .findFirst()
                .orElse(null);

        if (lookupIp == null) {
          // Nothing matched, execute default action
          visit(fibLookupAction.getDefaultAction());
          return null;
        }

        // Just a sanity check, can't be Ip.AUTO
        assert lookupIp.valid();

        Fib fib = _tracerouteContext.getFib(currentNodeName, incomingInterface.getVrfName()).get();
        // Re-resolve and send out using FIB lookup part of the pipeline.
        // Call the version of fibLookup that keeps track of overridden nextHopIp
        Ip dstIp = result.getFinalFlow().getDstIp();
        fibLookup(dstIp, lookupIp, currentNodeName, fib);
        return null;
      }

      /**
       * Check if the packet should be accepted in current VRF. If yes, build accept trace, returns
       * true. If no, returns false.
       */
      private boolean isAcceptedAtCurrentVrf() {
        String currentNodeName = _currentNode.getName();
        Ip dstIp = result.getFinalFlow().getDstIp();
        if (_tracerouteContext.vrfAcceptsIp(currentNodeName, _vrfName, dstIp)) {
          buildAcceptTrace();
          return true;
        }
        return false;
      }

      private void makePermittedStep() {
        _steps.add(new PolicyStep(new PolicyStepDetail(policy.getName()), PERMITTED));
      }
    }.visit(result.getAction());

    return true;
  }

  /** Apply the input {@link Transformation} to the current flow in the current context. */
  @VisibleForTesting
  void applyTransformation(Transformation transformation) {
    TransformationResult transformationResult = eval(transformation);
    _steps.addAll(transformationResult.getTraceSteps());
    _currentFlow = transformationResult.getOutputFlow();
  }

  /** Evaluate the input {@link Transformation} against the current flow in the current context. */
  @VisibleForTesting
  TransformationResult eval(Transformation transformation) {
    return TransformationEvaluator.eval(
        transformation,
        _currentFlow,
        _ingressInterface,
        _currentConfig.getIpAccessLists(),
        _currentConfig.getIpSpaces());
  }

  /**
   * Perform a FIB lookup of {@code dstIp} on {@code fib} of {@code currentNodeName} and take
   * corresponding actions.
   */
  @VisibleForTesting
  void fibLookup(Ip dstIp, String currentNodeName, Fib fib) {
    fibLookup(dstIp, null, currentNodeName, fib);
  }

  /**
   * Perform a FIB lookup for the {@code overrideNextHopIp} next hop IP and take corresponding
   * actions. Note that {@code dstIp} can still be used as ARP IP in the scenario that both {@code
   * overrideNextHopIp} and the FIB entry's ARP IP is missing.
   */
  private void fibLookup(
      Ip dstIp, @Nullable Ip overrideNextHopIp, String currentNodeName, Fib fib) {
    fibLookup(
        // Intentionally looking up the overridden NH
        firstNonNull(overrideNextHopIp, dstIp),
        currentNodeName,
        fib,
        (flowTracer, fibForward) -> {
          flowTracer.forwardOutInterface(
              _currentConfig.getAllInterfaces().get(fibForward.getInterfaceName()),
              fibForward.getArpIp(),
              overrideNextHopIp);
        },
        new Stack<>());
  }

  /**
   * Perform a FIB lookup of {@code dstIp} on {@code fib} of {@code currentNodeName} and take
   * corresponding actions. Use {@code forwardOutInterfaceHandler} to handle forwarding action.
   */
  @VisibleForTesting
  void fibLookup(
      Ip dstIp,
      String currentNodeName,
      Fib fib,
      BiConsumer<FlowTracer, FibForward> forwardOutInterfaceHandler) {
    fibLookup(dstIp, currentNodeName, fib, forwardOutInterfaceHandler, new Stack<>());
  }

  /**
   * Perform a FIB lookup of {@code dstIp} on {@code fib} of {@code currentNodeName} and take
   * corresponding actions given {@code intraHopBreadcrumbs} already produced at this node. Use
   * {@code forwardOutInterfaceHandler} to handle forwarding action.
   */
  @VisibleForTesting
  void fibLookup(
      Ip dstIp,
      String currentNodeName,
      Fib fib,
      BiConsumer<FlowTracer, FibForward> forwardOutInterfaceHandler,
      Stack<Breadcrumb> intraHopBreadcrumbs) {
    // Loop detection
    Breadcrumb breadcrumb =
        new Breadcrumb(currentNodeName, _vrfName, _ingressInterface, _currentFlow);
    if (_breadcrumbs.contains(breadcrumb)) {
      buildLoopTrace(breadcrumb);
      return;
    }
    if (intraHopBreadcrumbs.isEmpty()) {
      _breadcrumbs.push(breadcrumb);
    }
    try {
      Set<FibEntry> fibEntries = fib.get(dstIp);

      if (fibEntries.isEmpty()) {
        buildNoRouteTrace();
        return;
      }

      // Group traces by action (we do not want extra branching if there is branching
      // in FIB resolution)
      SortedMap<FibAction, Set<FibEntry>> groupedByFibAction =
          // Sort so that resulting traces will be in sensible deterministic order
          ImmutableSortedMap.copyOf(
              fibEntries.stream()
                  .collect(Collectors.groupingBy(FibEntry::getAction, Collectors.toSet())),
              FibActionComparator.INSTANCE);

      // For every action corresponding to ECMP LPM FibEntry
      groupedByFibAction.forEach(
          ((fibAction, fibEntriesForFibAction) -> {
            forkTracerSameNode()
                .forward(
                    fibAction,
                    fibEntriesForFibAction,
                    dstIp,
                    currentNodeName,
                    forwardOutInterfaceHandler,
                    intraHopBreadcrumbs,
                    breadcrumb);
          }));
    } finally {
      if (intraHopBreadcrumbs.isEmpty()) {
        _breadcrumbs.pop();
      }
    }
  }

  @Nonnull
  private OriginateStep buildOriginateStep() {
    return OriginateStep.builder()
        .setDetail(OriginateStepDetail.builder().setOriginatingVrf(_vrfName).build())
        .setAction(StepAction.ORIGINATED)
        .build();
  }

  /**
   * Check this {@param flow} matches a session on this device. If so, process the flow. Returns
   * true if the flow is matched/processed.
   */
  private boolean processSessions() {
    String inputIfaceName = _ingressInterface;
    String currentNodeName = _currentNode.getName();

    Collection<FirewallSessionTraceInfo> sessions =
        _ingressInterface != null
            ? _tracerouteContext.getSessionsForIncomingInterface(currentNodeName, inputIfaceName)
            // Flow originated here; check for sessions to match flows originating in current VRF
            : _tracerouteContext.getSessionsForOriginatingVrf(currentNodeName, _vrfName);

    if (sessions.isEmpty()) {
      return false;
    }

    // session match expr cannot use MatchSrcInterface or ACL/IpSpace references.
    Evaluator aclEval = new Evaluator(_currentFlow, null, ImmutableMap.of(), ImmutableMap.of());
    List<FirewallSessionTraceInfo> matchingSessions =
        sessions.stream()
            .filter(session -> aclEval.visit(session.getSessionFlows()))
            .collect(Collectors.toList());
    checkState(matchingSessions.size() < 2, "Flow cannot match more than 1 session");
    if (matchingSessions.isEmpty()) {
      return false;
    }
    FirewallSessionTraceInfo session = matchingSessions.get(0);

    MatchSessionStepDetail.Builder matchDetail =
        MatchSessionStepDetail.builder()
            .setSessionScope(session.getSessionScope())
            .setSessionAction(session.getAction())
            .setMatchCriteria(session.getMatchCriteria());

    Configuration config = _tracerouteContext.getConfigurations().get(currentNodeName);
    Map<String, IpAccessList> ipAccessLists = config.getIpAccessLists();
    Map<String, IpSpace> ipSpaces = config.getIpSpaces();

    // compute transformation. it will be applied after applying incoming ACL
    Transformation transformation = session.getTransformation();
    TransformationResult transformationResult = null;
    if (transformation != null) {
      transformationResult =
          TransformationEvaluator.eval(
              transformation, _currentFlow, inputIfaceName, ipAccessLists, ipSpaces);
      matchDetail.setTransformation(flowDiffs(_currentFlow, transformationResult.getOutputFlow()));
    }

    _steps.add(new MatchSessionStep(matchDetail.build()));

    // apply incoming ACL if any
    if (inputIfaceName != null) {
      FirewallSessionInterfaceInfo incomingIfaceSessionInfo =
          config.getAllInterfaces().get(inputIfaceName).getFirewallSessionInterfaceInfo();
      checkState(
          incomingIfaceSessionInfo != null,
          "Session matched, but interface %s does not have FirewallSessionInterfaceInfo.",
          inputIfaceName);
      String incomingAclName = incomingIfaceSessionInfo.getIncomingAclName();
      if (incomingAclName != null
          && applyFilter(ipAccessLists.get(incomingAclName), FilterType.INGRESS_FILTER) == DENIED) {
        return true;
      }
    }

    // apply transformation
    Flow originalFlow = _currentFlow;
    if (transformationResult != null) {
      _steps.addAll(transformationResult.getTraceSteps());
      _currentFlow = transformationResult.getOutputFlow();
    }

    session
        .getAction()
        .accept(
            new SessionActionVisitor<Void>() {
              @Override
              public Void visitAcceptVrf(Accept acceptVrf) {
                buildAcceptTrace();
                return null;
              }

              @Override
              public Void visitFibLookup(org.batfish.datamodel.flow.FibLookup fibLookup) {
                fibLookup(
                    _currentFlow.getDstIp(),
                    currentNodeName,
                    _tracerouteContext.getFib(currentNodeName, _vrfName).get(),
                    (flowTracer, fibForward) -> {
                      String outgoingIfaceName = fibForward.getInterfaceName();

                      // TODO: handle ACLs

                      // add ExitOutputIfaceStep
                      flowTracer._steps.add(buildExitOutputIfaceStep(outgoingIfaceName));

                      SortedSet<NodeInterfacePair> neighborIfaces =
                          _tracerouteContext.getInterfaceNeighbors(
                              currentNodeName, outgoingIfaceName);
                      if (neighborIfaces.isEmpty()) {
                        FlowDisposition disposition =
                            _tracerouteContext.computeDisposition(
                                currentNodeName, outgoingIfaceName, _currentFlow.getDstIp());
                        flowTracer.buildArpFailureTrace(
                            outgoingIfaceName, _currentFlow.getDstIp(), disposition);
                      } else {
                        flowTracer.processOutgoingInterfaceEdges(
                            outgoingIfaceName, fibForward.getArpIp(), neighborIfaces);
                      }
                    });
                return null;
              }

              @Override
              public Void visitForwardOutInterface(ForwardOutInterface forwardOutInterface) {
                // cycle detection
                Breadcrumb breadcrumb =
                    new Breadcrumb(currentNodeName, _vrfName, _ingressInterface, originalFlow);
                if (_breadcrumbs.contains(breadcrumb)) {
                  buildLoopTrace(breadcrumb);
                  return null;
                }
                _breadcrumbs.push(breadcrumb);
                try {
                  NodeInterfacePair nextHop = forwardOutInterface.getNextHop();
                  String outgoingInterfaceName = forwardOutInterface.getOutgoingInterface();
                  Interface outgoingInterface =
                      config.getAllInterfaces().get(outgoingInterfaceName);

                  // apply outgoing ACL from firewall info, if any
                  StepAction filterResult =
                      Optional.ofNullable(outgoingInterface.getFirewallSessionInterfaceInfo())
                          .map(FirewallSessionInterfaceInfo::getOutgoingAclName)
                          .map(
                              outgoingAclName ->
                                  applyFilter(
                                      ipAccessLists.get(outgoingAclName), FilterType.EGRESS_FILTER))
                          .orElse(null);
                  if (filterResult == DENIED) {
                    return null;
                  }

                  // add ExitOutIfaceStep
                  _steps.add(buildExitOutputIfaceStep(outgoingInterfaceName));

                  if (nextHop == null) {
                    /* ARP error. Currently we can't use buildArpFailureTrace for sessions, because forwarding
                     * analysis disposition maps currently include routing conditions, which do not apply to
                     * sessions.
                     *
                     * ARP failure is only possible for sessions that have an outgoing interface but no next
                     * hop. This only happens when the user started the trace entering the outgoing interface.
                     * For now, just always call this EXITS_NETWORK. In the future we may want to apply the
                     * normal ARP error disposition logic, which would require factoring out routing-independent
                     * disposition maps in forwarding analysis.
                     */
                    buildArpFailureTrace(
                        outgoingInterfaceName,
                        originalFlow.getDstIp(),
                        FlowDisposition.EXITS_NETWORK);
                    return null;
                  }

                  Hop hop = new Hop(new Node(currentNodeName), _steps);
                  _hops.add(
                      forwardedHop(
                          hop,
                          _originalFlow,
                          checkNotNull(
                              getVisitedBreadcrumb(),
                              "Must push a breadcrumb before forwarding to next hop"),
                          getHopSessionInfo()));
                  if (_traceRecorder.tryRecordPartialTrace(_hops)) {
                    return null;
                  }

                  // Forward to neighbor.
                  forkTracerFollowEdge(
                          NodeInterfacePair.of(currentNodeName, outgoingInterfaceName), nextHop)
                      .processHop();
                  return null;
                } finally {
                  _breadcrumbs.pop();
                }
              }
            });
    return true;
  }

  private void buildNullRoutedTrace() {
    checkState(
        Iterables.getLast(_steps) instanceof RoutingStep,
        "RoutingStep should be the last step while creating a null routed trace");
    checkState(
        Iterables.getLast(_steps).getAction() == NULL_ROUTED,
        "The last routing step should should have the action as NULL_ROUTED");
    _hops.add(
        failureHop(
            new Hop(_currentNode, _steps),
            _originalFlow,
            FlowDisposition.NULL_ROUTED,
            getHopSessionInfo(),
            getVisitedBreadcrumb()));
    _traceRecorder.recordTrace(_hops);
  }

  /** add a step for NO_ROUTE from source to output interface */
  private void buildNoRouteTrace() {
    Builder routingStepBuilder = RoutingStep.builder();
    routingStepBuilder
        .setDetail(RoutingStepDetail.builder().build())
        .setAction(StepAction.NO_ROUTE);
    _steps.add(routingStepBuilder.build());
    _hops.add(
        failureHop(
            new Hop(_currentNode, _steps),
            _originalFlow,
            FlowDisposition.NO_ROUTE,
            getHopSessionInfo(),
            getVisitedBreadcrumb()));
    _traceRecorder.recordTrace(_hops);
  }

  /**
   * Perform actions associated with forwarding a packet out an interface: apply outgoing filters,
   * setup sessions if necessary, figure out L3 edge to follow.
   *
   * @param outgoingInterface the interface out of which the packet is being sent
   * @param nextHopIp the next hop IP (a.k.a ARP IP) <emph>as far as the FIB is concerned</emph>
   * @param overriddenNextHopIp not {@code null} if the next hop was overridden outside of the FIB
   *     (e.g., in PBR)
   */
  @VisibleForTesting
  void forwardOutInterface(
      Interface outgoingInterface, Ip nextHopIp, @Nullable Ip overriddenNextHopIp) {
    // Apply preSourceNatOutgoingFilter
    if (applyFilter(
            outgoingInterface.getPreTransformationOutgoingFilter(),
            PRE_TRANSFORMATION_EGRESS_FILTER)
        == DENIED) {
      return;
    }

    applyTransformation(outgoingInterface.getOutgoingTransformation());

    // apply outgoing filter
    if (applyFilter(outgoingInterface.getOutgoingFilter(), EGRESS_FILTER) == DENIED) {
      return;
    }

    // apply outgoing filter matching original flow
    if (applyFilterToOriginalFlow(
            outgoingInterface.getOutgoingOriginalFlowFilter(), EGRESS_ORIGINAL_FLOW_FILTER)
        == DENIED) {
      return;
    }

    // setup session if necessary
    FirewallSessionInterfaceInfo firewallSessionInterfaceInfo =
        outgoingInterface.getFirewallSessionInterfaceInfo();
    if (firewallSessionInterfaceInfo != null) {
      @Nullable
      FirewallSessionTraceInfo session =
          buildFirewallSessionTraceInfo(firewallSessionInterfaceInfo);
      if (session != null) {
        _newSessions.add(session);
        _steps.add(
            new SetupSessionStep(
                SetupSessionStepDetail.builder()
                    .setSessionScope(session.getSessionScope())
                    .setMatchCriteria(session.getMatchCriteria())
                    .setSessionAction(session.getAction())
                    .setTransformation(returnFlowDiffs(_originalFlow, _currentFlow))
                    .build()));
      }
    }

    String currentNodeName = _currentNode.getName();
    String outgoingIfaceName = outgoingInterface.getName();

    // add ExitOutputIfaceStep
    _steps.add(buildExitOutputIfaceStep(outgoingIfaceName));

    SortedSet<NodeInterfacePair> neighborIfaces =
        _tracerouteContext.getInterfaceNeighbors(currentNodeName, outgoingIfaceName);
    /*
    Special handling is necessary if ARP IP was overridden.
    Consider the following: dst IP: 2.2.2.2, NH was overridden to 1.1.1.1 and matched a connected route 1.1.1.0/24, for "iface0"
    Since the FIB entry has no ARP IP (because connected route) we'd normally compute disposition for the dest IP (2.2.2.2)
    However, nothing in forwarding analysis says 2.2.2.2 has a valid disposition for "iface0":
      - there is no route for 2.2.2.2 there,
      - it's not in the subnet of "iface0",
      - nobody will ARP reply for it as far as we know.
    To simplify our life, just compute disposition for 1.1.1.1, at least that's guaranteed to give us a disposition
    */
    if (neighborIfaces.isEmpty()) {
      Ip arpIp = firstNonNull(overriddenNextHopIp, _currentFlow.getDstIp());
      FlowDisposition disposition =
          _tracerouteContext.computeDisposition(currentNodeName, outgoingIfaceName, arpIp);
      buildArpFailureTrace(outgoingIfaceName, arpIp, disposition);
    } else {
      processOutgoingInterfaceEdges(
          outgoingIfaceName, firstNonNull(overriddenNextHopIp, nextHopIp), neighborIfaces);
    }
  }

  /**
   * Creates {@link FirewallSessionTraceInfo} scoped to the {@link
   * FirewallSessionInterfaceInfo#getSessionInterfaces() interfaces} defined in the given {@code
   * firewallSessionInterfaceInfo}
   */
  @Nullable
  private FirewallSessionTraceInfo buildFirewallSessionTraceInfo(
      @Nonnull FirewallSessionInterfaceInfo firewallSessionInterfaceInfo) {
    SessionAction action =
        getSessionAction(
            firewallSessionInterfaceInfo.getFibLookup(),
            _ingressInterface,
            _lastHopNodeAndOutgoingInterface);
    return buildFirewallSessionTraceInfo(
        action, new IncomingSessionScope(firewallSessionInterfaceInfo.getSessionInterfaces()));
  }

  @Nullable
  private FirewallSessionTraceInfo buildFirewallSessionTraceInfo(
      @Nonnull SessionAction sessionAction, @Nonnull SessionScope sessionScope) {
    return buildFirewallSessionTraceInfo(
        _currentNode.getName(), _currentFlow, _originalFlow, sessionAction, sessionScope);
  }

  @VisibleForTesting
  @Nullable
  static FirewallSessionTraceInfo buildFirewallSessionTraceInfo(
      String currentNode,
      Flow currentFlow,
      Flow originalFlow,
      @Nonnull SessionAction sessionAction,
      @Nonnull SessionScope sessionScope) {
    IpProtocol ipProtocol = currentFlow.getIpProtocol();
    if (!IpProtocol.IP_PROTOCOLS_WITH_SESSIONS.contains(ipProtocol)) {
      // TODO verify only protocols with ports can have sessions
      return null;
    }
    return new FirewallSessionTraceInfo(
        currentNode,
        sessionAction,
        sessionScope,
        matchSessionReturnFlow(currentFlow),
        sessionTransformation(originalFlow, currentFlow));
  }

  @VisibleForTesting
  @Nonnull
  static SessionAction getSessionAction(
      boolean fibLookup,
      @Nullable String ingressInterface,
      @Nullable NodeInterfacePair lastHopNodeAndOutgoingInterface) {
    return fibLookup
        ? org.batfish.datamodel.flow.FibLookup.INSTANCE
        : ingressInterface != null
            ? new ForwardOutInterface(ingressInterface, lastHopNodeAndOutgoingInterface)
            : Accept.INSTANCE;
  }

  @VisibleForTesting
  static SessionMatchExpr matchSessionReturnFlow(Flow forwardFlow) {
    IpProtocol ipProtocol = forwardFlow.getIpProtocol();
    checkArgument(
        IpProtocol.IP_PROTOCOLS_WITH_SESSIONS.contains(ipProtocol),
        "cannot match session return flow with IP protocol %s",
        ipProtocol);
    return new SessionMatchExpr(
        ipProtocol,
        forwardFlow.getDstIp(),
        forwardFlow.getSrcIp(),
        forwardFlow.getDstPort(),
        forwardFlow.getSrcPort());
  }

  @VisibleForTesting
  void buildAcceptTrace() {
    // Choose accepting interface based on dst IP
    String acceptingInterface =
        _tracerouteContext
            .interfaceAcceptingIp(_currentNode.getName(), _vrfName, _currentFlow.getDstIp())
            .orElseGet(
                // If no interface in VRF owns dst IP, choose arbitrary accepting interface.
                // Currently we will only resort to this if a session is matched.
                () -> _currentConfig.getActiveInterfaces(_vrfName).keySet().iterator().next());

    FirewallSessionVrfInfo firewallSessionVrfInfo =
        _currentConfig.getVrfs().get(_vrfName).getFirewallSessionVrfInfo();
    if (_ingressInterface != null && firewallSessionVrfInfo != null) {
      // Set up a session that will match return traffic originating from this VRF.
      // TODO Ensure this behavior is valid for all vendors.
      //  - Is FibLookup the right action for all vendors?
      //  - Do any vendors set up sessions for intranode traffic? AWS should not. If others do, then
      //    for those cases we would need to set up a session even if ingressInterface is null.
      SessionAction action =
          getSessionAction(
              firewallSessionVrfInfo.getFibLookup(),
              _ingressInterface,
              _lastHopNodeAndOutgoingInterface);
      @Nullable
      FirewallSessionTraceInfo session =
          buildFirewallSessionTraceInfo(action, new OriginatingSessionScope(_vrfName));
      if (session != null) {
        _newSessions.add(session);
        _steps.add(
            new SetupSessionStep(
                SetupSessionStepDetail.builder()
                    .setSessionScope(session.getSessionScope())
                    .setMatchCriteria(session.getMatchCriteria())
                    .setSessionAction(session.getAction())
                    .setTransformation(returnFlowDiffs(_originalFlow, _currentFlow))
                    .build()));
      }
    }

    InboundStep inboundStep =
        InboundStep.builder().setDetail(new InboundStepDetail(acceptingInterface)).build();
    _steps.add(inboundStep);
    Flow returnFlow = returnFlow(_currentFlow, _currentNode.getName(), _vrfName, null);
    _hops.add(
        successHop(
            new Hop(_currentNode, _steps),
            _originalFlow,
            FlowDisposition.ACCEPTED,
            returnFlow,
            getHopSessionInfo(),
            getVisitedBreadcrumb()));
    _traceRecorder.recordTrace(_hops);
  }

  private void buildLoopTrace(Breadcrumb loopDetectedBreadcrumb) {
    _steps.add(LoopStep.INSTANCE);
    _hops.add(
        loopHop(
            new Hop(_currentNode, _steps),
            _originalFlow,
            loopDetectedBreadcrumb,
            getHopSessionInfo()));
    _traceRecorder.recordTrace(_hops);
  }

  /**
   * Apply a filter to the current flow, and create the corresponding step. If the filter DENIED the
   * flow, then create a trace ending in the denial. Return the action if the filter is non-null. If
   * the filter is null, return null.
   */
  private @Nullable StepAction applyFilter(@Nullable IpAccessList filter, FilterType filterType) {
    return applyFilter(filter, filterType, _currentFlow);
  }

  /**
   * Apply a filter to the original flow, and create the corresponding step. If the filter DENIED
   * the flow, then create a trace ending in the denial. Return the action if the filter is
   * non-null. If the filter is null, return null.
   */
  private @Nullable StepAction applyFilterToOriginalFlow(
      @Nullable IpAccessList filter, FilterType filterType) {
    return applyFilter(filter, filterType, _originalFlow);
  }

  private @Nullable StepAction applyFilter(
      @Nullable IpAccessList filter, FilterType filterType, @Nonnull Flow flow) {
    if (filter == null) {
      return null;
    }
    FilterStep filterStep =
        createFilterStep(
            flow,
            _ingressInterface,
            filter,
            filterType,
            _currentConfig.getIpAccessLists(),
            _currentConfig.getIpSpaces(),
            _tracerouteContext.getIgnoreFilters());
    _steps.add(filterStep);
    if (filterStep.getAction() == DENIED) {
      buildDeniedTrace(filterType.deniedDisposition());
    }
    return filterStep.getAction();
  }

  @VisibleForTesting
  void buildDeniedTrace(FlowDisposition disposition) {
    _hops.add(
        failureHop(
            new Hop(_currentNode, _steps),
            _originalFlow,
            disposition,
            getHopSessionInfo(),
            getVisitedBreadcrumb()));
    _traceRecorder.recordTrace(_hops);
  }

  /**
   * Build ARP failure trace for the current flow, for when its forwarded out the input
   * outgoingInterface and end up with a Exit Network, Delivered To Subnet, Insufficient Info or
   * Neighbor Unreachable disposition.
   */
  private void buildArpFailureTrace(
      String outInterface, Ip resolvedNhIp, FlowDisposition disposition) {
    String currentNodeName = _currentNode.getName();

    _steps.add(buildArpFailureStep(outInterface, resolvedNhIp, disposition));

    Hop hop = new Hop(_currentNode, _steps);
    if (disposition.isSuccessful()) {
      Flow returnFlow = returnFlow(_currentFlow, currentNodeName, null, outInterface);
      _hops.add(
          successHop(
              hop,
              _originalFlow,
              disposition,
              returnFlow,
              getHopSessionInfo(),
              getVisitedBreadcrumb()));
    } else {
      _hops.add(
          failureHop(hop, _originalFlow, disposition, getHopSessionInfo(), getVisitedBreadcrumb()));
    }
    _traceRecorder.recordTrace(_hops);
  }

  @VisibleForTesting
  static RoutingStep buildRoutingStep(FibAction fibAction, Set<FibEntry> fibEntries) {
    RoutingStep.Builder routingStepBuilder = RoutingStep.builder();
    RoutingStepDetail.Builder routingStepDetailBuilder =
        RoutingStepDetail.builder().setRoutes(fibEntriesToRouteInfos(fibEntries));
    fibAction.accept(
        new FibActionVisitor<Void>() {
          @Override
          public Void visitFibForward(FibForward fibForward) {
            routingStepDetailBuilder.setArpIp(fibForward.getArpIp());
            routingStepDetailBuilder.setOutputInterface(fibForward.getInterfaceName());
            routingStepBuilder.setAction(FORWARDED);
            return null;
          }

          @Override
          public Void visitFibNextVrf(FibNextVrf fibNextVrf) {
            routingStepBuilder.setAction(FORWARDED_TO_NEXT_VRF);
            return null;
          }

          @Override
          public Void visitFibNullRoute(FibNullRoute fibNullRoute) {
            routingStepBuilder.setAction(NULL_ROUTED);
            return null;
          }
        });
    return routingStepBuilder.setDetail(routingStepDetailBuilder.build()).build();
  }

  private void forward(
      FibAction fibAction,
      Set<FibEntry> fibEntries,
      Ip dstIp,
      String currentNodeName,
      BiConsumer<FlowTracer, FibForward> forwardOutInterfaceHandler,
      Stack<Breadcrumb> intraHopBreadcrumbs,
      Breadcrumb breadcrumb) {
    FlowTracer flowTracer = this;
    _steps.add(buildRoutingStep(fibAction, fibEntries));
    fibAction.accept(
        new FibActionVisitor<Void>() {
          @Override
          public Void visitFibForward(FibForward fibForward) {
            forwardOutInterfaceHandler.accept(flowTracer, fibForward);
            return null;
          }

          @Override
          public Void visitFibNextVrf(FibNextVrf fibNextVrf) {
            if (intraHopBreadcrumbs.contains(breadcrumb)) {
              buildLoopTrace(breadcrumb);
              return null;
            }
            intraHopBreadcrumbs.push(breadcrumb);
            String nextVrf = fibNextVrf.getNextVrf();
            forkTracerSameNode(nextVrf)
                .fibLookup(
                    dstIp,
                    currentNodeName,
                    _tracerouteContext.getFib(currentNodeName, nextVrf).get(),
                    forwardOutInterfaceHandler,
                    intraHopBreadcrumbs);
            intraHopBreadcrumbs.pop();
            return null;
          }

          @Override
          public Void visitFibNullRoute(FibNullRoute fibNullRoute) {
            buildNullRoutedTrace();
            return null;
          }
        });
  }

  @VisibleForTesting
  Step<?> buildArpFailureStep(String outInterface, Ip resolvedNhIp, FlowDisposition disposition) {
    switch (disposition) {
      case INSUFFICIENT_INFO:
      case NEIGHBOR_UNREACHABLE:
        return buildArpErrorStep(
            outInterface, resolvedNhIp, getFinalActionForDisposition(disposition));
      case DELIVERED_TO_SUBNET:
      case EXITS_NETWORK:
        return buildDeliveredStep(
            outInterface, resolvedNhIp, getFinalActionForDisposition(disposition));
      default:
        throw new BatfishException(
            "the disposition is must be insufficient info, neighbor unreachable, delivered to subnet or exits network.");
    }
  }

  @VisibleForTesting
  Flow getCurrentFlow() {
    return _currentFlow;
  }

  @VisibleForTesting
  Flow getOriginalFlow() {
    return _originalFlow;
  }
}
