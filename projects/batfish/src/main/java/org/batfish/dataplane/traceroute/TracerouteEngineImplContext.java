package org.batfish.dataplane.traceroute;

import static org.batfish.dataplane.traceroute.TracerouteUtils.createEnterSrcIfaceStep;
import static org.batfish.dataplane.traceroute.TracerouteUtils.getFinalActionForDisposition;
import static org.batfish.dataplane.traceroute.TracerouteUtils.isArpSuccessful;
import static org.batfish.dataplane.traceroute.TracerouteUtils.validateInputs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import java.util.ArrayList;
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.TransformationList;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.InboundStep;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;
import org.batfish.datamodel.flow.OriginateStep;
import org.batfish.datamodel.flow.OriginateStep.OriginateStepDetail;
import org.batfish.datamodel.flow.RouteInfo;
import org.batfish.datamodel.flow.RoutingStep;
import org.batfish.datamodel.flow.RoutingStep.Builder;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.transformation.Transformation.Direction;

/**
 * Class containing an implementation of {@link
 * org.batfish.dataplane.TracerouteEngineImpl#buildFlows(DataPlane, Set, Map, boolean)} and the
 * context (data) needed for it
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
    private String _filterOutNotes;
    private final List<Trace> _flowTraces;
    private final List<Hop> _hopsSoFar;
    private final NavigableMap<String, IpSpace> _namedIpSpaces;
    private final Flow _originalFlow;
    private final Flow _transformedFlow;

    private TransmissionContext(
        Map<String, IpAccessList> aclDefinitions,
        Node currentNode,
        List<Trace> flowTraces,
        List<Hop> hopsSoFar,
        NavigableMap<String, IpSpace> namedIpSpaces,
        Flow originalFlow,
        Flow transformedFlow) {
      _aclDefinitions = aclDefinitions;
      _currentNode = currentNode;
      _flowTraces = flowTraces;
      _hopsSoFar = new ArrayList<>(hopsSoFar);
      _namedIpSpaces = namedIpSpaces;
      _originalFlow = originalFlow;
      _transformedFlow = transformedFlow;
    }

    private TransmissionContext branch() {
      TransmissionContext transmissionContext =
          new TransmissionContext(
              _aclDefinitions,
              _currentNode,
              _flowTraces,
              _hopsSoFar,
              _namedIpSpaces,
              _originalFlow,
              _transformedFlow);
      transmissionContext._filterOutNotes = _filterOutNotes;
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

  /**
   * Applies the given list of NAT rules to the given flow and returns the new transformed flow. If
   * {@code egressNats} is null, empty, or does not contain any ACL rules matching the {@link Flow},
   * the original flow is returned.
   *
   * <p>Each {@link Transformation} is expected to be valid
   */
  @VisibleForTesting
  static Flow applyEgressNats(
      Flow flow,
      @Nullable String srcInterface,
      Map<String, IpAccessList> aclDefinitions,
      Map<String, IpSpace> namedIpSpaces,
      @Nullable TransformationList egressNats) {
    if (egressNats != null) {
      return egressNats.apply(flow, Direction.EGRESS, srcInterface, aclDefinitions, namedIpSpaces);
    }
    return flow;
  }

  /**
   * Applies the given list of NAT rules to the given flow and returns the new transformed flow. If
   * {@code ingressNats} is null, empty, or does not contain any ACL rules matching the {@link
   * Flow}, the original flow is returned.
   *
   * <p>Each {@link Transformation} is expected to be valid
   */
  private static Flow applyIngressNats(
      Flow flow,
      Map<String, IpAccessList> aclDefinitions,
      NavigableMap<String, IpSpace> namedIpSpaces,
      @Nullable TransformationList ingressNats) {
    if (ingressNats != null) {
      return ingressNats.apply(flow, Direction.INGRESS, null, aclDefinitions, namedIpSpaces);
    } else {
      return flow;
    }
  }

  private void processCurrentNextHopInterfaceEdges(
      String currentNodeName,
      @Nullable String srcInterface,
      Ip dstIp,
      String nextHopInterfaceName,
      @Nullable Ip finalNextHopIp,
      SortedSet<Edge> edges,
      TransmissionContext transmissionContext,
      ImmutableList.Builder<Step<?>> stepBuilder,
      Stack<Breadcrumb> breadcrumbs) {
    if (!processFlowTransmission(
        currentNodeName,
        srcInterface,
        dstIp,
        nextHopInterfaceName,
        finalNextHopIp,
        transmissionContext,
        stepBuilder)) {
      return;
    }
    IpAccessList outFilter =
        _configurations
            .get(transmissionContext._currentNode.getName())
            .getAllInterfaces()
            .get(nextHopInterfaceName)
            .getOutgoingFilter();
    stepBuilder.add(
        ExitOutputIfaceStep.builder()
            .setDetail(
                ExitOutputIfaceStepDetail.builder()
                    .setOutputInterface(
                        new NodeInterfacePair(currentNodeName, nextHopInterfaceName))
                    .setOutputFilter(outFilter != null ? outFilter.getName() : null)
                    .setTransformedFlow(
                        hopFlow(
                            transmissionContext._originalFlow,
                            transmissionContext._transformedFlow))
                    .build())
            .setAction(StepAction.TRANSMITTED)
            .build());
    Hop hop = new Hop(new Node(currentNodeName), stepBuilder.build());
    transmissionContext._hopsSoFar.add(hop);
    for (Edge edge : edges) {
      if (!edge.getNode1().equals(currentNodeName)) {
        continue;
      }
      if (isArpSuccessful(
          finalNextHopIp != null ? finalNextHopIp : dstIp,
          _forwardingAnalysis,
          _configurations.get(edge.getNode2()),
          edge.getInt2())) {
        processHop(
            edge.getNode2(),
            edge.getInt2(),
            transmissionContext,
            transmissionContext._transformedFlow,
            breadcrumbs);
      }
    }
  }

  private boolean processFlowTransmission(
      String currentNodeName,
      @Nullable String srcInterface,
      Ip dstIp,
      String nextHopInterfaceName,
      @Nullable Ip finalNextHopIp,
      TransmissionContext transmissionContext,
      ImmutableList.Builder<Step<?>> stepBuilder) {
    // check output filter
    IpAccessList outFilter =
        _configurations
            .get(transmissionContext._currentNode.getName())
            .getAllInterfaces()
            .get(nextHopInterfaceName)
            .getOutgoingFilter();
    if (!_ignoreFilters && outFilter != null) {
      FilterResult filterResult =
          outFilter.filter(
              transmissionContext._transformedFlow,
              srcInterface,
              transmissionContext._aclDefinitions,
              transmissionContext._namedIpSpaces);
      if (filterResult.getAction() == LineAction.DENY) {
        stepBuilder.add(
            ExitOutputIfaceStep.builder()
                .setDetail(
                    ExitOutputIfaceStepDetail.builder()
                        .setOutputInterface(
                            new NodeInterfacePair(currentNodeName, nextHopInterfaceName))
                        .setOutputFilter(outFilter.getName())
                        .setTransformedFlow(
                            hopFlow(
                                transmissionContext._originalFlow,
                                transmissionContext._transformedFlow))
                        .build())
                .setAction(StepAction.DENIED)
                .build());
        Hop deniedOutHop = new Hop(new Node(currentNodeName), stepBuilder.build());
        transmissionContext._hopsSoFar.add(deniedOutHop);
        Trace trace = new Trace(FlowDisposition.DENIED_OUT, transmissionContext._hopsSoFar);
        transmissionContext._flowTraces.add(trace);
        return false;
      }
    }
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

      stepBuilder.add(
          ExitOutputIfaceStep.builder()
              .setDetail(
                  ExitOutputIfaceStepDetail.builder()
                      .setOutputInterface(
                          new NodeInterfacePair(currentNodeName, nextHopInterfaceName))
                      .setOutputFilter(outFilter != null ? outFilter.getName() : null)
                      .setTransformedFlow(
                          hopFlow(
                              transmissionContext._originalFlow,
                              transmissionContext._transformedFlow))
                      .build())
              .setAction(getFinalActionForDisposition(disposition))
              .build());

      Hop terminalHop = new Hop(new Node(currentNodeName), stepBuilder.build());

      transmissionContext._hopsSoFar.add(terminalHop);
      Trace trace = new Trace(disposition, transmissionContext._hopsSoFar);
      transmissionContext._flowTraces.add(trace);

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
  public SortedMap<Flow, List<Trace>> buildFlows() {
    Map<Flow, List<Trace>> flowTraces = new ConcurrentHashMap<>();
    _flows
        .parallelStream()
        .forEach(
            flow -> {
              List<Trace> currentFlowTraces =
                  flowTraces.computeIfAbsent(flow, k -> new ArrayList<>());
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
                        currentFlowTraces,
                        hops,
                        Maps.newTreeMap(),
                        flow,
                        flow);
                processHop(
                    ingressNodeName, ingressInterfaceName, transmissionContext, flow, breadcrumbs);
              } else {
                TransmissionContext transmissionContext =
                    new TransmissionContext(
                        Maps.newHashMap(),
                        new Node(flow.getIngressNode()),
                        currentFlowTraces,
                        hops,
                        Maps.newTreeMap(),
                        flow,
                        flow);
                processHop(ingressNodeName, null, transmissionContext, flow, breadcrumbs);
              }
            });
    return new TreeMap<>(flowTraces);
  }

  private void processHop(
      String currentNodeName,
      @Nullable String inputIfaceName,
      TransmissionContext oldTransmissionContext,
      Flow ingressFlow,
      Stack<Breadcrumb> breadcrumbs) {
    List<Step<?>> steps = new ArrayList<>();
    Configuration currentConfiguration = _configurations.get(currentNodeName);
    if (currentConfiguration == null) {
      throw new BatfishException(
          String.format(
              "Node %s is not in the network, cannot perform traceroute", currentNodeName));
    }

    Map<String, IpAccessList> aclDefinitions = currentConfiguration.getIpAccessLists();
    NavigableMap<String, IpSpace> namedIpSpaces = currentConfiguration.getIpSpaces();

    TransmissionContext transmissionContext = oldTransmissionContext.branch();

    // trace was received on a source interface of this hop
    if (inputIfaceName != null) {
      EnterInputIfaceStep enterIfaceStep =
          createEnterSrcIfaceStep(
              currentConfiguration,
              inputIfaceName,
              _ignoreFilters,
              ingressFlow,
              aclDefinitions,
              namedIpSpaces);
      steps.add(enterIfaceStep);

      if (enterIfaceStep.getAction() == StepAction.DENIED) {
        Hop deniedHop = new Hop(new Node(currentNodeName), ImmutableList.copyOf(steps));
        transmissionContext._hopsSoFar.add(deniedHop);
        Trace trace = new Trace(FlowDisposition.DENIED_IN, transmissionContext._hopsSoFar);
        transmissionContext._flowTraces.add(trace);
        return;
      }
    } else if (ingressFlow.getIngressVrf() != null) {
      // if inputIfaceName is not set for this hop, this is the originating step
      steps.add(
          OriginateStep.builder()
              .setDetail(
                  OriginateStepDetail.builder()
                      .setOriginatingVrf(ingressFlow.getIngressVrf())
                      .build())
              .setAction(StepAction.ORIGINATED)
              .build());
    }

    // Figure out where the trace came from..
    String vrfName;
    if (inputIfaceName == null) {
      vrfName = ingressFlow.getIngressVrf();
    } else {
      vrfName = currentConfiguration.getAllInterfaces().get(inputIfaceName).getVrfName();
    }

    // Loop detection
    Breadcrumb breadcrumb = new Breadcrumb(currentNodeName, vrfName, ingressFlow);
    if (breadcrumbs.contains(breadcrumb)) {
      Hop loopHop = new Hop(new Node(currentNodeName), ImmutableList.copyOf(steps));
      transmissionContext._hopsSoFar.add(loopHop);
      Trace trace = new Trace(FlowDisposition.LOOP, transmissionContext._hopsSoFar);
      transmissionContext._flowTraces.add(trace);
      return;
    }

    breadcrumbs.push(breadcrumb);
    // use try/finally to make sure we pop off the breadcrumb
    try {
      // Accept if the flow is destined for this vrf on this host.
      if (_dataPlane
          .getIpVrfOwners()
          .getOrDefault(ingressFlow.getDstIp(), ImmutableMap.of())
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
        transmissionContext._flowTraces.add(trace);
        return;
      }

      // Apply ingress NATs
      Flow currentFlow;
      if (inputIfaceName != null) {
        currentFlow =
            applyIngressNats(
                ingressFlow,
                aclDefinitions,
                namedIpSpaces,
                currentConfiguration.getAllInterfaces().get(inputIfaceName).getIngressNats());
      } else {
        currentFlow = ingressFlow;
      }
      Ip dstIp = currentFlow.getDstIp();

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
        transmissionContext._flowTraces.add(trace);
        return;
      }

      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByRoute =
          currentFib.getNextHopInterfacesByRoute(dstIp);

      List<RouteInfo> matchedRibRouteInfo =
          _dataPlane
              .getRibs()
              .get(currentNodeName)
              .get(vrfName)
              .longestPrefixMatch(dstIp)
              .stream()
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

                  ImmutableList.Builder<Step<?>> clonedStepsBuilder = ImmutableList.builder();
                  clonedStepsBuilder.addAll(steps);
                  clonedStepsBuilder.add(
                      RoutingStep.builder()
                          .setDetail(
                              RoutingStepDetail.builder().setRoutes(matchedRibRouteInfo).build())
                          .setAction(StepAction.FORWARDED)
                          .build());

                  if (nextHopInterfaceName.equals(Interface.NULL_INTERFACE_NAME)) {
                    clonedStepsBuilder.add(
                        ExitOutputIfaceStep.builder()
                            .setDetail(
                                ExitOutputIfaceStepDetail.builder()
                                    .setOutputInterface(
                                        new NodeInterfacePair(
                                            currentNodeName, Interface.NULL_INTERFACE_NAME))
                                    .build())
                            .setAction(StepAction.NULL_ROUTED)
                            .build());
                    Hop nullRoutedHop =
                        new Hop(new Node(currentNodeName), clonedStepsBuilder.build());
                    transmissionContext._hopsSoFar.add(nullRoutedHop);
                    Trace trace =
                        new Trace(FlowDisposition.NULL_ROUTED, transmissionContext._hopsSoFar);
                    transmissionContext._flowTraces.add(trace);
                    return;
                  }

                  Interface outgoingInterface =
                      _configurations
                          .get(nextHopInterface.getHostname())
                          .getAllInterfaces()
                          .get(nextHopInterface.getInterface());

                  // Apply any relevant NAT rules.
                  Flow newTransformedFlow =
                      applyEgressNats(
                          currentFlow,
                          inputIfaceName,
                          aclDefinitions,
                          namedIpSpaces,
                          outgoingInterface.getEgressNats());

                  SortedSet<Edge> edges =
                      _dataPlane.getTopology().getInterfaceEdges().get(nextHopInterface);

                  TransmissionContext clonedTransmissionContext =
                      new TransmissionContext(
                          aclDefinitions,
                          new Node(currentNodeName),
                          transmissionContext._flowTraces,
                          transmissionContext._hopsSoFar,
                          namedIpSpaces,
                          ingressFlow,
                          newTransformedFlow);
                  if (edges == null || edges.isEmpty()) {
                    updateDeniedOrUnreachableTrace(
                        currentNodeName,
                        outgoingInterface,
                        inputIfaceName,
                        clonedTransmissionContext,
                        clonedStepsBuilder);
                  } else {
                    processCurrentNextHopInterfaceEdges(
                        currentNodeName,
                        inputIfaceName,
                        dstIp,
                        nextHopInterfaceName,
                        finalNextHopIp,
                        edges,
                        clonedTransmissionContext,
                        clonedStepsBuilder,
                        breadcrumbs);
                  }
                });
      }
    } finally {
      breadcrumbs.pop();
    }
  }

  @Nullable
  private static Flow hopFlow(@Nullable Flow originalFlow, @Nullable Flow transformedFlow) {
    if (originalFlow == transformedFlow) {
      return null;
    } else {
      return transformedFlow;
    }
  }

  private void updateDeniedOrUnreachableTrace(
      String currentNodeName,
      Interface outInterface,
      String sourceInterfaceName,
      TransmissionContext transmissionContext,
      ImmutableList.Builder<Step<?>> stepsTillNow) {
    IpAccessList outFilter = outInterface.getOutgoingFilter();
    boolean denied = false;
    if (!_ignoreFilters && outFilter != null) {
      FilterResult filterResult =
          outFilter.filter(
              transmissionContext._transformedFlow,
              sourceInterfaceName,
              transmissionContext._aclDefinitions,
              transmissionContext._namedIpSpaces);
      denied = filterResult.getAction() == LineAction.DENY;
    }
    ExitOutputIfaceStep.Builder exitOutIfaceBuilder = ExitOutputIfaceStep.builder();
    exitOutIfaceBuilder.setDetail(
        ExitOutputIfaceStepDetail.builder()
            .setOutputInterface(new NodeInterfacePair(currentNodeName, outInterface.getName()))
            .setOutputFilter(outFilter != null ? outFilter.getName() : null)
            .setTransformedFlow(
                hopFlow(transmissionContext._originalFlow, transmissionContext._transformedFlow))
            .build());
    Trace trace;
    if (denied) {
      // add a denied out step action and terminate the current trace
      exitOutIfaceBuilder.setAction(StepAction.DENIED);
      List<Step<?>> currentSteps = stepsTillNow.add(exitOutIfaceBuilder.build()).build();
      Hop deniedOutHop = new Hop(new Node(currentNodeName), currentSteps);
      transmissionContext._hopsSoFar.add(deniedOutHop);
      trace = new Trace(FlowDisposition.DENIED_OUT, transmissionContext._hopsSoFar);
    } else {
      FlowDisposition disposition =
          computeDisposition(
              currentNodeName,
              outInterface.getName(),
              transmissionContext._transformedFlow.getDstIp());

      // create appropriate step
      exitOutIfaceBuilder.setAction(getFinalActionForDisposition(disposition));
      List<Step<?>> currentSteps = stepsTillNow.add(exitOutIfaceBuilder.build()).build();

      Hop terminalHop = new Hop(new Node(currentNodeName), currentSteps);

      transmissionContext._hopsSoFar.add(terminalHop);
      trace = new Trace(disposition, transmissionContext._hopsSoFar);
    }
    transmissionContext._flowTraces.add(trace);
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
