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

  /** Context specific to a single hop. */
  private class HopContext {
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

    private HopContext(
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

    private HopContext(
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
    private HopContext followEdge(Edge edge) {
      checkArgument(edge.getNode1().equals(_currentNode.getName()));
      return new HopContext(
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

    private HopContext branch() {
      return new HopContext(
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
      HopContext hopContext,
      Stack<Breadcrumb> breadcrumbs) {
    checkArgument(!edges.isEmpty(), "No edges.");
    Ip arpIp =
        Route.UNSET_ROUTE_NEXT_HOP_IP.equals(nextHopIp)
            ? hopContext._currentFlow.getDstIp()
            : nextHopIp;

    if (!processArpFailure(outgoingInterface, arpIp, hopContext)) {
      return;
    }

    hopContext._steps.add(buildExitOutputIfaceStep(outgoingInterface, hopContext, TRANSMITTED));
    Hop hop = new Hop(hopContext._currentNode, hopContext._steps);
    hopContext._hops.add(hop);

    for (Edge edge : edges) {
      if (!edge.getNode1().equals(hopContext._currentNode.getName())) {
        continue;
      }
      checkState(edge.getInt1().equals(outgoingInterface), "Edge is not for outgoingInterface");
      String toNode = edge.getNode2();
      String toIface = edge.getInt2();
      if (isArpSuccessful(arpIp, _forwardingAnalysis, _configurations.get(toNode), toIface)) {
        processHop(hopContext.followEdge(edge), breadcrumbs);
      }
    }
  }

  @Nonnull
  private static ExitOutputIfaceStep buildExitOutputIfaceStep(
      String outputIface, HopContext hopContext, StepAction action) {
    return ExitOutputIfaceStep.builder()
        .setDetail(
            ExitOutputIfaceStepDetail.builder()
                .setOutputInterface(
                    new NodeInterfacePair(hopContext._currentNode.getName(), outputIface))
                .setTransformedFlow(hopFlow(hopContext._originalFlow, hopContext._currentFlow))
                .build())
        .setAction(action)
        .build();
  }

  /**
   * Checks ARP reply for {@param arpIp}. Returns whether someone replies. If not, also constructs
   * the trace.
   */
  private boolean processArpFailure(String outgoingInterfaceName, Ip arpIp, HopContext hopContext) {
    String currentNodeName = hopContext._currentNode.getName();
    String vrf =
        hopContext._currentConfig.getAllInterfaces().get(outgoingInterfaceName).getVrfName();
    // halt processing and add neighbor-unreachable trace if no one would respond
    if (_forwardingAnalysis
        .getNeighborUnreachableOrExitsNetwork()
        .get(currentNodeName)
        .get(vrf)
        .get(outgoingInterfaceName)
        .containsIp(arpIp, hopContext._currentConfig.getIpSpaces())) {
      FlowDisposition disposition =
          computeDisposition(
              currentNodeName, outgoingInterfaceName, hopContext._currentFlow.getDstIp());
      buildArpFailureTrace(outgoingInterfaceName, disposition, hopContext);
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
              HopContext hopContext =
                  new HopContext(ingressNodeName, ingressInterfaceName, flow, currentTraces::add);
              processHop(hopContext, breadcrumbs);
            });
    return new TreeMap<>(traces);
  }

  private void processHop(HopContext hopContext, Stack<Breadcrumb> breadcrumbs) {
    checkState(hopContext._steps.isEmpty(), "Steps must be empty when processHop is called");

    String currentNodeName = hopContext._currentNode.getName();
    String inputIfaceName = hopContext._ingressInterface;
    Configuration currentConfiguration = _configurations.get(currentNodeName);
    if (currentConfiguration == null) {
      throw new BatfishException(
          String.format(
              "Node %s is not in the network, cannot perform traceroute", currentNodeName));
    }
    if (processSessions(hopContext, breadcrumbs)) {
      // flow was processed by a session.
      return;
    }

    Map<String, IpAccessList> aclDefinitions = currentConfiguration.getIpAccessLists();
    NavigableMap<String, IpSpace> namedIpSpaces = currentConfiguration.getIpSpaces();

    // trace was received on a source interface of this hop
    if (inputIfaceName != null) {
      hopContext._steps.add(createEnterSrcIfaceStep(currentConfiguration, inputIfaceName));

      // apply ingress filter
      IpAccessList inputFilter =
          currentConfiguration.getAllInterfaces().get(inputIfaceName).getIncomingFilter();
      if (inputFilter != null) {
        if (applyFilter(inputFilter, INGRESS_FILTER, hopContext) == DENIED) {
          return;
        }
      }

      TransformationResult transformationResult =
          TransformationEvaluator.eval(
              currentConfiguration
                  .getAllInterfaces()
                  .get(inputIfaceName)
                  .getIncomingTransformation(),
              hopContext._currentFlow,
              inputIfaceName,
              aclDefinitions,
              namedIpSpaces);
      hopContext._steps.addAll(transformationResult.getTraceSteps());
      hopContext._currentFlow = transformationResult.getOutputFlow();
    } else if (hopContext._currentFlow.getIngressVrf() != null) {
      // if inputIfaceName is not set for this hop, this is the originating step
      hopContext._steps.add(
          OriginateStep.builder()
              .setDetail(
                  OriginateStepDetail.builder()
                      .setOriginatingVrf(hopContext._currentFlow.getIngressVrf())
                      .build())
              .setAction(StepAction.ORIGINATED)
              .build());
    }

    Ip dstIp = hopContext._currentFlow.getDstIp();

    // Figure out where the trace came from..
    String vrfName;
    if (inputIfaceName == null) {
      vrfName = hopContext._currentFlow.getIngressVrf();
    } else {
      vrfName = currentConfiguration.getAllInterfaces().get(inputIfaceName).getVrfName();
    }
    checkNotNull(vrfName, "Missing VRF.");

    // Loop detection
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, vrfName, hopContext._currentFlow);
    if (breadcrumbs.contains(breadcrumb)) {
      buildLoopTrace(hopContext);
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
        buildAcceptTrace(hopContext, vrfName);
        return;
      }

      // .. and what the next hops are based on the FIB.
      Fib currentFib = _fibs.get(currentNodeName).get(vrfName);
      Set<String> nextHopInterfaces = currentFib.getNextHopInterfaces(dstIp);
      if (nextHopInterfaces.isEmpty()) {
        buildNoRouteTrace(hopContext);
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

      hopContext._steps.add(
          RoutingStep.builder()
              .setDetail(RoutingStepDetail.builder().setRoutes(matchedRibRouteInfo).build())
              .setAction(StepAction.FORWARDED)
              .build());

      // For every interface with a route to the dst IP
      for (String nextHopInterfaceName : nextHopInterfaces) {
        HopContext clonedHopContext = hopContext.branch();

        if (nextHopInterfaceName.equals(Interface.NULL_INTERFACE_NAME)) {
          buildNullRoutedTrace(clonedHopContext);
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
                        nextHopInterface, resolvedNextHopIp, clonedHopContext, breadcrumbs));
      }
    } finally {
      breadcrumbs.pop();
    }
  }

  private void buildNullRoutedTrace(HopContext hopContext) {
    hopContext._steps.add(
        ExitOutputIfaceStep.builder()
            .setDetail(
                ExitOutputIfaceStepDetail.builder()
                    .setOutputInterface(
                        new NodeInterfacePair(
                            hopContext._currentNode.getName(), Interface.NULL_INTERFACE_NAME))
                    .build())
            .setAction(StepAction.NULL_ROUTED)
            .build());
    hopContext._hops.add(new Hop(hopContext._currentNode, hopContext._steps));
    Trace trace = new Trace(FlowDisposition.NULL_ROUTED, hopContext._hops);
    hopContext._flowTraces.accept(new TraceAndReverseFlow(trace, null, hopContext._newSessions));
  }

  /** add a step for NO_ROUTE from source to output interface */
  private static void buildNoRouteTrace(HopContext hopContext) {
    Builder routingStepBuilder = RoutingStep.builder();
    routingStepBuilder
        .setDetail(RoutingStepDetail.builder().build())
        .setAction(StepAction.NO_ROUTE);
    hopContext._steps.add(routingStepBuilder.build());
    hopContext._hops.add(new Hop(hopContext._currentNode, hopContext._steps));
    Trace trace = new Trace(FlowDisposition.NO_ROUTE, hopContext._hops);
    hopContext._flowTraces.accept(new TraceAndReverseFlow(trace, null, hopContext._newSessions));
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
      HopContext hopContext,
      Stack<Breadcrumb> breadcrumbs) {
    String currentNodeName = hopContext._currentNode.getName();
    String outgoingIfaceName = outgoingInterface.getName();
    NodeInterfacePair nextHopInterface = new NodeInterfacePair(currentNodeName, outgoingIfaceName);

    // Apply preSourceNatOutgoingFilter
    if (applyFilter(
            outgoingInterface.getPreTransformationOutgoingFilter(),
            PRE_SOURCE_NAT_FILTER,
            hopContext)
        == DENIED) {
      return;
    }

    // Apply outgoing transformation
    Transformation transformation = outgoingInterface.getOutgoingTransformation();
    TransformationResult transformationResult =
        TransformationEvaluator.eval(
            transformation,
            hopContext._currentFlow,
            hopContext._ingressInterface,
            hopContext._aclDefinitions,
            hopContext._namedIpSpaces);
    hopContext._steps.addAll(transformationResult.getTraceSteps());
    hopContext._currentFlow = transformationResult.getOutputFlow();

    // apply outgoing filter
    if (applyFilter(outgoingInterface.getOutgoingFilter(), EGRESS_FILTER, hopContext) == DENIED) {
      return;
    }

    // setup session if necessary
    FirewallSessionInterfaceInfo firewallSessionInterfaceInfo =
        outgoingInterface.getFirewallSessionInterfaceInfo();
    if (firewallSessionInterfaceInfo != null) {
      hopContext._newSessions.add(
          buildFirewallSessionTraceInfo(hopContext, firewallSessionInterfaceInfo));
      hopContext._steps.add(new SetupSessionStep());
    }

    SortedSet<Edge> edges = _dataPlane.getTopology().getInterfaceEdges().get(nextHopInterface);
    if (edges == null || edges.isEmpty()) {
      FlowDisposition disposition =
          computeDisposition(
              currentNodeName, outgoingIfaceName, hopContext._currentFlow.getDstIp());

      buildArpFailureTrace(outgoingIfaceName, disposition, hopContext);
    } else {
      processOutgoingInterfaceEdges(outgoingIfaceName, nextHopIp, edges, hopContext, breadcrumbs);
    }
  }

  @Nonnull
  private static FirewallSessionTraceInfo buildFirewallSessionTraceInfo(
      HopContext hopContext, @Nonnull FirewallSessionInterfaceInfo firewallSessionInterfaceInfo) {
    return new FirewallSessionTraceInfo(
        hopContext._currentNode.getName(),
        hopContext._ingressInterface,
        hopContext._lastHopNodeAndOutgoingInterface,
        firewallSessionInterfaceInfo.getSessionInterfaces(),
        match5Tuple(
            hopContext._currentFlow.getDstIp(),
            hopContext._currentFlow.getDstPort(),
            hopContext._currentFlow.getSrcIp(),
            hopContext._currentFlow.getSrcPort(),
            hopContext._currentFlow.getIpProtocol()),
        sessionTransformation(hopContext._originalFlow, hopContext._currentFlow));
  }

  private static void buildAcceptTrace(HopContext hopContext, String vrfName) {
    InboundStep inboundStep =
        InboundStep.builder()
            .setAction(StepAction.ACCEPTED)
            .setDetail(new InboundStepDetail())
            .build();
    hopContext._steps.add(inboundStep);
    hopContext._hops.add(new Hop(hopContext._currentNode, hopContext._steps));
    Trace trace = new Trace(FlowDisposition.ACCEPTED, hopContext._hops);
    Flow returnFlow =
        returnFlow(hopContext._currentFlow, hopContext._currentNode.getName(), vrfName, null);
    hopContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, returnFlow, hopContext._newSessions));
  }

  private static void buildLoopTrace(HopContext hopContext) {
    hopContext._hops.add(new Hop(hopContext._currentNode, hopContext._steps));
    Trace trace = new Trace(FlowDisposition.LOOP, hopContext._hops);
    hopContext._flowTraces.accept(new TraceAndReverseFlow(trace, null, hopContext._newSessions));
  }

  /**
   * Apply a filter, and create the corresponding step. If the filter DENIED the flow, then create a
   * trace ending in the denial. Return the action if the filter is non-null. If the filter is null,
   * return null.
   */
  private @Nullable StepAction applyFilter(
      @Nullable IpAccessList filter, FilterType filterType, HopContext hopContext) {
    if (filter == null) {
      return null;
    }
    FilterStep filterStep =
        createFilterStep(
            hopContext._currentFlow,
            hopContext._ingressInterface,
            filter,
            filterType,
            hopContext._aclDefinitions,
            hopContext._namedIpSpaces,
            _ignoreFilters);
    hopContext._steps.add(filterStep);
    if (filterStep.getAction() == DENIED) {
      hopContext._hops.add(new Hop(hopContext._currentNode, hopContext._steps));
      Trace trace = new Trace(filterType.deniedDisposition(), hopContext._hops);
      hopContext._flowTraces.accept(new TraceAndReverseFlow(trace, null));
    }
    return filterStep.getAction();
  }

  /**
   * Check this {@param flow} matches a session on this device. If so, process the flow. Returns
   * true if the flow is matched/processed.
   */
  private boolean processSessions(HopContext hopContext, Stack<Breadcrumb> breadcrumbs) {
    String inputIfaceName = hopContext._ingressInterface;
    if (inputIfaceName == null) {
      // Sessions only exist when entering an interface.
      return false;
    }

    String currentNodeName = hopContext._currentNode.getName();
    Flow flow = hopContext._currentFlow;
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

    hopContext._steps.add(new MatchSessionStep());

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
        && applyFilter(ipAccessLists.get(incomingAclName), FilterType.INGRESS_FILTER, hopContext)
            == DENIED) {
      return true;
    }

    // cycle detection
    String vrf = incomingInterface.getVrfName();
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, vrf, flow);
    if (breadcrumbs.contains(breadcrumb)) {
      buildLoopTrace(hopContext);
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
        hopContext._steps.addAll(result.getTraceSteps());
        hopContext._currentFlow = result.getOutputFlow();
      }
      if (session.getOutgoingInterface() == null) {
        // Accepted by this node (vrf of incoming interface).
        buildAcceptTrace(hopContext, vrf);
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
          && applyFilter(ipAccessLists.get(outgoingAclName), FilterType.EGRESS_FILTER, hopContext)
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
            session.getOutgoingInterface(), FlowDisposition.EXITS_NETWORK, hopContext);
        return true;
      }

      hopContext._steps.add(
          buildExitOutputIfaceStep(outgoingInterface.getName(), hopContext, TRANSMITTED));
      hopContext._hops.add(new Hop(new Node(currentNodeName), hopContext._steps));

      // Forward to neighbor.
      processHop(
          hopContext.followEdge(
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
      String outInterface, FlowDisposition disposition, HopContext hopContext) {
    String currentNodeName = hopContext._currentNode.getName();
    hopContext._steps.add(
        buildExitOutputIfaceStep(
            outInterface, hopContext, getFinalActionForDisposition(disposition)));

    hopContext._hops.add(new Hop(hopContext._currentNode, hopContext._steps));

    Flow returnFlow =
        SUCCESS_DISPOSITIONS.contains(disposition)
            ? returnFlow(hopContext._currentFlow, currentNodeName, null, outInterface)
            : null;

    Trace trace = new Trace(disposition, hopContext._hops);
    hopContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, returnFlow, hopContext._newSessions));
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
