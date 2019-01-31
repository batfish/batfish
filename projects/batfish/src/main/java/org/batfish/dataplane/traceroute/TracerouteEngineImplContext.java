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
import org.batfish.datamodel.flow.EnterInputIfaceStep;
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

  private static class TransmissionContext {
    private final Map<String, Configuration> _configurations;
    private final Configuration _currentConfig;
    private final @Nullable String _ingressInterface;
    private final Map<String, IpAccessList> _aclDefinitions;
    private final Node _currentNode;
    private final Consumer<TraceAndReverseFlow> _flowTraces;
    private final List<Hop> _hopsSoFar;
    private final NodeInterfacePair _lastHopNodeAndOutgoingInterface;
    private final Set<FirewallSessionTraceInfo> _newSessions;
    private final NavigableMap<String, IpSpace> _namedIpSpaces;
    private final Flow _originalFlow;

    // The current flow can change as we process the packet.
    private Flow _currentFlow;

    private TransmissionContext(
        Map<String, Configuration> configurations,
        String node,
        @Nullable String ingressInterface,
        Flow originalFlow,
        Consumer<TraceAndReverseFlow> flowTraces) {
      _configurations = configurations;
      _currentConfig = _configurations.get(node);
      _currentNode = new Node(node);
      _aclDefinitions = _currentConfig.getIpAccessLists();
      _flowTraces = flowTraces;
      _hopsSoFar = new ArrayList<>();
      _ingressInterface = ingressInterface;
      _lastHopNodeAndOutgoingInterface = null;
      _newSessions = new HashSet<>();
      _namedIpSpaces = _currentConfig.getIpSpaces();
      _originalFlow = originalFlow;
      _currentFlow = originalFlow;
    }

    private TransmissionContext(
        Map<String, Configuration> configurations,
        Node currentNode,
        @Nullable String ingressInterface,
        List<Hop> hopsSoFar,
        NodeInterfacePair lastHopNodeAndOutgoingInterface,
        Set<FirewallSessionTraceInfo> newSessions,
        Flow originalFlow,
        Flow currentFlow,
        Consumer<TraceAndReverseFlow> flowTraces) {
      _configurations = configurations;
      _currentNode = currentNode;
      _ingressInterface = ingressInterface;
      _lastHopNodeAndOutgoingInterface = lastHopNodeAndOutgoingInterface;
      _originalFlow = originalFlow;
      _currentFlow = currentFlow;
      _flowTraces = flowTraces;

      // essentially just cached values
      _currentConfig = configurations.get(currentNode.getName());
      _aclDefinitions = _currentConfig.getIpAccessLists();
      _namedIpSpaces = _currentConfig.getIpSpaces();

      // hops and sessions are per-trace.
      _hopsSoFar = new ArrayList<>(hopsSoFar);
      _newSessions = new HashSet<>(newSessions);
    }

    /** Creates a new TransmissionContext for the specified last-hop node and outgoing interface. */
    private TransmissionContext followEdge(Edge edge) {
      checkArgument(edge.getNode1().equals(_currentNode.getName()));
      return new TransmissionContext(
          _configurations,
          new Node(edge.getNode2()),
          edge.getInt2(),
          _hopsSoFar,
          edge.getTail(),
          _newSessions,
          _originalFlow,
          _currentFlow,
          _flowTraces);
    }

    private TransmissionContext branch() {
      return new TransmissionContext(
          _configurations,
          _currentNode,
          _ingressInterface,
          _hopsSoFar,
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
      TransmissionContext transmissionContext,
      List<Step<?>> steps,
      Stack<Breadcrumb> breadcrumbs) {
    checkArgument(!edges.isEmpty(), "No edges.");
    Ip arpIp =
        Route.UNSET_ROUTE_NEXT_HOP_IP.equals(nextHopIp)
            ? transmissionContext._currentFlow.getDstIp()
            : nextHopIp;

    if (!processArpFailure(outgoingInterface, arpIp, transmissionContext, steps)) {
      return;
    }

    steps.add(buildExitOutputIfaceStep(outgoingInterface, transmissionContext, TRANSMITTED));
    Hop hop = new Hop(transmissionContext._currentNode, steps);
    transmissionContext._hopsSoFar.add(hop);

    for (Edge edge : edges) {
      if (!edge.getNode1().equals(transmissionContext._currentNode.getName())) {
        continue;
      }
      checkState(edge.getInt1().equals(outgoingInterface), "Edge is not for outgoingInterface");
      String toNode = edge.getNode2();
      String toIface = edge.getInt2();
      if (isArpSuccessful(arpIp, _forwardingAnalysis, _configurations.get(toNode), toIface)) {
        processHop(transmissionContext.followEdge(edge), breadcrumbs);
      }
    }
  }

  @Nonnull
  private static ExitOutputIfaceStep buildExitOutputIfaceStep(
      String outputIface, TransmissionContext transmissionContext, StepAction action) {
    return ExitOutputIfaceStep.builder()
        .setDetail(
            ExitOutputIfaceStepDetail.builder()
                .setOutputInterface(
                    new NodeInterfacePair(transmissionContext._currentNode.getName(), outputIface))
                .setTransformedFlow(
                    hopFlow(transmissionContext._originalFlow, transmissionContext._currentFlow))
                .build())
        .setAction(action)
        .build();
  }

  /**
   * Checks ARP reply for {@param arpIp}. Returns whether someone replies. If not, also constructs
   * the trace.
   */
  private boolean processArpFailure(
      String outgoingInterfaceName,
      Ip arpIp,
      TransmissionContext transmissionContext,
      List<Step<?>> steps) {
    String currentNodeName = transmissionContext._currentNode.getName();
    String vrf =
        transmissionContext
            ._currentConfig
            .getAllInterfaces()
            .get(transmissionContext._ingressInterface)
            .getVrfName();
    // halt processing and add neighbor-unreachable trace if no one would respond
    if (_forwardingAnalysis
        .getNeighborUnreachableOrExitsNetwork()
        .get(currentNodeName)
        .get(vrf)
        .get(outgoingInterfaceName)
        .containsIp(arpIp, transmissionContext._currentConfig.getIpSpaces())) {
      FlowDisposition disposition =
          computeDisposition(
              currentNodeName, outgoingInterfaceName, transmissionContext._currentFlow.getDstIp());
      buildArpFailureTrace(outgoingInterfaceName, disposition, transmissionContext, steps);
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
              TransmissionContext transmissionContext =
                  new TransmissionContext(
                      _configurations,
                      ingressNodeName,
                      ingressInterfaceName,
                      flow,
                      currentTraces::add);
              processHop(transmissionContext, breadcrumbs);
            });
    return new TreeMap<>(traces);
  }

  private void processHop(TransmissionContext transmissionContext, Stack<Breadcrumb> breadcrumbs) {
    String currentNodeName = transmissionContext._currentNode.getName();
    String inputIfaceName = transmissionContext._ingressInterface;
    Configuration currentConfiguration = _configurations.get(currentNodeName);
    if (currentConfiguration == null) {
      throw new BatfishException(
          String.format(
              "Node %s is not in the network, cannot perform traceroute", currentNodeName));
    }
    if (inputIfaceName != null && processSessions(transmissionContext, breadcrumbs)) {
      // flow was processed by a session.
      return;
    }

    List<Step<?>> steps = new ArrayList<>();
    Map<String, IpAccessList> aclDefinitions = currentConfiguration.getIpAccessLists();
    NavigableMap<String, IpSpace> namedIpSpaces = currentConfiguration.getIpSpaces();

    // trace was received on a source interface of this hop
    if (inputIfaceName != null) {
      EnterInputIfaceStep enterIfaceStep =
          createEnterSrcIfaceStep(currentConfiguration, inputIfaceName);
      steps.add(enterIfaceStep);

      // apply ingress filter
      IpAccessList inputFilter =
          currentConfiguration.getAllInterfaces().get(inputIfaceName).getIncomingFilter();
      if (inputFilter != null) {
        if (applyFilter(inputFilter, INGRESS_FILTER, transmissionContext, steps) == DENIED) {
          return;
        }
      }

      TransformationResult transformationResult =
          TransformationEvaluator.eval(
              currentConfiguration
                  .getAllInterfaces()
                  .get(inputIfaceName)
                  .getIncomingTransformation(),
              transmissionContext._currentFlow,
              inputIfaceName,
              aclDefinitions,
              namedIpSpaces);
      steps.addAll(transformationResult.getTraceSteps());
      transmissionContext._currentFlow = transformationResult.getOutputFlow();
    } else if (transmissionContext._currentFlow.getIngressVrf() != null) {
      // if inputIfaceName is not set for this hop, this is the originating step
      steps.add(
          OriginateStep.builder()
              .setDetail(
                  OriginateStepDetail.builder()
                      .setOriginatingVrf(transmissionContext._currentFlow.getIngressVrf())
                      .build())
              .setAction(StepAction.ORIGINATED)
              .build());
    }

    Ip dstIp = transmissionContext._currentFlow.getDstIp();

    // Figure out where the trace came from..
    String vrfName;
    if (inputIfaceName == null) {
      vrfName = transmissionContext._currentFlow.getIngressVrf();
    } else {
      vrfName = currentConfiguration.getAllInterfaces().get(inputIfaceName).getVrfName();
    }
    checkNotNull(vrfName, "Missing VRF.");

    // Loop detection
    Breadcrumb breadcrumb =
        new Breadcrumb(currentNodeName, vrfName, transmissionContext._currentFlow);
    if (breadcrumbs.contains(breadcrumb)) {
      buildLoopTrace(transmissionContext, steps);
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
        buildAcceptTrace(transmissionContext, steps, vrfName);
        return;
      }

      // .. and what the next hops are based on the FIB.
      Fib currentFib = _fibs.get(currentNodeName).get(vrfName);
      Set<String> nextHopInterfaces = currentFib.getNextHopInterfaces(dstIp);
      if (nextHopInterfaces.isEmpty()) {
        buildNoRouteTrace(transmissionContext, steps);
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

      // For every interface with a route to the dst IP
      for (String nextHopInterfaceName : nextHopInterfaces) {
        Interface outgoingInterface =
            currentConfiguration.getAllInterfaces().get(nextHopInterfaceName);

        Multimap<Ip, AbstractRoute> resolvedNextHopIpRoutes =
            resolveNextHopIpRoutes(nextHopInterfaceName, nextHopInterfacesByRoute);
        resolvedNextHopIpRoutes
            .asMap()
            .forEach(
                (resolvedNextHopIp, routeCandidates) ->
                    forwardOutInterface(
                        outgoingInterface,
                        resolvedNextHopIp,
                        matchedRibRouteInfo,
                        transmissionContext.branch(),
                        breadcrumbs,
                        new ArrayList<>(steps)));
      }
    } finally {
      breadcrumbs.pop();
    }
  }

  /** add a step for NO_ROUTE from source to output interface */
  private static void buildNoRouteTrace(
      TransmissionContext transmissionContext, List<Step<?>> steps) {
    Builder routingStepBuilder = RoutingStep.builder();
    routingStepBuilder
        .setDetail(RoutingStepDetail.builder().build())
        .setAction(StepAction.NO_ROUTE);
    steps.add(routingStepBuilder.build());
    transmissionContext._hopsSoFar.add(new Hop(transmissionContext._currentNode, steps));
    Trace trace = new Trace(FlowDisposition.NO_ROUTE, transmissionContext._hopsSoFar);
    transmissionContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, null, transmissionContext._newSessions));
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
      List<RouteInfo> matchedRibRouteInfo,
      TransmissionContext transmissionContext,
      Stack<Breadcrumb> breadcrumbs,
      List<Step<?>> steps) {
    String currentNodeName = transmissionContext._currentNode.getName();
    String outgoingIfaceName = outgoingInterface.getName();
    NodeInterfacePair nextHopInterface = new NodeInterfacePair(currentNodeName, outgoingIfaceName);
    steps.add(
        RoutingStep.builder()
            .setDetail(RoutingStepDetail.builder().setRoutes(matchedRibRouteInfo).build())
            .setAction(StepAction.FORWARDED)
            .build());

    if (outgoingIfaceName.equals(Interface.NULL_INTERFACE_NAME)) {
      steps.add(
          ExitOutputIfaceStep.builder()
              .setDetail(
                  ExitOutputIfaceStepDetail.builder()
                      .setOutputInterface(
                          new NodeInterfacePair(currentNodeName, Interface.NULL_INTERFACE_NAME))
                      .build())
              .setAction(StepAction.NULL_ROUTED)
              .build());
      Trace trace =
          new Trace(
              FlowDisposition.NULL_ROUTED,
              ImmutableList.<Hop>builder()
                  .addAll(transmissionContext._hopsSoFar)
                  .add(new Hop(transmissionContext._currentNode, steps))
                  .build());
      transmissionContext._flowTraces.accept(
          new TraceAndReverseFlow(trace, null, transmissionContext._newSessions));
      return;
    }

    // Apply preSourceNatOutgoingFilter
    if (applyFilter(
            outgoingInterface.getPreTransformationOutgoingFilter(),
            PRE_SOURCE_NAT_FILTER,
            transmissionContext,
            steps)
        == DENIED) {
      return;
    }

    // Apply outgoing transformation
    Transformation transformation = outgoingInterface.getOutgoingTransformation();
    TransformationResult transformationResult =
        TransformationEvaluator.eval(
            transformation,
            transmissionContext._currentFlow,
            transmissionContext._ingressInterface,
            transmissionContext._aclDefinitions,
            transmissionContext._namedIpSpaces);
    steps.addAll(transformationResult.getTraceSteps());
    transmissionContext._currentFlow = transformationResult.getOutputFlow();

    // apply outgoing filter
    if (applyFilter(
            outgoingInterface.getOutgoingFilter(), EGRESS_FILTER, transmissionContext, steps)
        == DENIED) {
      return;
    }

    // setup session if necessary
    FirewallSessionInterfaceInfo firewallSessionInterfaceInfo =
        outgoingInterface.getFirewallSessionInterfaceInfo();
    if (firewallSessionInterfaceInfo != null) {
      transmissionContext._newSessions.add(
          buildFirewallSessionTraceInfo(transmissionContext, firewallSessionInterfaceInfo));
      steps.add(new SetupSessionStep());
    }

    SortedSet<Edge> edges = _dataPlane.getTopology().getInterfaceEdges().get(nextHopInterface);
    if (edges == null || edges.isEmpty()) {
      FlowDisposition disposition =
          computeDisposition(
              currentNodeName, outgoingIfaceName, transmissionContext._currentFlow.getDstIp());

      buildArpFailureTrace(outgoingIfaceName, disposition, transmissionContext, steps);
    } else {
      processOutgoingInterfaceEdges(
          outgoingIfaceName, nextHopIp, edges, transmissionContext, steps, breadcrumbs);
    }
  }

  @Nonnull
  private static FirewallSessionTraceInfo buildFirewallSessionTraceInfo(
      TransmissionContext transmissionContext,
      @Nonnull FirewallSessionInterfaceInfo firewallSessionInterfaceInfo) {
    return new FirewallSessionTraceInfo(
        transmissionContext._currentNode.getName(),
        transmissionContext._ingressInterface,
        transmissionContext._lastHopNodeAndOutgoingInterface,
        firewallSessionInterfaceInfo.getSessionInterfaces(),
        match5Tuple(
            transmissionContext._currentFlow.getDstIp(),
            transmissionContext._currentFlow.getDstPort(),
            transmissionContext._currentFlow.getSrcIp(),
            transmissionContext._currentFlow.getSrcPort(),
            transmissionContext._currentFlow.getIpProtocol()),
        sessionTransformation(transmissionContext._originalFlow, transmissionContext._currentFlow));
  }

  private static void buildAcceptTrace(
      TransmissionContext transmissionContext, List<Step<?>> steps, String vrfName) {
    InboundStep inboundStep =
        InboundStep.builder()
            .setAction(StepAction.ACCEPTED)
            .setDetail(new InboundStepDetail())
            .build();
    steps.add(inboundStep);
    transmissionContext._hopsSoFar.add(
        new Hop(transmissionContext._currentNode, ImmutableList.copyOf(steps)));
    Trace trace = new Trace(FlowDisposition.ACCEPTED, transmissionContext._hopsSoFar);
    Flow returnFlow =
        returnFlow(
            transmissionContext._currentFlow,
            transmissionContext._currentNode.getName(),
            vrfName,
            null);
    transmissionContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, returnFlow, transmissionContext._newSessions));
  }

  private static void buildLoopTrace(TransmissionContext transmissionContext, List<Step<?>> steps) {
    transmissionContext._hopsSoFar.add(
        new Hop(transmissionContext._currentNode, ImmutableList.copyOf(steps)));
    Trace trace = new Trace(FlowDisposition.LOOP, transmissionContext._hopsSoFar);
    transmissionContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, null, transmissionContext._newSessions));
  }

  /**
   * Apply a filter, and create the corresponding step. If the filter DENIED the flow, then create a
   * trace ending in the denial. Return the action if the filter is non-null. If the filter is null,
   * return null.
   */
  private @Nullable StepAction applyFilter(
      @Nullable IpAccessList filter,
      FilterType filterType,
      TransmissionContext transmissionContext,
      List<Step<?>> steps) {
    if (filter == null) {
      return null;
    }
    FilterStep filterStep =
        createFilterStep(
            transmissionContext._currentFlow,
            transmissionContext._ingressInterface,
            filter,
            filterType,
            transmissionContext._aclDefinitions,
            transmissionContext._namedIpSpaces,
            _ignoreFilters);
    steps.add(filterStep);
    if (filterStep.getAction() == DENIED) {
      transmissionContext._hopsSoFar.add(
          new Hop(transmissionContext._currentNode, ImmutableList.copyOf(steps)));
      Trace trace = new Trace(filterType.deniedDisposition(), transmissionContext._hopsSoFar);
      transmissionContext._flowTraces.accept(new TraceAndReverseFlow(trace, null));
    }
    return filterStep.getAction();
  }

  /**
   * Check this {@param flow} matches a session on this device. If so, process the flow. Returns
   * true if the flow is matched/processed.
   */
  private boolean processSessions(
      TransmissionContext transmissionContext, Stack<Breadcrumb> breadcrumbs) {
    String currentNodeName = transmissionContext._currentNode.getName();
    String inputIfaceName = transmissionContext._ingressInterface;
    Flow flow = transmissionContext._currentFlow;
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

    List<Step<?>> steps = new ArrayList<>();
    steps.add(new MatchSessionStep());

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
        && applyFilter(
                ipAccessLists.get(incomingAclName),
                FilterType.INGRESS_FILTER,
                transmissionContext,
                steps)
            == DENIED) {
      return true;
    }

    // cycle detection
    String vrf = incomingInterface.getVrfName();
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, vrf, flow);
    if (breadcrumbs.contains(breadcrumb)) {
      buildLoopTrace(transmissionContext, steps);
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
        steps.addAll(result.getTraceSteps());
        transmissionContext._currentFlow = result.getOutputFlow();
      }
      if (session.getOutgoingInterface() == null) {
        // Accepted by this node (vrf of incoming interface).
        buildAcceptTrace(transmissionContext, steps, vrf);
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
          && applyFilter(
                  ipAccessLists.get(outgoingAclName),
                  FilterType.EGRESS_FILTER,
                  transmissionContext,
                  steps)
              == DENIED) {
        return true;
      }

      if (session.getNextHop() == null) {
        // Delivered to subnet/exits network/insufficient info.
        buildArpFailureTrace(
            session.getOutgoingInterface(),
            FlowDisposition.EXITS_NETWORK,
            transmissionContext,
            steps);
        return true;
      }

      steps.add(
          buildExitOutputIfaceStep(outgoingInterface.getName(), transmissionContext, TRANSMITTED));
      transmissionContext._hopsSoFar.add(new Hop(new Node(currentNodeName), steps));

      // Forward to neighbor.
      processHop(
          transmissionContext.followEdge(
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
      String outInterface,
      FlowDisposition disposition,
      TransmissionContext transmissionContext,
      List<Step<?>> steps) {
    String currentNodeName = transmissionContext._currentNode.getName();
    steps.add(
        buildExitOutputIfaceStep(
            outInterface, transmissionContext, getFinalActionForDisposition(disposition)));

    transmissionContext._hopsSoFar.add(new Hop(transmissionContext._currentNode, steps));

    Flow returnFlow =
        SUCCESS_DISPOSITIONS.contains(disposition)
            ? returnFlow(transmissionContext._currentFlow, currentNodeName, null, outInterface)
            : null;

    Trace trace = new Trace(disposition, transmissionContext._hopsSoFar);
    transmissionContext._flowTraces.accept(
        new TraceAndReverseFlow(trace, returnFlow, transmissionContext._newSessions));
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
