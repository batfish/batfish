package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match5Tuple;
import static org.batfish.datamodel.flow.FilterStep.FilterType.EGRESS_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.INGRESS_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.POST_TRANSFORMATION_INGRESS_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.PRE_TRANSFORMATION_EGRESS_FILTER;
import static org.batfish.datamodel.flow.StepAction.DENIED;
import static org.batfish.datamodel.flow.StepAction.FORWARDED;
import static org.batfish.datamodel.flow.StepAction.PERMITTED;
import static org.batfish.datamodel.flow.StepAction.TRANSMITTED;
import static org.batfish.dataplane.traceroute.TracerouteUtils.buildEnterSrcIfaceStep;
import static org.batfish.dataplane.traceroute.TracerouteUtils.createFilterStep;
import static org.batfish.dataplane.traceroute.TracerouteUtils.getFinalActionForDisposition;
import static org.batfish.dataplane.traceroute.TracerouteUtils.returnFlow;
import static org.batfish.dataplane.traceroute.TracerouteUtils.sessionTransformation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibAction;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.acl.Evaluator;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;
import org.batfish.datamodel.flow.FilterStep;
import org.batfish.datamodel.flow.FilterStep.FilterStepDetail;
import org.batfish.datamodel.flow.FilterStep.FilterType;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.InboundStep;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;
import org.batfish.datamodel.flow.MatchSessionStep;
import org.batfish.datamodel.flow.OriginateStep;
import org.batfish.datamodel.flow.OriginateStep.OriginateStepDetail;
import org.batfish.datamodel.flow.RouteInfo;
import org.batfish.datamodel.flow.RoutingStep;
import org.batfish.datamodel.flow.RoutingStep.Builder;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.batfish.datamodel.flow.SetupSessionStep;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.packet_policy.ActionVisitor;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.FlowEvaluator;
import org.batfish.datamodel.packet_policy.FlowEvaluator.FlowResult;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationEvaluator;
import org.batfish.datamodel.transformation.TransformationEvaluator.TransformationResult;
import org.batfish.datamodel.visitors.FibActionVisitor;

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
  private final Map<String, IpAccessList> _aclDefinitions;
  private final Node _currentNode;
  private final Consumer<TraceAndReverseFlow> _flowTraces;
  private final NodeInterfacePair _lastHopNodeAndOutgoingInterface;
  private final Set<FirewallSessionTraceInfo> _newSessions;
  private final NavigableMap<String, IpSpace> _namedIpSpaces;
  private final Flow _originalFlow;
  private final String _vrfName;

  // Mutable list of hops in the current trace
  private final List<Hop> _hops;

  // Mutable list of steps in the current hop
  private final List<Step<?>> _steps;

  private final Stack<Breadcrumb> _breadcrumbs;

  // The current flow can change as we process the packet.
  private Flow _currentFlow;

  @Nonnull
  static FlowTracer initialFlowTracer(
      TracerouteEngineImplContext tracerouteContext,
      String node,
      @Nullable String ingressInterface,
      Flow originalFlow,
      Consumer<TraceAndReverseFlow> flowTraces) {
    Configuration currentConfig = tracerouteContext.getConfigurations().get(node);
    return new FlowTracer(
        tracerouteContext,
        currentConfig,
        ingressInterface,
        currentConfig.getIpAccessLists(),
        new Node(node),
        flowTraces,
        null,
        new HashSet<>(),
        currentConfig.getIpSpaces(),
        originalFlow,
        initVrfName(ingressInterface, currentConfig, originalFlow),
        new ArrayList<>(),
        new ArrayList<>(),
        new Stack<>(),
        originalFlow);
  }

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
        newConfig.getIpAccessLists(),
        new Node(newConfig.getHostname()),
        _flowTraces,
        lastHopNodeAndOutgoingInterface,
        new HashSet<>(_newSessions),
        _namedIpSpaces,
        _originalFlow,
        newVrfName,
        new ArrayList<>(_hops),
        new ArrayList<>(initialSteps),
        _breadcrumbs,
        _currentFlow);
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

  @VisibleForTesting
  FlowTracer(
      TracerouteEngineImplContext tracerouteContext,
      Configuration currentConfig,
      @Nullable String ingressInterface,
      Map<String, IpAccessList> aclDefinitions,
      Node currentNode,
      Consumer<TraceAndReverseFlow> flowTraces,
      NodeInterfacePair lastHopNodeAndOutgoingInterface,
      Set<FirewallSessionTraceInfo> newSessions,
      NavigableMap<String, IpSpace> namedIpSpaces,
      Flow originalFlow,
      String vrfName,
      List<Hop> hops,
      List<Step<?>> steps,
      Stack<Breadcrumb> breadcrumbs,
      Flow currentFlow) {
    _tracerouteContext = tracerouteContext;
    _currentConfig = currentConfig;
    _ingressInterface = ingressInterface;
    _aclDefinitions = aclDefinitions;
    _currentNode = currentNode;
    _flowTraces = flowTraces;
    _lastHopNodeAndOutgoingInterface = lastHopNodeAndOutgoingInterface;
    _newSessions = newSessions;
    _namedIpSpaces = namedIpSpaces;
    _originalFlow = originalFlow;
    _vrfName = vrfName;
    _hops = hops;
    _steps = steps;
    _breadcrumbs = breadcrumbs;
    _currentFlow = currentFlow;
  }

  /** Creates a new TransmissionContext for the specified last-hop node and outgoing interface. */
  private FlowTracer followEdge(NodeInterfacePair exitIface, NodeInterfacePair enterIface) {
    /*
     * At this point there should be 1 breadcrumb for every lookup in a VRF (at least one per
     * visited node); and one hop for every node visited beyond the first, plus one for the node we
     * are about to visit. So we should expect there to be more at least as many breadcrumbs as
     * hops.
     */
    checkState(_hops.size() <= _breadcrumbs.size(), "Must not have more hops than breadcrumbs");
    // grab configuration-specific information from the node that owns enterIface
    String newHostname = enterIface.getHostname();
    Configuration newConfig = _tracerouteContext.getConfigurations().get(newHostname);
    checkArgument(
        newConfig != null, "Node %s is not in the network, cannot perform traceroute", newHostname);
    String newIngressInterface = enterIface.getInterface();
    return forkTracer(
        newConfig,
        newIngressInterface,
        ImmutableList.of(),
        exitIface,
        initVrfName(newIngressInterface, newConfig, _currentFlow));
  }

  private @Nonnull FlowTracer branch() {
    return branch(_vrfName);
  }

  private @Nonnull FlowTracer branch(String newVrfName) {
    /*
     * At this point there should be 1 breadcrumb for every lookup in a VRF (at least one per
     * visited node), and one hop for every node visited beyond the first. So we should expect there
     * to be more breadcrumbs than hops.
     */
    checkState(_hops.size() < _breadcrumbs.size(), "Must have fewer hops than breadcrumbs");
    return forkTracer(
        _currentConfig, _ingressInterface, _steps, _lastHopNodeAndOutgoingInterface, newVrfName);
  }

  private void processOutgoingInterfaceEdges(
      String outgoingInterface, Ip nextHopIp, SortedSet<NodeInterfacePair> neighborIfaces) {
    checkArgument(!neighborIfaces.isEmpty(), "No neighbor interfaces.");
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
      buildArpFailureTrace(outgoingInterface);
      return;
    }

    _steps.add(buildExitOutputIfaceStep(outgoingInterface, TRANSMITTED));
    Hop hop = new Hop(_currentNode, _steps);
    _hops.add(hop);

    NodeInterfacePair exitIface = new NodeInterfacePair(_currentNode.getName(), outgoingInterface);
    interfacesThatReplyToArp.forEach(enterIface -> followEdge(exitIface, enterIface).processHop());
  }

  @Nonnull
  private ExitOutputIfaceStep buildExitOutputIfaceStep(String outputIface, StepAction action) {
    return ExitOutputIfaceStep.builder()
        .setDetail(
            ExitOutputIfaceStepDetail.builder()
                .setOutputInterface(new NodeInterfacePair(_currentNode.getName(), outputIface))
                .setTransformedFlow(TracerouteUtils.hopFlow(_originalFlow, _currentFlow))
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

      TransformationResult transformationResult =
          TransformationEvaluator.eval(
              incomingInterface.getIncomingTransformation(),
              _currentFlow,
              _ingressInterface,
              _aclDefinitions,
              _namedIpSpaces);
      _steps.addAll(transformationResult.getTraceSteps());
      _currentFlow = transformationResult.getOutputFlow();

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
    if (_tracerouteContext.ownsIp(currentNodeName, _vrfName, dstIp)) {
      buildAcceptTrace();
      return;
    }

    Fib fib = _tracerouteContext.getFib(currentNodeName, _vrfName).get();
    fibLookup(dstIp, currentNodeName, fib);
  }

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

    Configuration owner = incomingInterface.getOwner();
    FlowResult result =
        FlowEvaluator.evaluate(
            _currentFlow,
            incomingInterface.getName(),
            policy,
            owner.getIpAccessLists(),
            owner.getIpSpaces());
    return new ActionVisitor<Boolean>() {

      @Override
      public Boolean visitDrop(Drop drop) {
        _steps.add(new FilterStep(new FilterStepDetail(policy.getName(), INGRESS_FILTER), DENIED));
        return true;
      }

      @Override
      public Boolean visitFibLookup(FibLookup fibLookup) {
        _steps.add(
            new FilterStep(new FilterStepDetail(policy.getName(), INGRESS_FILTER), PERMITTED));
        String lookupVrfName = fibLookup.getVrfName();
        Ip dstIp = result.getFinalFlow().getDstIp();

        // Accept if the flow is destined for this vrf on this host.
        String currentNodeName = _currentNode.getName();
        if (_tracerouteContext.ownsIp(currentNodeName, _vrfName, dstIp)) {
          buildAcceptTrace();
          return true;
        }

        Fib fib = _tracerouteContext.getFib(currentNodeName, lookupVrfName).get();
        fibLookup(dstIp, currentNodeName, fib);
        return true;
      }
    }.visit(result.getAction());
  }

  @VisibleForTesting
  void fibLookup(Ip dstIp, String currentNodeName, Fib fib) {
    // Loop detection
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, _vrfName, _currentFlow);
    if (_breadcrumbs.contains(breadcrumb)) {
      buildLoopTrace();
      return;
    }
    _breadcrumbs.push(breadcrumb);
    try {
      Set<FibEntry> fibEntries = fib.get(dstIp);

      if (fibEntries.isEmpty()) {
        buildNoRouteTrace();
        return;
      }

      _steps.add(buildRoutingStep(fibEntries));

      // Group traces by action (we do not want extra branching if there is branching
      // in FIB resolution)
      SortedMap<FibAction, Set<FibEntry>> groupedByFibAction =
          // Sort so that resulting traces will be in sensible deterministic order
          ImmutableSortedMap.copyOf(
              fibEntries.stream()
                  .collect(Collectors.groupingBy(FibEntry::getAction, Collectors.toSet())),
              FibActionComparator.INSTANCE);

      // For every action corresponding to ECMP LPM FibEntry
      for (FibAction action : groupedByFibAction.keySet()) {
        action.accept(
            new FibActionVisitor<Void>() {
              @Override
              public Void visitFibForward(FibForward fibForward) {
                branch()
                    .forwardOutInterface(
                        _currentConfig.getAllInterfaces().get(fibForward.getInterfaceName()),
                        fibForward.getArpIp());
                return null;
              }

              @Override
              public Void visitFibNextVrf(FibNextVrf fibNextVrf) {
                String nextVrf = fibNextVrf.getNextVrf();
                branch(fibNextVrf.getNextVrf())
                    .fibLookup(
                        dstIp,
                        currentNodeName,
                        _tracerouteContext.getFib(currentNodeName, nextVrf).get());
                return null;
              }

              @Override
              public Void visitFibNullRoute(FibNullRoute fibNullRoute) {
                branch().buildNullRoutedTrace();
                return null;
              }
            });
      }
    } finally {
      _breadcrumbs.pop();
    }
  }

  @Nonnull
  private OriginateStep buildOriginateStep() {
    return OriginateStep.builder()
        .setDetail(OriginateStepDetail.builder().setOriginatingVrf(_vrfName).build())
        .setAction(StepAction.ORIGINATED)
        .build();
  }

  private RoutingStep buildRoutingStep(Set<FibEntry> fibEntries) {
    List<RouteInfo> matchedRibRouteInfo =
        fibEntries.stream()
            .map(FibEntry::getTopLevelRoute)
            .map(
                route ->
                    new RouteInfo(
                        route.getProtocol(),
                        route.getNetwork(),
                        route.getNextHopIp(),
                        route.getProtocol() == RoutingProtocol.STATIC
                            ? ((StaticRoute) route).getNextVrf()
                            : null))
            .sorted(
                comparing(RouteInfo::getNetwork)
                    .thenComparing(RouteInfo::getNextHopIp)
                    .thenComparing(RouteInfo::getNextVrf, nullsFirst(String::compareTo))
                    .thenComparing(RouteInfo::getProtocol))
            .distinct()
            .collect(ImmutableList.toImmutableList());
    return RoutingStep.builder()
        .setDetail(RoutingStepDetail.builder().setRoutes(matchedRibRouteInfo).build())
        .setAction(FORWARDED)
        .build();
  }

  /**
   * Check this {@param flow} matches a session on this device. If so, process the flow. Returns
   * true if the flow is matched/processed.
   */
  private boolean processSessions() {
    String inputIfaceName = _ingressInterface;
    if (inputIfaceName == null) {
      // Sessions only exist when entering an interface.
      return false;
    }

    String currentNodeName = _currentNode.getName();
    Flow flow = _currentFlow;
    Collection<FirewallSessionTraceInfo> sessions =
        _tracerouteContext.getSessions(currentNodeName, inputIfaceName);
    if (sessions.isEmpty()) {
      return false;
    }

    // session match expr cannot use MatchSrcInterface or ACL/IpSpace references.
    Evaluator aclEval = new Evaluator(flow, null, ImmutableMap.of(), ImmutableMap.of());
    List<FirewallSessionTraceInfo> matchingSessions =
        sessions.stream()
            .filter(session -> aclEval.visit(session.getSessionFlows()))
            .collect(Collectors.toList());
    checkState(matchingSessions.size() < 2, "Flow cannot match more than 1 session");
    if (matchingSessions.isEmpty()) {
      return false;
    }
    FirewallSessionTraceInfo session = matchingSessions.get(0);

    _steps.add(new MatchSessionStep());

    Configuration config = _tracerouteContext.getConfigurations().get(currentNodeName);
    Map<String, IpAccessList> ipAccessLists = config.getIpAccessLists();
    Map<String, IpSpace> ipSpaces = config.getIpSpaces();
    Interface incomingInterface = config.getAllInterfaces().get(inputIfaceName);
    checkState(
        incomingInterface.getFirewallSessionInterfaceInfo() != null,
        "Cannot have a session entering an interface without FirewallSessionInterfaceInfo");

    // apply imcoming ACL
    String incomingAclName =
        incomingInterface.getFirewallSessionInterfaceInfo().getIncomingAclName();
    if (incomingAclName != null
        && applyFilter(ipAccessLists.get(incomingAclName), FilterType.INGRESS_FILTER) == DENIED) {
      return true;
    }

    // cycle detection
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, _vrfName, flow);
    if (_breadcrumbs.contains(breadcrumb)) {
      buildLoopTrace();
      return true;
    }

    _breadcrumbs.push(breadcrumb);
    try {
      // apply transformation
      Transformation transformation = session.getTransformation();
      if (transformation != null) {
        TransformationResult result =
            TransformationEvaluator.eval(
                transformation, flow, inputIfaceName, ipAccessLists, ipSpaces);
        _steps.addAll(result.getTraceSteps());
        _currentFlow = result.getOutputFlow();
      }
      if (session.getOutgoingInterface() == null) {
        // Accepted by this node (vrf of incoming interface).
        buildAcceptTrace();
        return true;
      }

      Interface outgoingInterface = config.getAllInterfaces().get(session.getOutgoingInterface());
      checkState(
          outgoingInterface.getFirewallSessionInterfaceInfo() != null,
          "Cannot have a session exiting an interface without FirewallSessionInterfaceInfo");
      // apply outgoing ACL
      String outgoingAclName =
          outgoingInterface.getFirewallSessionInterfaceInfo().getOutgoingAclName();
      if (outgoingAclName != null
          && applyFilter(ipAccessLists.get(outgoingAclName), FilterType.EGRESS_FILTER) == DENIED) {
        return true;
      }

      if (session.getNextHop() == null) {
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
        buildArpFailureTrace(session.getOutgoingInterface(), FlowDisposition.EXITS_NETWORK);
        return true;
      }

      _steps.add(buildExitOutputIfaceStep(outgoingInterface.getName(), TRANSMITTED));
      _hops.add(new Hop(new Node(currentNodeName), _steps));

      // Forward to neighbor.
      followEdge(
              new NodeInterfacePair(currentNodeName, session.getOutgoingInterface()),
              session.getNextHop())
          .processHop();
      return true;
    } finally {
      _breadcrumbs.pop();
    }
  }

  private void buildNullRoutedTrace() {
    _steps.add(
        ExitOutputIfaceStep.builder()
            .setDetail(
                ExitOutputIfaceStepDetail.builder()
                    .setOutputInterface(
                        new NodeInterfacePair(
                            _currentNode.getName(), Interface.NULL_INTERFACE_NAME))
                    .build())
            .setAction(StepAction.NULL_ROUTED)
            .build());
    _hops.add(new Hop(_currentNode, _steps));
    Trace trace = new Trace(FlowDisposition.NULL_ROUTED, _hops);
    _flowTraces.accept(new TraceAndReverseFlow(trace, null, _newSessions));
  }

  /** add a step for NO_ROUTE from source to output interface */
  private void buildNoRouteTrace() {
    Builder routingStepBuilder = RoutingStep.builder();
    routingStepBuilder
        .setDetail(RoutingStepDetail.builder().build())
        .setAction(StepAction.NO_ROUTE);
    _steps.add(routingStepBuilder.build());
    _hops.add(new Hop(_currentNode, _steps));
    Trace trace = new Trace(FlowDisposition.NO_ROUTE, _hops);
    _flowTraces.accept(new TraceAndReverseFlow(trace, null, _newSessions));
  }

  private void forwardOutInterface(Interface outgoingInterface, Ip nextHopIp) {
    // Apply preSourceNatOutgoingFilter
    if (applyFilter(
            outgoingInterface.getPreTransformationOutgoingFilter(),
            PRE_TRANSFORMATION_EGRESS_FILTER)
        == DENIED) {
      return;
    }

    // Apply outgoing transformation
    Transformation transformation = outgoingInterface.getOutgoingTransformation();
    TransformationResult transformationResult =
        TransformationEvaluator.eval(
            transformation, _currentFlow, _ingressInterface, _aclDefinitions, _namedIpSpaces);
    _steps.addAll(transformationResult.getTraceSteps());
    _currentFlow = transformationResult.getOutputFlow();

    // apply outgoing filter
    if (applyFilter(outgoingInterface.getOutgoingFilter(), EGRESS_FILTER) == DENIED) {
      return;
    }

    // setup session if necessary
    FirewallSessionInterfaceInfo firewallSessionInterfaceInfo =
        outgoingInterface.getFirewallSessionInterfaceInfo();
    if (firewallSessionInterfaceInfo != null) {
      _newSessions.add(buildFirewallSessionTraceInfo(firewallSessionInterfaceInfo));
      _steps.add(new SetupSessionStep());
    }

    String currentNodeName = _currentNode.getName();
    String outgoingIfaceName = outgoingInterface.getName();
    SortedSet<NodeInterfacePair> neighborIfaces =
        _tracerouteContext.getInterfaceNeighbors(currentNodeName, outgoingIfaceName);
    if (neighborIfaces.isEmpty()) {
      FlowDisposition disposition =
          _tracerouteContext.computeDisposition(
              currentNodeName, outgoingIfaceName, _currentFlow.getDstIp());

      buildArpFailureTrace(outgoingIfaceName, disposition);
    } else {
      processOutgoingInterfaceEdges(outgoingIfaceName, nextHopIp, neighborIfaces);
    }
  }

  @Nonnull
  private FirewallSessionTraceInfo buildFirewallSessionTraceInfo(
      @Nonnull FirewallSessionInterfaceInfo firewallSessionInterfaceInfo) {
    return new FirewallSessionTraceInfo(
        _currentNode.getName(),
        _ingressInterface,
        _lastHopNodeAndOutgoingInterface,
        firewallSessionInterfaceInfo.getSessionInterfaces(),
        match5Tuple(
            _currentFlow.getDstIp(),
            _currentFlow.getDstPort(),
            _currentFlow.getSrcIp(),
            _currentFlow.getSrcPort(),
            _currentFlow.getIpProtocol()),
        sessionTransformation(_originalFlow, _currentFlow));
  }

  private void buildAcceptTrace() {
    InboundStep inboundStep =
        InboundStep.builder()
            .setAction(StepAction.ACCEPTED)
            .setDetail(new InboundStepDetail())
            .build();
    _steps.add(inboundStep);
    _hops.add(new Hop(_currentNode, _steps));
    Trace trace = new Trace(FlowDisposition.ACCEPTED, _hops);
    Flow returnFlow = returnFlow(_currentFlow, _currentNode.getName(), _vrfName, null);
    _flowTraces.accept(new TraceAndReverseFlow(trace, returnFlow, _newSessions));
  }

  private void buildLoopTrace() {
    _hops.add(new Hop(_currentNode, _steps));
    Trace trace = new Trace(FlowDisposition.LOOP, _hops);
    _flowTraces.accept(new TraceAndReverseFlow(trace, null, _newSessions));
  }

  /**
   * Apply a filter, and create the corresponding step. If the filter DENIED the flow, then create a
   * trace ending in the denial. Return the action if the filter is non-null. If the filter is null,
   * return null.
   */
  private @Nullable StepAction applyFilter(@Nullable IpAccessList filter, FilterType filterType) {
    if (filter == null) {
      return null;
    }
    FilterStep filterStep =
        createFilterStep(
            _currentFlow,
            _ingressInterface,
            filter,
            filterType,
            _aclDefinitions,
            _namedIpSpaces,
            _tracerouteContext.getIgnoreFilters());
    _steps.add(filterStep);
    if (filterStep.getAction() == DENIED) {
      buildDeniedTrace(filterType.deniedDisposition());
    }
    return filterStep.getAction();
  }

  @VisibleForTesting
  void buildDeniedTrace(FlowDisposition disposition) {
    _hops.add(new Hop(_currentNode, _steps));
    Trace trace = new Trace(disposition, _hops);
    _flowTraces.accept(new TraceAndReverseFlow(trace, null, _newSessions));
  }

  /**
   * Build ARP failure trace for the current flow, for when its forwarded out the input
   * outgoingInterface.
   */
  private void buildArpFailureTrace(String outgoingInterfaceName) {
    String currentNodeName = _currentNode.getName();
    FlowDisposition disposition =
        _tracerouteContext.computeDisposition(
            currentNodeName, outgoingInterfaceName, _currentFlow.getDstIp());
    buildArpFailureTrace(outgoingInterfaceName, disposition);
  }

  private void buildArpFailureTrace(String outInterface, FlowDisposition disposition) {
    String currentNodeName = _currentNode.getName();
    _steps.add(buildExitOutputIfaceStep(outInterface, getFinalActionForDisposition(disposition)));

    _hops.add(new Hop(_currentNode, _steps));

    Flow returnFlow =
        disposition.isSuccessful()
            ? returnFlow(_currentFlow, currentNodeName, null, outInterface)
            : null;

    Trace trace = new Trace(disposition, _hops);
    _flowTraces.accept(new TraceAndReverseFlow(trace, returnFlow, _newSessions));
  }
}
