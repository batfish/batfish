package org.batfish.dataplane.traceroute;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match5Tuple;
import static org.batfish.datamodel.flow.FilterStep.FilterType.EGRESS_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.INGRESS_FILTER;
import static org.batfish.datamodel.flow.FilterStep.FilterType.PRE_SOURCE_NAT_FILTER;
import static org.batfish.datamodel.flow.StepAction.DENIED;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
    private final Map<String, IpAccessList> _aclDefinitions;
    private final Node _currentNode;
    private final List<TraceAndReverseFlow> _flowTraces;
    private final List<Hop> _hopsSoFar;
    private final NodeInterfacePair _lastHopNodeAndOutgoingInterface;
    private final Set<FirewallSessionTraceInfo> _newSessions;
    private final NavigableMap<String, IpSpace> _namedIpSpaces;
    private final Flow _originalFlow;
    private final Flow _transformedFlow;

    private TransmissionContext(
        Map<String, IpAccessList> aclDefinitions,
        Node currentNode,
        List<TraceAndReverseFlow> flowTraces,
        List<Hop> hopsSoFar,
        @Nullable NodeInterfacePair lastHopNodeAndOutgoingInterface,
        Set<FirewallSessionTraceInfo> newSessions,
        NavigableMap<String, IpSpace> namedIpSpaces,
        Flow originalFlow,
        Flow transformedFlow) {
      _aclDefinitions = aclDefinitions;
      _currentNode = currentNode;
      _flowTraces = flowTraces;
      _hopsSoFar = new ArrayList<>(hopsSoFar);
      _lastHopNodeAndOutgoingInterface = lastHopNodeAndOutgoingInterface;
      _newSessions = new HashSet<>(newSessions);
      _namedIpSpaces = namedIpSpaces;
      _originalFlow = originalFlow;
      _transformedFlow = transformedFlow;
    }

    /** Creates a new TransmissionContext for the specified last-hop node and outgoing interface. */
    private TransmissionContext branch(
        NodeInterfacePair lastHopNodeAndOutgoingInterface, String currentHop) {
      TransmissionContext transmissionContext =
          new TransmissionContext(
              _aclDefinitions,
              new Node(currentHop),
              _flowTraces,
              _hopsSoFar,
              lastHopNodeAndOutgoingInterface,
              _newSessions,
              _namedIpSpaces,
              _originalFlow,
              _transformedFlow);
      return transmissionContext;
    }
  }

  private final Map<String, Configuration> _configurations;
  private final DataPlane _dataPlane;
  private final Map<String, Map<String, Fib>> _fibs;
  private final Set<Flow> _flows;
  private final ForwardingAnalysis _forwardingAnalysis;
  private final boolean _ignoreFilters;

  public TracerouteEngineImplContext(
      DataPlane dataPlane,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreFilters) {
    _configurations = dataPlane.getConfigurations();
    _dataPlane = dataPlane;
    _flows = flows;
    _fibs = fibs;
    _ignoreFilters = ignoreFilters;
    _forwardingAnalysis = _dataPlane.getForwardingAnalysis();
  }

  private void processCurrentNextHopInterfaceEdges(
      String currentNodeName,
      Ip dstIp,
      String nextHopInterfaceName,
      @Nullable Ip finalNextHopIp,
      SortedSet<Edge> edges,
      TransmissionContext transmissionContext,
      List<Step<?>> steps,
      Stack<Breadcrumb> breadcrumbs) {
    if (!processFlowTransmission(
        currentNodeName, dstIp, nextHopInterfaceName, finalNextHopIp, transmissionContext, steps)) {
      return;
    }

    steps.add(
        ExitOutputIfaceStep.builder()
            .setDetail(
                ExitOutputIfaceStepDetail.builder()
                    .setOutputInterface(
                        new NodeInterfacePair(currentNodeName, nextHopInterfaceName))
                    .setTransformedFlow(
                        hopFlow(
                            transmissionContext._originalFlow,
                            transmissionContext._transformedFlow))
                    .build())
            .setAction(StepAction.TRANSMITTED)
            .build());
    Hop hop = new Hop(new Node(currentNodeName), steps);
    transmissionContext._hopsSoFar.add(hop);
    for (Edge edge : edges) {
      String fromNode = edge.getNode1();
      if (!fromNode.equals(currentNodeName)) {
        continue;
      }
      String fromIface = edge.getInt1();
      String toNode = edge.getNode2();
      String toIface = edge.getInt2();
      Ip arpIp = finalNextHopIp != null ? finalNextHopIp : dstIp;
      if (isArpSuccessful(arpIp, _forwardingAnalysis, _configurations.get(toNode), toIface)) {
        processHop(
            toIface,
            transmissionContext.branch(new NodeInterfacePair(fromNode, fromIface), toNode),
            transmissionContext._transformedFlow,
            breadcrumbs);
      }
    }
  }

  private boolean processFlowTransmission(
      String currentNodeName,
      Ip dstIp,
      String nextHopInterfaceName,
      @Nullable Ip finalNextHopIp,
      TransmissionContext transmissionContext,
      List<Step<?>> steps) {
    Ip arpIp = finalNextHopIp != null ? finalNextHopIp : dstIp;
    Configuration c = _configurations.get(transmissionContext._currentNode.getName());
    // halt processing and add neighbor-unreachable trace if no one would respond
    if (_forwardingAnalysis
        .getNeighborUnreachableOrExitsNetwork()
        .get(transmissionContext._currentNode.getName())
        .get(c.getAllInterfaces().get(nextHopInterfaceName).getVrfName())
        .get(nextHopInterfaceName)
        .containsIp(arpIp, c.getIpSpaces())) {
      FlowDisposition disposition =
          computeDisposition(
              currentNodeName,
              nextHopInterfaceName,
              transmissionContext._transformedFlow.getDstIp());

      steps.add(
          ExitOutputIfaceStep.builder()
              .setDetail(
                  ExitOutputIfaceStepDetail.builder()
                      .setOutputInterface(
                          new NodeInterfacePair(currentNodeName, nextHopInterfaceName))
                      .setTransformedFlow(
                          hopFlow(
                              transmissionContext._originalFlow,
                              transmissionContext._transformedFlow))
                      .build())
              .setAction(getFinalActionForDisposition(disposition))
              .build());

      Hop terminalHop = new Hop(new Node(currentNodeName), steps);

      transmissionContext._hopsSoFar.add(terminalHop);
      Trace trace = new Trace(disposition, transmissionContext._hopsSoFar);

      /* This trace ends in an ARP failure, which could be considered either a success
       * or a failure. Only create return flow when it's a success.
       */
      Flow returnFlow =
          SUCCESS_DISPOSITIONS.contains(disposition)
              ? returnFlow(
                  transmissionContext._transformedFlow, currentNodeName, null, nextHopInterfaceName)
              : null;

      transmissionContext._flowTraces.add(
          new TraceAndReverseFlow(trace, returnFlow, transmissionContext._newSessions));

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
              List<Hop> hops = new ArrayList<>();
              Stack<Breadcrumb> breadcrumbs = new Stack<>();
              String ingressInterfaceName = flow.getIngressInterface();
              if (ingressInterfaceName != null) {
                TransmissionContext transmissionContext =
                    new TransmissionContext(
                        Maps.newHashMap(),
                        new Node(ingressNodeName),
                        currentTraces,
                        hops,
                        null,
                        Sets.newHashSet(),
                        Maps.newTreeMap(),
                        flow,
                        flow);
                processHop(ingressInterfaceName, transmissionContext, flow, breadcrumbs);
              } else {
                TransmissionContext transmissionContext =
                    new TransmissionContext(
                        Maps.newHashMap(),
                        new Node(flow.getIngressNode()),
                        currentTraces,
                        hops,
                        null,
                        Sets.newHashSet(),
                        Maps.newTreeMap(),
                        flow,
                        flow);
                processHop(null, transmissionContext, flow, breadcrumbs);
              }
            });
    return new TreeMap<>(traces);
  }

  private void processHop(
      @Nullable String inputIfaceName,
      TransmissionContext transmissionContext,
      Flow inputFlow,
      Stack<Breadcrumb> breadcrumbs) {
    String currentNodeName = transmissionContext._currentNode.getName();
    List<Step<?>> steps = new ArrayList<>();
    Configuration currentConfiguration = _configurations.get(currentNodeName);
    if (currentConfiguration == null) {
      throw new BatfishException(
          String.format(
              "Node %s is not in the network, cannot perform traceroute", currentNodeName));
    }

    Map<String, IpAccessList> aclDefinitions = currentConfiguration.getIpAccessLists();
    NavigableMap<String, IpSpace> namedIpSpaces = currentConfiguration.getIpSpaces();

    // trace was received on a source interface of this hop
    Flow dstNatFlow = null;
    if (inputIfaceName != null) {
      EnterInputIfaceStep enterIfaceStep =
          createEnterSrcIfaceStep(currentConfiguration, inputIfaceName);
      steps.add(enterIfaceStep);

      // apply ingress filter
      IpAccessList inputFilter =
          currentConfiguration.getAllInterfaces().get(inputIfaceName).getIncomingFilter();
      if (inputFilter != null) {
        if (applyFilter(
                inputFilter,
                INGRESS_FILTER,
                inputFlow,
                inputIfaceName,
                aclDefinitions,
                namedIpSpaces,
                transmissionContext,
                steps)
            == DENIED) {
          return;
        }
      }

      TransformationResult transformationResult =
          TransformationEvaluator.eval(
              currentConfiguration
                  .getAllInterfaces()
                  .get(inputIfaceName)
                  .getIncomingTransformation(),
              inputFlow,
              inputIfaceName,
              aclDefinitions,
              namedIpSpaces);
      dstNatFlow = transformationResult.getOutputFlow();
      steps.addAll(transformationResult.getTraceSteps());
    } else if (inputFlow.getIngressVrf() != null) {
      // if inputIfaceName is not set for this hop, this is the originating step
      steps.add(
          OriginateStep.builder()
              .setDetail(
                  OriginateStepDetail.builder()
                      .setOriginatingVrf(inputFlow.getIngressVrf())
                      .build())
              .setAction(StepAction.ORIGINATED)
              .build());
    }

    Flow currentFlow = firstNonNull(dstNatFlow, inputFlow);
    Ip dstIp = currentFlow.getDstIp();

    // Figure out where the trace came from..
    String vrfName;
    if (inputIfaceName == null) {
      vrfName = currentFlow.getIngressVrf();
    } else {
      vrfName = currentConfiguration.getAllInterfaces().get(inputIfaceName).getVrfName();
    }

    // Loop detection
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, vrfName, currentFlow);
    if (breadcrumbs.contains(breadcrumb)) {
      Hop loopHop = new Hop(new Node(currentNodeName), ImmutableList.copyOf(steps));
      transmissionContext._hopsSoFar.add(loopHop);
      Trace trace = new Trace(FlowDisposition.LOOP, transmissionContext._hopsSoFar);
      transmissionContext._flowTraces.add(
          new TraceAndReverseFlow(trace, null, transmissionContext._newSessions));
      return;
    }

    breadcrumbs.push(breadcrumb);
    // use try/finally to make sure we pop off the breadcrumb
    try {
      // Accept if the flow is destined for this vrf on this host.
      if (_dataPlane
          .getIpVrfOwners()
          .getOrDefault(currentFlow.getDstIp(), ImmutableMap.of())
          .getOrDefault(currentConfiguration.getHostname(), ImmutableSet.of())
          .contains(vrfName)) {
        InboundStep inboundStep =
            InboundStep.builder()
                .setAction(StepAction.ACCEPTED)
                .setDetail(new InboundStepDetail())
                .build();
        steps.add(inboundStep);
        Hop acceptedHop = new Hop(new Node(currentNodeName), ImmutableList.copyOf(steps));
        transmissionContext._hopsSoFar.add(acceptedHop);
        Trace trace = new Trace(FlowDisposition.ACCEPTED, transmissionContext._hopsSoFar);
        Flow returnFlow = returnFlow(currentFlow, currentNodeName, vrfName, null);
        transmissionContext._flowTraces.add(
            new TraceAndReverseFlow(trace, returnFlow, transmissionContext._newSessions));
        return;
      }

      // .. and what the next hops are based on the FIB.
      Fib currentFib = _fibs.get(currentNodeName).get(vrfName);
      Set<String> nextHopInterfaces = currentFib.getNextHopInterfaces(dstIp);
      if (nextHopInterfaces.isEmpty()) {
        // add a step for  NO_ROUTE from source to output interface
        Builder routingStepBuilder = RoutingStep.builder();
        routingStepBuilder
            .setDetail(RoutingStepDetail.builder().build())
            .setAction(StepAction.NO_ROUTE);
        steps.add(routingStepBuilder.build());
        Hop noRouteHop = new Hop(new Node(currentNodeName), ImmutableList.copyOf(steps));
        transmissionContext._hopsSoFar.add(noRouteHop);
        Trace trace = new Trace(FlowDisposition.NO_ROUTE, transmissionContext._hopsSoFar);
        transmissionContext._flowTraces.add(
            new TraceAndReverseFlow(trace, null, transmissionContext._newSessions));
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

        NodeInterfacePair nextHopInterface =
            new NodeInterfacePair(currentNodeName, nextHopInterfaceName);
        resolvedNextHopWithRoutes
            .asMap()
            .forEach(
                (resolvedNextHopIp, routeCandidates) -> {
                  // Later parts of the stack expect null instead of unset to trigger proxy arp,
                  // etc.
                  Ip finalNextHopIp =
                      Route.UNSET_ROUTE_NEXT_HOP_IP.equals(resolvedNextHopIp)
                          ? null
                          : resolvedNextHopIp;

                  List<Step<?>> clonedSteps = new ArrayList<>(steps);
                  clonedSteps.add(
                      RoutingStep.builder()
                          .setDetail(
                              RoutingStepDetail.builder().setRoutes(matchedRibRouteInfo).build())
                          .setAction(StepAction.FORWARDED)
                          .build());

                  if (nextHopInterfaceName.equals(Interface.NULL_INTERFACE_NAME)) {
                    clonedSteps.add(
                        ExitOutputIfaceStep.builder()
                            .setDetail(
                                ExitOutputIfaceStepDetail.builder()
                                    .setOutputInterface(
                                        new NodeInterfacePair(
                                            currentNodeName, Interface.NULL_INTERFACE_NAME))
                                    .build())
                            .setAction(StepAction.NULL_ROUTED)
                            .build());
                    Hop nullRoutedHop = new Hop(new Node(currentNodeName), clonedSteps);
                    Trace trace =
                        new Trace(
                            FlowDisposition.NULL_ROUTED,
                            ImmutableList.<Hop>builder()
                                .addAll(transmissionContext._hopsSoFar)
                                .add(nullRoutedHop)
                                .build());
                    transmissionContext._flowTraces.add(
                        new TraceAndReverseFlow(trace, null, transmissionContext._newSessions));
                    return;
                  }

                  Interface outgoingInterface =
                      _configurations
                          .get(nextHopInterface.getHostname())
                          .getAllInterfaces()
                          .get(nextHopInterface.getInterface());

                  // Apply preSourceNatOutgoingFilter
                  if (applyFilter(
                          outgoingInterface.getPreTransformationOutgoingFilter(),
                          PRE_SOURCE_NAT_FILTER,
                          currentFlow,
                          inputIfaceName,
                          aclDefinitions,
                          namedIpSpaces,
                          transmissionContext,
                          clonedSteps)
                      == DENIED) {
                    return;
                  }

                  // Apply outgoing transformation
                  Transformation transformation = outgoingInterface.getOutgoingTransformation();
                  TransformationResult transformationResult =
                      TransformationEvaluator.eval(
                          transformation,
                          currentFlow,
                          inputIfaceName,
                          aclDefinitions,
                          namedIpSpaces);
                  Flow newTransformedFlow = transformationResult.getOutputFlow();
                  clonedSteps.addAll(transformationResult.getTraceSteps());

                  SortedSet<Edge> edges =
                      _dataPlane.getTopology().getInterfaceEdges().get(nextHopInterface);

                  TransmissionContext clonedTransmissionContext =
                      new TransmissionContext(
                          aclDefinitions,
                          new Node(currentNodeName),
                          transmissionContext._flowTraces,
                          transmissionContext._hopsSoFar,
                          transmissionContext._lastHopNodeAndOutgoingInterface,
                          transmissionContext._newSessions,
                          namedIpSpaces,
                          currentFlow,
                          newTransformedFlow);

                  // apply outgoing filter
                  if (applyFilter(
                          outgoingInterface.getOutgoingFilter(),
                          EGRESS_FILTER,
                          clonedTransmissionContext._transformedFlow,
                          inputIfaceName,
                          aclDefinitions,
                          namedIpSpaces,
                          transmissionContext,
                          clonedSteps)
                      == DENIED) {
                    return;
                  }

                  // setup session if necessary
                  FirewallSessionInterfaceInfo firewallSessionInterfaceInfo =
                      outgoingInterface.getFirewallSessionInterfaceInfo();
                  if (firewallSessionInterfaceInfo != null) {
                    clonedTransmissionContext._newSessions.add(
                        new FirewallSessionTraceInfo(
                            currentNodeName,
                            inputIfaceName,
                            clonedTransmissionContext._lastHopNodeAndOutgoingInterface,
                            firewallSessionInterfaceInfo.getSessionInterfaces(),
                            match5Tuple(
                                newTransformedFlow.getDstIp(),
                                newTransformedFlow.getDstPort(),
                                newTransformedFlow.getSrcIp(),
                                newTransformedFlow.getSrcPort(),
                                newTransformedFlow.getIpProtocol()),
                            sessionTransformation(inputFlow, newTransformedFlow)));
                    steps.add(new SetupSessionStep());
                  }

                  if (edges == null || edges.isEmpty()) {
                    updateUnreachableTrace(
                        currentNodeName, outgoingInterface, clonedTransmissionContext, clonedSteps);
                  } else {
                    processCurrentNextHopInterfaceEdges(
                        currentNodeName,
                        dstIp,
                        nextHopInterfaceName,
                        finalNextHopIp,
                        edges,
                        clonedTransmissionContext,
                        clonedSteps,
                        breadcrumbs);
                  }
                });
      }
    } finally {
      breadcrumbs.pop();
    }
  }

  /**
   * Apply a filter, and create the corresponding step. If the filter DENIED the flow, then create a
   * trace ending in the denial. Return the action if the filter is non-null. If the filter is null,
   * return null.
   */
  private @Nullable StepAction applyFilter(
      @Nullable IpAccessList filter,
      FilterType filterType,
      Flow flow,
      @Nullable String inputIfaceName,
      Map<String, IpAccessList> aclDefinitions,
      NavigableMap<String, IpSpace> namedIpSpaces,
      TransmissionContext transmissionContext,
      List<Step<?>> steps) {
    if (filter == null) {
      return null;
    }
    FilterStep filterStep =
        createFilterStep(
            flow,
            inputIfaceName,
            filter,
            filterType,
            aclDefinitions,
            namedIpSpaces,
            _ignoreFilters);
    steps.add(filterStep);
    if (filterStep.getAction() == DENIED) {
      Hop deniedOutHop = new Hop(transmissionContext._currentNode, ImmutableList.copyOf(steps));
      Trace trace =
          new Trace(
              filterType.deniedDisposition(),
              ImmutableList.<Hop>builder()
                  .addAll(transmissionContext._hopsSoFar)
                  .add(deniedOutHop)
                  .build());
      transmissionContext._flowTraces.add(new TraceAndReverseFlow(trace, null));
    }
    return filterStep.getAction();
  }

  /**
   * Check this {@param flow} matches a session on this device. If so, process the flow. Returns
   * true if the flow is matched/processed.
   */
  private boolean processSessions(
      String currentNodeName,
      String inputIfaceName,
      TransmissionContext transmissionContext,
      Flow flow,
      Stack<Breadcrumb> breadcrumbs) {
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
                flow,
                inputIfaceName,
                ipAccessLists,
                ipSpaces,
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
      Flow postTransformationFlow = flow;
      if (transformation != null) {
        TransformationResult result =
            TransformationEvaluator.eval(
                transformation, flow, inputIfaceName, ipAccessLists, ipSpaces);
        postTransformationFlow = result.getOutputFlow();
        steps.addAll(result.getTraceSteps());
      }
      if (session.getOutgoingInterface() == null) {
        // Accepted by this node (vrf of incoming interface).
        buildAcceptTrace(transmissionContext, steps, flow, vrf);
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
                  postTransformationFlow,
                  inputIfaceName,
                  ipAccessLists,
                  ipSpaces,
                  transmissionContext,
                  steps)
              == DENIED) {
        return true;
      }

      if (session.getNextHop() == null) {
        // Delivered to subnet/exits network/insufficient info.
        buildSessionArpFailureTrace(session.getOutgoingInterface(), transmissionContext, steps);
        return true;
      }

      steps.add(
          buildExitOutputIfaceStep(outgoingInterface.getName(), transmissionContext, TRANSMITTED));
      transmissionContext._hopsSoFar.add(new Hop(new Node(currentNodeName), steps));

      // Forward to neighbor.
      processHop(
          session.getNextHop().getInterface(),
          transmissionContext.branch(
              new NodeInterfacePair(currentNodeName, session.getOutgoingInterface()),
              session.getNextHop().getHostname()),
          postTransformationFlow,
          breadcrumbs);
      return true;
    } finally {
      breadcrumbs.pop();
    }
  }

  /**
   * We can't use {@link TracerouteEngineImplContext#buildArpFailureTrace} for sessions, because
   * forwarding analysis disposition maps currently include routing conditions, which do not apply
   * to sessions.
   *
   * <p>This ARP failure situation only arises for sessions that have an outgoing interface but no
   * next hop. This means the user started the trace entering the outgoing interface. For now, just
   * always call this EXITS_NETWORK. In the future we may want to apply the normal ARP error
   * disposition logic, which would require factoring out routing-independent disposition maps in
   * forwarding analysis.
   */
  private void buildSessionArpFailureTrace(
      String outgoingInterfaceName, TransmissionContext transmissionContext, List<Step<?>> steps) {
    Interface outgoingInterface =
        _configurations
            .get(transmissionContext._currentNode.getName())
            .getAllInterfaces()
            .get(outgoingInterfaceName);
    buildArpFailureTrace(
        outgoingInterface, FlowDisposition.EXITS_NETWORK, transmissionContext, steps);
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

  private void updateUnreachableTrace(
      String currentNodeName,
      Interface outInterface,
      TransmissionContext transmissionContext,
      List<Step<?>> steps) {
    ExitOutputIfaceStep.Builder exitOutIfaceBuilder = ExitOutputIfaceStep.builder();
    exitOutIfaceBuilder.setDetail(
        ExitOutputIfaceStepDetail.builder()
            .setOutputInterface(new NodeInterfacePair(currentNodeName, outInterface.getName()))
            .setTransformedFlow(
                hopFlow(transmissionContext._originalFlow, transmissionContext._transformedFlow))
            .build());
    Trace trace;
    FlowDisposition disposition =
        computeDisposition(
            currentNodeName,
            outInterface.getName(),
            transmissionContext._transformedFlow.getDstIp());

    // create appropriate step
    exitOutIfaceBuilder.setAction(getFinalActionForDisposition(disposition));
    steps.add(exitOutIfaceBuilder.build());

    Hop terminalHop = new Hop(new Node(currentNodeName), steps);

    transmissionContext._hopsSoFar.add(terminalHop);
    trace = new Trace(disposition, transmissionContext._hopsSoFar);

    Flow returnFlow =
        SUCCESS_DISPOSITIONS.contains(disposition)
            ? returnFlow(
                transmissionContext._transformedFlow, currentNodeName, null, outInterface.getName())
            : null;

    transmissionContext._flowTraces.add(
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
