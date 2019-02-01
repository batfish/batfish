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
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.dataplane.traceroute.TracerouteUtils.buildSessionsByIngressInterface;
import static org.batfish.dataplane.traceroute.TracerouteUtils.createEnterSrcIfaceStep;
import static org.batfish.dataplane.traceroute.TracerouteUtils.createFilterStep;
import static org.batfish.dataplane.traceroute.TracerouteUtils.getFinalActionForDisposition;
import static org.batfish.dataplane.traceroute.TracerouteUtils.isArpSuccessful;
import static org.batfish.dataplane.traceroute.TracerouteUtils.returnFlow;
import static org.batfish.dataplane.traceroute.TracerouteUtils.validateInputs;
import static org.batfish.specifier.DispositionSpecifier.SUCCESS_DISPOSITIONS;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.ForwardingAnalysis;
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
import org.batfish.datamodel.transformation.TransformationStep;

/**
 * Class containing an implementation of {@link
 * org.batfish.dataplane.TracerouteEngineImpl#computeTraces(Set, boolean)} and the context (data)
 * needed for it
 */
public class TracerouteEngineImplContext {
  /** Used for loop detection */
  private static class Breadcrumb {
    private final @Nonnull String _node;
    private final @Nonnull String _vrf;
    private final @Nonnull Flow _flow;

