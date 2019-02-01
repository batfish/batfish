package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match5Tuple;
import static org.batfish.datamodel.flow.FilterStep.FilterType.EGRESS_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.INGRESS_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.PRE_SOURCE_NAT_FILTER;
import static org.batfish.datamodel.flow.StepAction.DENIED;
import static org.batfish.datamodel.flow.StepAction.TRANSMITTED;
import static org.batfish.dataplane.traceroute.TracerouteUtils.createEnterSrcIfaceStep;
import static org.batfish.dataplane.traceroute.TracerouteUtils.createFilterStep;
import static org.batfish.dataplane.traceroute.TracerouteUtils.getFinalActionForDisposition;
import static org.batfish.dataplane.traceroute.TracerouteUtils.resolveNextHopIpRoutes;
import static org.batfish.dataplane.traceroute.TracerouteUtils.returnFlow;
import static org.batfish.dataplane.traceroute.TracerouteUtils.sessionTransformation;
import static org.batfish.specifier.DispositionSpecifier.SUCCESS_DISPOSITIONS;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.acl.Evaluator;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;
import org.batfish.datamodel.flow.FilterStep;
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
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationEvaluator;
import org.batfish.datamodel.transformation.TransformationEvaluator.TransformationResult;

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

  // Mutable list of hops in the current trace
  private final List<Hop> _hops;

  // Mutable list of steps in the current hop
  private final List<Step<?>> _steps;

  private final Stack<Breadcrumb> _breadcrumbs;

  // The current flow can change as we process the packet.
  private Flow _currentFlow;

  FlowTracer(
      TracerouteEngineImplContext tracerouteContext,
      String node,
      @Nullable String ingressInterface,
      Flow originalFlow,
      Consumer<TraceAndReverseFlow> flowTraces) {
    _tracerouteContext = tracerouteContext;
    _currentConfig = _tracerouteContext.getConfigurations().get(node);
    _currentNode = new Node(node);
    _aclDefinitions = _currentConfig.getIpAccessLists();
    _flowTraces = flowTraces;
    _ingressInterface = ingressInterface;
    _lastHopNodeAndOutgoingInterface = null;
    _newSessions = new HashSet<>();
    _namedIpSpaces = _currentConfig.getIpSpaces();
    _originalFlow = originalFlow;

    _breadcrumbs = new Stack<>();
    _hops = new ArrayList<>();
    _steps = new ArrayList<>();

    _currentFlow = originalFlow;
  }

  private FlowTracer(
      TracerouteEngineImplContext tracerouteContext,
      Node currentNode,
      @Nullable String ingressInterface,
      Stack<Breadcrumb> breadcrumbs,
      List<Hop> hops,
      List<Step<?>> steps,
      NodeInterfacePair lastHopNodeAndOutgoingInterface,
      Set<FirewallSessionTraceInfo> newSessions,
      Flow originalFlow,
      Flow currentFlow,
      Consumer<TraceAndReverseFlow> flowTraces) {
    _tracerouteContext = tracerouteContext;
    _currentNode = currentNode;
    _ingressInterface = ingressInterface;
    _lastHopNodeAndOutgoingInterface = lastHopNodeAndOutgoingInterface;
    _originalFlow = originalFlow;
    _currentFlow = currentFlow;
    _flowTraces = flowTraces;

    Configuration currentConfig = _tracerouteContext.getConfigurations().get(currentNode.getName());
    checkArgument(
        currentConfig != null,
        "Node %s is not in the network, cannot perform traceroute",
        currentNode.getName());

    // essentially just cached values
    _currentConfig = currentConfig;
    _aclDefinitions = _currentConfig.getIpAccessLists();
    _namedIpSpaces = _currentConfig.getIpSpaces();

    // hops and sessions are per-trace.
    _breadcrumbs = breadcrumbs;
    _hops = new ArrayList<>(hops);
    _steps = new ArrayList<>(steps);
    _newSessions = new HashSet<>(newSessions);
  }

  /** Creates a new TransmissionContext for the specified last-hop node and outgoing interface. */
  private FlowTracer followEdge(Edge edge) {
    checkArgument(edge.getNode1().equals(_currentNode.getName()));
    return new FlowTracer(
        _tracerouteContext,
        new Node(edge.getNode2()),
        edge.getInt2(),
        _breadcrumbs,
        _hops,
        new ArrayList<>(),
        edge.getTail(),
        _newSessions,
        _originalFlow,
        _currentFlow,
        _flowTraces);
  }

  private FlowTracer branch() {
    return new FlowTracer(
        _tracerouteContext,
        _currentNode,
        _ingressInterface,
        _breadcrumbs,
        _hops,
        _steps,
        _lastHopNodeAndOutgoingInterface,
        _newSessions,
        _originalFlow,
        _currentFlow,
        _flowTraces);
  }

  private void processOutgoingInterfaceEdges(
      String outgoingInterface, Ip nextHopIp, SortedSet<Edge> edges) {
    checkArgument(!edges.isEmpty(), "No edges.");
    Ip arpIp =
        Route.UNSET_ROUTE_NEXT_HOP_IP.equals(nextHopIp) ? _currentFlow.getDstIp() : nextHopIp;

    if (!processArpFailure(outgoingInterface, arpIp)) {
      return;
    }

    _steps.add(buildExitOutputIfaceStep(outgoingInterface, TRANSMITTED));
    Hop hop = new Hop(_currentNode, _steps);
    _hops.add(hop);

    for (Edge edge : edges) {
      if (!edge.getNode1().equals(_currentNode.getName())) {
        continue;
      }
      checkState(edge.getInt1().equals(outgoingInterface), "Edge is not for outgoingInterface");
      String toNode = edge.getNode2();
      String toIface = edge.getInt2();
      if (_tracerouteContext.repliesToArp(toNode, toIface, arpIp)) {
        followEdge(edge).processHop();
      }
    }
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

  /**
   * Checks ARP reply for {@param arpIp}. Returns whether someone replies. If not, also constructs
   * the trace.
   */
  private boolean processArpFailure(String outgoingInterfaceName, Ip arpIp) {
    String currentNodeName = _currentNode.getName();
    String vrf = _currentConfig.getAllInterfaces().get(outgoingInterfaceName).getVrfName();
    // halt processing and add neighbor-unreachable trace if no one would respond
    if (_tracerouteContext.receivesArpReply(currentNodeName, vrf, outgoingInterfaceName, arpIp)) {
      FlowDisposition disposition =
          _tracerouteContext.computeDisposition(
              currentNodeName, outgoingInterfaceName, _currentFlow.getDstIp());
      buildArpFailureTrace(outgoingInterfaceName, disposition);
      return false;
    }
    return true;
  }

  void processHop() {
    checkState(_steps.isEmpty(), "Steps must be empty when processHop is called");
    checkState(
        _hops.size() == _breadcrumbs.size(), "Must have equal number of hops and breadcrumbs");

    String currentNodeName = _currentNode.getName();

    if (processSessions()) {
      // flow was processed by a session.
      return;
    }

    Map<String, IpAccessList> aclDefinitions = _currentConfig.getIpAccessLists();
    NavigableMap<String, IpSpace> namedIpSpaces = _currentConfig.getIpSpaces();

    // trace was received on a source interface of this hop
    if (_ingressInterface != null) {
      _steps.add(createEnterSrcIfaceStep(_currentConfig, _ingressInterface));

      // apply ingress filter
      IpAccessList inputFilter =
          _currentConfig.getAllInterfaces().get(_ingressInterface).getIncomingFilter();
      if (inputFilter != null) {
        if (applyFilter(inputFilter, INGRESS_FILTER) == DENIED) {
          return;
        }
      }

      TransformationResult transformationResult =
          TransformationEvaluator.eval(
              _currentConfig.getAllInterfaces().get(_ingressInterface).getIncomingTransformation(),
              _currentFlow,
              _ingressInterface,
              aclDefinitions,
              namedIpSpaces);
      _steps.addAll(transformationResult.getTraceSteps());
      _currentFlow = transformationResult.getOutputFlow();
    } else if (_currentFlow.getIngressVrf() != null) {
      // if inputIfaceName is not set for this hop, this is the originating step
      _steps.add(
          OriginateStep.builder()
              .setDetail(
                  OriginateStepDetail.builder()
                      .setOriginatingVrf(_currentFlow.getIngressVrf())
                      .build())
              .setAction(StepAction.ORIGINATED)
              .build());
    }

    Ip dstIp = _currentFlow.getDstIp();

    // Figure out where the trace came from..
    String vrfName;
    if (_ingressInterface == null) {
      vrfName = _currentFlow.getIngressVrf();
    } else {
      vrfName = _currentConfig.getAllInterfaces().get(_ingressInterface).getVrfName();
    }
    checkNotNull(vrfName, "Missing VRF.");

    // Loop detection
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, vrfName, _currentFlow);
    if (_breadcrumbs.contains(breadcrumb)) {
      buildLoopTrace();
      return;
    }

    _breadcrumbs.push(breadcrumb);
    try {
      // Accept if the flow is destined for this vrf on this host.
      if (_tracerouteContext.ownsIp(currentNodeName, vrfName, dstIp)) {
        buildAcceptTrace(vrfName);
        return;
      }

      // .. and what the next hops are based on the FIB.
      Fib fib = _tracerouteContext.getFib(currentNodeName, vrfName);
      Set<String> nextHopInterfaces = fib.getNextHopInterfaces(dstIp);
      if (nextHopInterfaces.isEmpty()) {
        buildNoRouteTrace();
        return;
      }

      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByRoute =
          fib.getNextHopInterfacesByRoute(dstIp);

      List<RouteInfo> matchedRibRouteInfo =
          _tracerouteContext.longestPrefixMatch(currentNodeName, vrfName, dstIp);

      _steps.add(
          RoutingStep.builder()
              .setDetail(RoutingStepDetail.builder().setRoutes(matchedRibRouteInfo).build())
              .setAction(StepAction.FORWARDED)
              .build());

      // For every interface with a route to the dst IP
      for (String nextHopInterfaceName : nextHopInterfaces) {
        FlowTracer clonedFlowTracer = branch();

        if (nextHopInterfaceName.equals(Interface.NULL_INTERFACE_NAME)) {
          clonedFlowTracer.buildNullRoutedTrace();
          continue;
        }

        Interface nextHopInterface = _currentConfig.getAllInterfaces().get(nextHopInterfaceName);

        Multimap<Ip, AbstractRoute> resolvedNextHopIpRoutes =
            resolveNextHopIpRoutes(nextHopInterfaceName, nextHopInterfacesByRoute);
        resolvedNextHopIpRoutes
            .asMap()
            .forEach(
                (resolvedNextHopIp, routeCandidates) ->
                    clonedFlowTracer.forwardOutInterface(nextHopInterface, resolvedNextHopIp));
      }
    } finally {
      _breadcrumbs.pop();
    }
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
    String vrf = incomingInterface.getVrfName();
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, vrf, flow);
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
        buildAcceptTrace(vrf);
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
              new Edge(
                  new NodeInterfacePair(currentNodeName, session.getOutgoingInterface()),
                  session.getNextHop()))
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
    if (applyFilter(outgoingInterface.getPreTransformationOutgoingFilter(), PRE_SOURCE_NAT_FILTER)
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
    SortedSet<Edge> edges =
        _tracerouteContext.getInterfaceOutEdges(currentNodeName, outgoingIfaceName);
    if (edges == null || edges.isEmpty()) {
      FlowDisposition disposition =
          _tracerouteContext.computeDisposition(
              currentNodeName, outgoingIfaceName, _currentFlow.getDstIp());

      buildArpFailureTrace(outgoingIfaceName, disposition);
    } else {
      processOutgoingInterfaceEdges(outgoingIfaceName, nextHopIp, edges);
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

  private void buildAcceptTrace(String vrfName) {
    InboundStep inboundStep =
        InboundStep.builder()
            .setAction(StepAction.ACCEPTED)
            .setDetail(new InboundStepDetail())
            .build();
    _steps.add(inboundStep);
    _hops.add(new Hop(_currentNode, _steps));
    Trace trace = new Trace(FlowDisposition.ACCEPTED, _hops);
    Flow returnFlow = returnFlow(_currentFlow, _currentNode.getName(), vrfName, null);
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
      _hops.add(new Hop(_currentNode, _steps));
      Trace trace = new Trace(filterType.deniedDisposition(), _hops);
      _flowTraces.accept(new TraceAndReverseFlow(trace, null));
    }
    return filterStep.getAction();
  }

  private void buildArpFailureTrace(String outInterface, FlowDisposition disposition) {
    String currentNodeName = _currentNode.getName();
    _steps.add(buildExitOutputIfaceStep(outInterface, getFinalActionForDisposition(disposition)));

    _hops.add(new Hop(_currentNode, _steps));

    Flow returnFlow =
        SUCCESS_DISPOSITIONS.contains(disposition)
            ? returnFlow(_currentFlow, currentNodeName, null, outInterface)
            : null;

    Trace trace = new Trace(disposition, _hops);
    _flowTraces.accept(new TraceAndReverseFlow(trace, returnFlow, _newSessions));
  }
}