    Breadcrumb(@Nonnull String node, @Nonnull String vrf, @Nonnull Flow flow) {
      _node = node;
      _vrf = vrf;
      _flow = flow;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (!(obj instanceof Breadcrumb)) {
        return false;
      }

      Breadcrumb other = (Breadcrumb) obj;

      return _node.equals(other._node) && _vrf.equals(other._vrf) && _flow.equals(other._flow);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_node, _vrf, _flow);
    }
  }

  /** Context specific to a single trace. */
  private class TraceContext {
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

    // The current flow can change as we process the packet.
    private Flow _currentFlow;

    private TraceContext(
        String node,
        @Nullable String ingressInterface,
        Flow originalFlow,
        Consumer<TraceAndReverseFlow> flowTraces) {
      _currentConfig = _configurations.get(node);
      _currentNode = new Node(node);
      _aclDefinitions = _currentConfig.getIpAccessLists();
      _flowTraces = flowTraces;
      _ingressInterface = ingressInterface;
      _lastHopNodeAndOutgoingInterface = null;
      _newSessions = new HashSet<>();
      _namedIpSpaces = _currentConfig.getIpSpaces();
      _originalFlow = originalFlow;

      _hops = new ArrayList<>();
      _steps = new ArrayList<>();

      _currentFlow = originalFlow;
    }

    private TraceContext(
        Node currentNode,
        @Nullable String ingressInterface,
        List<Hop> hops,
        List<Step<?>> steps,
        NodeInterfacePair lastHopNodeAndOutgoingInterface,
        Set<FirewallSessionTraceInfo> newSessions,
        Flow originalFlow,
        Flow currentFlow,
        Consumer<TraceAndReverseFlow> flowTraces) {
      _currentNode = currentNode;
      _ingressInterface = ingressInterface;
      _lastHopNodeAndOutgoingInterface = lastHopNodeAndOutgoingInterface;
      _originalFlow = originalFlow;
      _currentFlow = currentFlow;
      _flowTraces = flowTraces;

      // essentially just cached values
      _currentConfig = _configurations.get(currentNode.getName());
      _aclDefinitions = _currentConfig.getIpAccessLists();
      _namedIpSpaces = _currentConfig.getIpSpaces();

      // hops and sessions are per-trace.
      _hops = new ArrayList<>(hops);
      _steps = new ArrayList<>(steps);
      _newSessions = new HashSet<>(newSessions);
    }

    /** Creates a new TransmissionContext for the specified last-hop node and outgoing interface. */
    private TraceContext followEdge(Edge edge) {
      checkArgument(edge.getNode1().equals(_currentNode.getName()));
      return new TraceContext(
          new Node(edge.getNode2()),
          edge.getInt2(),
          _hops,
          new ArrayList<>(),
          edge.getTail(),
          _newSessions,
          _originalFlow,
          _currentFlow,
          _flowTraces);
    }

    private TraceContext branch() {
      return new TraceContext(
          _currentNode,
          _ingressInterface,
          _hops,
          _steps,
          _lastHopNodeAndOutgoingInterface,
          _newSessions,
          _originalFlow,
          _currentFlow,
          _flowTraces);
    }
  }

  private final Map<String, Configuration> _configurations;
  private final DataPlane _dataPlane;
  private final Multimap<NodeInterfacePair, FirewallSessionTraceInfo> _sessionsByIngressInterface;
  private final Map<String, Map<String, Fib>> _fibs;
  private final Set<Flow> _flows;
  private final ForwardingAnalysis _forwardingAnalysis;
  private final boolean _ignoreFilters;

  public TracerouteEngineImplContext(
      DataPlane dataPlane,
      Set<FirewallSessionTraceInfo> sessions,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreFilters) {
    _configurations = dataPlane.getConfigurations();
    _dataPlane = dataPlane;
    _flows = flows;
    _fibs = fibs;
    _ignoreFilters = ignoreFilters;
    _forwardingAnalysis = _dataPlane.getForwardingAnalysis();
    _sessionsByIngressInterface = buildSessionsByIngressInterface(sessions);
  }

  private void processOutgoingInterfaceEdges(
      String outgoingInterface,
      Ip nextHopIp,
      SortedSet<Edge> edges,
      TraceContext traceContext,
      Stack<Breadcrumb> breadcrumbs) {
    checkArgument(!edges.isEmpty(), "No edges.");
    Ip arpIp =
        Route.UNSET_ROUTE_NEXT_HOP_IP.equals(nextHopIp)
            ? traceContext._currentFlow.getDstIp()
            : nextHopIp;

    if (!processArpFailure(outgoingInterface, arpIp, traceContext)) {
      return;
    }

    traceContext._steps.add(buildExitOutputIfaceStep(outgoingInterface, traceContext, TRANSMITTED));
    Hop hop = new Hop(traceContext._currentNode, traceContext._steps);
    traceContext._hops.add(hop);

    for (Edge edge : edges) {
      if (!edge.getNode1().equals(traceContext._currentNode.getName())) {
        continue;
      }
      checkState(edge.getInt1().equals(outgoingInterface), "Edge is not for outgoingInterface");
      String toNode = edge.getNode2();
      String toIface = edge.getInt2();
      if (isArpSuccessful(arpIp, _forwardingAnalysis, _configurations.get(toNode), toIface)) {
        processHop(traceContext.followEdge(edge), breadcrumbs);
      }
    }
  }

  @Nonnull
  private static ExitOutputIfaceStep buildExitOutputIfaceStep(
      String outputIface, TraceContext traceContext, StepAction action) {
    return ExitOutputIfaceStep.builder()
        .setDetail(
            ExitOutputIfaceStepDetail.builder()
                .setOutputInterface(
                    new NodeInterfacePair(traceContext._currentNode.getName(), outputIface))
                .setTransformedFlow(hopFlow(traceContext._originalFlow, traceContext._currentFlow))
                .build())
        .setAction(action)
        .build();
  }

  /**
   * Checks ARP reply for {@param arpIp}. Returns whether someone replies. If not, also constructs
   * the trace.
   */
  private boolean processArpFailure(
      String outgoingInterfaceName, Ip arpIp, TraceContext traceContext) {
    String currentNodeName = traceContext._currentNode.getName();
    String vrf =
        traceContext._currentConfig.getAllInterfaces().get(outgoingInterfaceName).getVrfName();
    // halt processing and add neighbor-unreachable trace if no one would respond
    if (_forwardingAnalysis
        .getNeighborUnreachableOrExitsNetwork()
        .get(currentNodeName)
        .get(vrf)
        .get(outgoingInterfaceName)
        .containsIp(arpIp, traceContext._currentConfig.getIpSpaces())) {
      FlowDisposition disposition =
          computeDisposition(
              currentNodeName, outgoingInterfaceName, traceContext._currentFlow.getDstIp());
      buildArpFailureTrace(outgoingInterfaceName, disposition, traceContext);
      return false;
    }
    return true;
  }

  /**
   * Builds the possible {@link Trace}s for a {@link Set} of {@link Flow}s in {@link
   * TracerouteEngineImplContext#_flows}
   *
   * @return {@link SortedMap} of {@link Flow} to a {@link List} of {@link Trace}s
   */
  public SortedMap<Flow, List<TraceAndReverseFlow>> buildTracesAndReturnFlows() {
    Map<Flow, List<TraceAndReverseFlow>> traces = new ConcurrentHashMap<>();
    _flows
        .parallelStream()
        .forEach(
            flow -> {
              List<TraceAndReverseFlow> currentTraces =
                  traces.computeIfAbsent(flow, k -> new ArrayList<>());
              validateInputs(_configurations, flow);
              String ingressNodeName = flow.getIngressNode();
              Stack<Breadcrumb> breadcrumbs = new Stack<>();
              String ingressInterfaceName = flow.getIngressInterface();
              TraceContext traceContext =
                  new TraceContext(ingressNodeName, ingressInterfaceName, flow, currentTraces::add);
              processHop(traceContext, breadcrumbs);
            });
    return new TreeMap<>(traces);
  }

  private void processHop(TraceContext traceContext, Stack<Breadcrumb> breadcrumbs) {
    checkState(traceContext._steps.isEmpty(), "Steps must be empty when processHop is called");

    String currentNodeName = traceContext._currentNode.getName();
    String inputIfaceName = traceContext._ingressInterface;
    Configuration currentConfiguration = _configurations.get(currentNodeName);
    if (currentConfiguration == null) {
      throw new BatfishException(
          String.format(
              "Node %s is not in the network, cannot perform traceroute", currentNodeName));
    }
    if (processSessions(traceContext, breadcrumbs)) {
      // flow was processed by a session.
      return;
    }

    Map<String, IpAccessList> aclDefinitions = currentConfiguration.getIpAccessLists();
    NavigableMap<String, IpSpace> namedIpSpaces = currentConfiguration.getIpSpaces();

    // trace was received on a source interface of this hop
    if (inputIfaceName != null) {
      traceContext._steps.add(createEnterSrcIfaceStep(currentConfiguration, inputIfaceName));

      // apply ingress filter
      IpAccessList inputFilter =
          currentConfiguration.getAllInterfaces().get(inputIfaceName).getIncomingFilter();
      if (inputFilter != null) {
        if (applyFilter(inputFilter, INGRESS_FILTER, traceContext) == DENIED) {
          return;
        }
      }

      TransformationResult transformationResult =
          TransformationEvaluator.eval(
              currentConfiguration
                  .getAllInterfaces()
                  .get(inputIfaceName)
                  .getIncomingTransformation(),
              traceContext._currentFlow,
              inputIfaceName,
              aclDefinitions,
              namedIpSpaces);
      traceContext._steps.addAll(transformationResult.getTraceSteps());
      traceContext._currentFlow = transformationResult.getOutputFlow();
    } else if (traceContext._currentFlow.getIngressVrf() != null) {
      // if inputIfaceName is not set for this hop, this is the originating step
      traceContext._steps.add(
          OriginateStep.builder()
              .setDetail(
                  OriginateStepDetail.builder()
                      .setOriginatingVrf(traceContext._currentFlow.getIngressVrf())
                      .build())
              .setAction(StepAction.ORIGINATED)
              .build());
    }

    Ip dstIp = traceContext._currentFlow.getDstIp();

    // Figure out where the trace came from..
    String vrfName;
    if (inputIfaceName == null) {
      vrfName = traceContext._currentFlow.getIngressVrf();
    } else {
      vrfName = currentConfiguration.getAllInterfaces().get(inputIfaceName).getVrfName();
    }
    checkNotNull(vrfName, "Missing VRF.");

    // Loop detection
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, vrfName, traceContext._currentFlow);
    if (breadcrumbs.contains(breadcrumb)) {
      buildLoopTrace(traceContext);
      return;
    }

    breadcrumbs.push(breadcrumb);
    // use try/finally to make sure we pop off the breadcrumb
    try {
      // Accept if the flow is destined for this vrf on this host.
      if (_dataPlane
          .getIpVrfOwners()
          .getOrDefault(dstIp, ImmutableMap.of())
          .getOrDefault(currentConfiguration.getHostname(), ImmutableSet.of())
          .contains(vrfName)) {
        buildAcceptTrace(traceContext, vrfName);
        return;
      }

      // .. and what the next hops are based on the FIB.
      Fib currentFib = _fibs.get(currentNodeName).get(vrfName);
      Set<String> nextHopInterfaces = currentFib.getNextHopInterfaces(dstIp);
      if (nextHopInterfaces.isEmpty()) {
        buildNoRouteTrace(traceContext);
        return;
      }

      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByRoute =
          currentFib.getNextHopInterfacesByRoute(dstIp);

      List<RouteInfo> matchedRibRouteInfo =
          _dataPlane.getRibs().get(currentNodeName).get(vrfName).longestPrefixMatch(dstIp).stream()
              .sorted()
              .map(rc -> new RouteInfo(rc.getProtocol(), rc.getNetwork(), rc.getNextHopIp()))
              .distinct()
              .collect(ImmutableList.toImmutableList());

      traceContext._steps.add(
          RoutingStep.builder()
              .setDetail(RoutingStepDetail.builder().setRoutes(matchedRibRouteInfo).build())
              .setAction(StepAction.FORWARDED)
              .build());

      // For every interface with a route to the dst IP
      for (String nextHopInterfaceName : nextHopInterfaces) {
        TraceContext clonedTraceContext = traceContext.branch();

        if (nextHopInterfaceName.equals(Interface.NULL_INTERFACE_NAME)) {
          buildNullRoutedTrace(clonedTraceContext);
          continue;
        }

        Interface nextHopInterface =
            currentConfiguration.getAllInterfaces().get(nextHopInterfaceName);

        Multimap<Ip, AbstractRoute> resolvedNextHopIpRoutes =
            resolveNextHopIpRoutes(nextHopInterfaceName, nextHopInterfacesByRoute);
        resolvedNextHopIpRoutes
            .asMap()
            .forEach(
                (resolvedNextHopIp, routeCandidates) ->
                    forwardOutInterface(
                        nextHopInterface, resolvedNextHopIp, clonedTraceContext, breadcrumbs));
      }
    } finally {
      breadcrumbs.pop();
    }
  }

  private void buildNullRoutedTrace(TraceContext traceContext) {
    traceContext._steps.add(
        ExitOutputIfaceStep.builder()
            .setDetail(
                ExitOutputIfaceStepDetail.builder()
                    .setOutputInterface(
                        new NodeInterfacePair(
                            traceContext._currentNode.getName(), Interface.NULL_INTERFACE_NAME))
                    .build())
            .setAction(StepAction.NULL_ROUTED)
            .build());
    traceContext._hops.add(new Hop(traceContext._currentNode, traceContext._steps));
    Trace trace = new Trace(FlowDisposition.NULL_ROUTED, traceContext._hops);
    traceContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, null, traceContext._newSessions));
  }

  /** add a step for NO_ROUTE from source to output interface */
  private static void buildNoRouteTrace(TraceContext traceContext) {
    Builder routingStepBuilder = RoutingStep.builder();
    routingStepBuilder
        .setDetail(RoutingStepDetail.builder().build())
        .setAction(StepAction.NO_ROUTE);
    traceContext._steps.add(routingStepBuilder.build());
    traceContext._hops.add(new Hop(traceContext._currentNode, traceContext._steps));
    Trace trace = new Trace(FlowDisposition.NO_ROUTE, traceContext._hops);
    traceContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, null, traceContext._newSessions));
  }

  @Nonnull
  private static Multimap<Ip, AbstractRoute> resolveNextHopIpRoutes(
      String nextHopInterfaceName,
      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByRoute) {
    TreeMultimap<Ip, AbstractRoute> resolvedNextHopWithRoutes = TreeMultimap.create();

    // Loop over all matching routes that use nextHopInterfaceName as one of the next hop
    // interfaces.
    for (Entry<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> e :
        nextHopInterfacesByRoute.entrySet()) {
      Map<Ip, Set<AbstractRoute>> finalNextHops = e.getValue().get(nextHopInterfaceName);
      if (finalNextHops == null || finalNextHops.isEmpty()) {
        continue;
      }

      AbstractRoute routeCandidate = e.getKey();
      Ip nextHopIp = routeCandidate.getNextHopIp();
      if (nextHopIp.equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
        resolvedNextHopWithRoutes.put(nextHopIp, routeCandidate);
      } else {
        for (Ip resolvedNextHopIp : finalNextHops.keySet()) {
          resolvedNextHopWithRoutes.put(resolvedNextHopIp, routeCandidate);
        }
      }
    }
    return resolvedNextHopWithRoutes;
  }

  private void forwardOutInterface(
      Interface outgoingInterface,
      Ip nextHopIp,
      TraceContext traceContext,
      Stack<Breadcrumb> breadcrumbs) {
    String currentNodeName = traceContext._currentNode.getName();
    String outgoingIfaceName = outgoingInterface.getName();
    NodeInterfacePair nextHopInterface = new NodeInterfacePair(currentNodeName, outgoingIfaceName);

    // Apply preSourceNatOutgoingFilter
    if (applyFilter(
            outgoingInterface.getPreTransformationOutgoingFilter(),
            PRE_SOURCE_NAT_FILTER,
            traceContext)
        == DENIED) {
      return;
    }

    // Apply outgoing transformation
    Transformation transformation = outgoingInterface.getOutgoingTransformation();
    TransformationResult transformationResult =
        TransformationEvaluator.eval(
            transformation,
            traceContext._currentFlow,
            traceContext._ingressInterface,
            traceContext._aclDefinitions,
            traceContext._namedIpSpaces);
    traceContext._steps.addAll(transformationResult.getTraceSteps());
    traceContext._currentFlow = transformationResult.getOutputFlow();

    // apply outgoing filter
    if (applyFilter(outgoingInterface.getOutgoingFilter(), EGRESS_FILTER, traceContext) == DENIED) {
      return;
    }

    // setup session if necessary
    FirewallSessionInterfaceInfo firewallSessionInterfaceInfo =
        outgoingInterface.getFirewallSessionInterfaceInfo();
    if (firewallSessionInterfaceInfo != null) {
      traceContext._newSessions.add(
          buildFirewallSessionTraceInfo(traceContext, firewallSessionInterfaceInfo));
      traceContext._steps.add(new SetupSessionStep());
    }

    SortedSet<Edge> edges = _dataPlane.getTopology().getInterfaceEdges().get(nextHopInterface);
    if (edges == null || edges.isEmpty()) {
      FlowDisposition disposition =
          computeDisposition(
              currentNodeName, outgoingIfaceName, traceContext._currentFlow.getDstIp());

      buildArpFailureTrace(outgoingIfaceName, disposition, traceContext);
    } else {
      processOutgoingInterfaceEdges(outgoingIfaceName, nextHopIp, edges, traceContext, breadcrumbs);
    }
  }

  @Nonnull
  private static FirewallSessionTraceInfo buildFirewallSessionTraceInfo(
      TraceContext traceContext,
      @Nonnull FirewallSessionInterfaceInfo firewallSessionInterfaceInfo) {
    return new FirewallSessionTraceInfo(
        traceContext._currentNode.getName(),
        traceContext._ingressInterface,
        traceContext._lastHopNodeAndOutgoingInterface,
        firewallSessionInterfaceInfo.getSessionInterfaces(),
        match5Tuple(
            traceContext._currentFlow.getDstIp(),
            traceContext._currentFlow.getDstPort(),
            traceContext._currentFlow.getSrcIp(),
            traceContext._currentFlow.getSrcPort(),
            traceContext._currentFlow.getIpProtocol()),
        sessionTransformation(traceContext._originalFlow, traceContext._currentFlow));
  }

  private static void buildAcceptTrace(TraceContext traceContext, String vrfName) {
    InboundStep inboundStep =
        InboundStep.builder()
            .setAction(StepAction.ACCEPTED)
            .setDetail(new InboundStepDetail())
            .build();
    traceContext._steps.add(inboundStep);
    traceContext._hops.add(new Hop(traceContext._currentNode, traceContext._steps));
    Trace trace = new Trace(FlowDisposition.ACCEPTED, traceContext._hops);
    Flow returnFlow =
        returnFlow(traceContext._currentFlow, traceContext._currentNode.getName(), vrfName, null);
    traceContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, returnFlow, traceContext._newSessions));
  }

  private static void buildLoopTrace(TraceContext traceContext) {
    traceContext._hops.add(new Hop(traceContext._currentNode, traceContext._steps));
    Trace trace = new Trace(FlowDisposition.LOOP, traceContext._hops);
    traceContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, null, traceContext._newSessions));
  }

  /**
   * Apply a filter, and create the corresponding step. If the filter DENIED the flow, then create a
   * trace ending in the denial. Return the action if the filter is non-null. If the filter is null,
   * return null.
   */
  private @Nullable StepAction applyFilter(
      @Nullable IpAccessList filter, FilterType filterType, TraceContext traceContext) {
    if (filter == null) {
      return null;
    }
    FilterStep filterStep =
        createFilterStep(
            traceContext._currentFlow,
            traceContext._ingressInterface,
            filter,
            filterType,
            traceContext._aclDefinitions,
            traceContext._namedIpSpaces,
            _ignoreFilters);
    traceContext._steps.add(filterStep);
    if (filterStep.getAction() == DENIED) {
      traceContext._hops.add(new Hop(traceContext._currentNode, traceContext._steps));
      Trace trace = new Trace(filterType.deniedDisposition(), traceContext._hops);
      traceContext._flowTraces.accept(new TraceAndReverseFlow(trace, null));
    }
    return filterStep.getAction();
  }

  /**
   * Check this {@param flow} matches a session on this device. If so, process the flow. Returns
   * true if the flow is matched/processed.
   */
  private boolean processSessions(TraceContext traceContext, Stack<Breadcrumb> breadcrumbs) {
    String inputIfaceName = traceContext._ingressInterface;
    if (inputIfaceName == null) {
      // Sessions only exist when entering an interface.
      return false;
    }

    String currentNodeName = traceContext._currentNode.getName();
    Flow flow = traceContext._currentFlow;
    Collection<FirewallSessionTraceInfo> sessions =
        _sessionsByIngressInterface.get(new NodeInterfacePair(currentNodeName, inputIfaceName));
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

    traceContext._steps.add(new MatchSessionStep());

    Configuration config = _configurations.get(currentNodeName);
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
        && applyFilter(ipAccessLists.get(incomingAclName), FilterType.INGRESS_FILTER, traceContext)
            == DENIED) {
      return true;
    }

    // cycle detection
    String vrf = incomingInterface.getVrfName();
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, vrf, flow);
    if (breadcrumbs.contains(breadcrumb)) {
      buildLoopTrace(traceContext);
      return true;
    }

    breadcrumbs.push(breadcrumb);
    try {
      // apply transformation
      Transformation transformation = session.getTransformation();
      if (transformation != null) {
        TransformationResult result =
            TransformationEvaluator.eval(
                transformation, flow, inputIfaceName, ipAccessLists, ipSpaces);
        traceContext._steps.addAll(result.getTraceSteps());
        traceContext._currentFlow = result.getOutputFlow();
      }
      if (session.getOutgoingInterface() == null) {
        // Accepted by this node (vrf of incoming interface).
        buildAcceptTrace(traceContext, vrf);
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
          && applyFilter(ipAccessLists.get(outgoingAclName), FilterType.EGRESS_FILTER, traceContext)
              == DENIED) {
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
        buildArpFailureTrace(
            session.getOutgoingInterface(), FlowDisposition.EXITS_NETWORK, traceContext);
        return true;
      }

      traceContext._steps.add(
          buildExitOutputIfaceStep(outgoingInterface.getName(), traceContext, TRANSMITTED));
      traceContext._hops.add(new Hop(new Node(currentNodeName), traceContext._steps));

      // Forward to neighbor.
      processHop(
          traceContext.followEdge(
              new Edge(
                  new NodeInterfacePair(currentNodeName, session.getOutgoingInterface()),
                  session.getNextHop())),
          breadcrumbs);
      return true;
    } finally {
      breadcrumbs.pop();
    }
  }

  @Nullable
  private static Transformation sessionTransformation(Flow inputFlow, Flow currentFlow) {
    ImmutableList.Builder<TransformationStep> transformationStepsBuilder = ImmutableList.builder();

    Ip origDstIp = inputFlow.getDstIp();
    if (!origDstIp.equals(currentFlow.getDstIp())) {
      transformationStepsBuilder.add(assignSourceIp(origDstIp, origDstIp));
    }

    Ip origSrcIp = inputFlow.getSrcIp();
    if (!origSrcIp.equals(currentFlow.getSrcIp())) {
      transformationStepsBuilder.add(TransformationStep.assignDestinationIp(origSrcIp, origSrcIp));
    }

    List<TransformationStep> transformationSteps = transformationStepsBuilder.build();

    return transformationSteps.isEmpty() ? null : always().apply(transformationSteps).build();
  }

  @Nullable
  private static Flow hopFlow(Flow originalFlow, @Nullable Flow transformedFlow) {
    if (originalFlow == transformedFlow) {
      return null;
    } else {
      return transformedFlow;
    }
  }

  private static void buildArpFailureTrace(
      String outInterface, FlowDisposition disposition, TraceContext traceContext) {
    String currentNodeName = traceContext._currentNode.getName();
    traceContext._steps.add(
        buildExitOutputIfaceStep(
            outInterface, traceContext, getFinalActionForDisposition(disposition)));

    traceContext._hops.add(new Hop(traceContext._currentNode, traceContext._steps));

    Flow returnFlow =
        SUCCESS_DISPOSITIONS.contains(disposition)
            ? returnFlow(traceContext._currentFlow, currentNodeName, null, outInterface)
            : null;

    Trace trace = new Trace(disposition, traceContext._hops);
    traceContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, returnFlow, traceContext._newSessions));
  }

  /**
   * Returns dispositions for the special case when a {@link Flow} either exits the network, gets
   * delivered to subnet, gets terminated due to an unreachable neighbor or when information is not
   * sufficient to compute disposition
   *
   * @param hostname Hostname of the current {@link Hop}
   * @param outgoingInterfaceName output interface for the current {@link Hop}
   * @param dstIp Destination IP for the {@link Flow}
   * @return one of {@link FlowDisposition#DELIVERED_TO_SUBNET}, {@link
   *     FlowDisposition#EXITS_NETWORK}, {@link FlowDisposition#INSUFFICIENT_INFO} or {@link
   *     FlowDisposition#NEIGHBOR_UNREACHABLE}
   */
  private FlowDisposition computeDisposition(
      String hostname, String outgoingInterfaceName, Ip dstIp) {
    String vrfName =
        _configurations.get(hostname).getAllInterfaces().get(outgoingInterfaceName).getVrfName();
    if (_forwardingAnalysis
        .getDeliveredToSubnet()
        .get(hostname)
        .get(vrfName)
        .get(outgoingInterfaceName)
        .containsIp(dstIp, ImmutableMap.of())) {
      return FlowDisposition.DELIVERED_TO_SUBNET;
    } else if (_forwardingAnalysis
        .getExitsNetwork()
        .get(hostname)
        .get(vrfName)
        .get(outgoingInterfaceName)
        .containsIp(dstIp, ImmutableMap.of())) {
      return FlowDisposition.EXITS_NETWORK;
    } else if (_forwardingAnalysis
        .getInsufficientInfo()
        .get(hostname)
        .get(vrfName)
        .get(outgoingInterfaceName)
        .containsIp(dstIp, ImmutableMap.of())) {
      return FlowDisposition.INSUFFICIENT_INFO;
    } else if (_forwardingAnalysis
        .getNeighborUnreachable()
        .get(hostname)
        .get(vrfName)
        .get(outgoingInterfaceName)
        .containsIp(dstIp, ImmutableMap.of())) {
      return FlowDisposition.NEIGHBOR_UNREACHABLE;
    } else {
      throw new BatfishException(
          String.format(
              "No disposition at hostname=%s outgoingInterface=%s for destIp=%s",
              hostname, outgoingInterfaceName, dstIp));
    }
  }
}
